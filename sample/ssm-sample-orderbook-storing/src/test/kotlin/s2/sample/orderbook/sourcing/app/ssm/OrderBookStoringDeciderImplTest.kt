package s2.sample.orderbook.sourcing.app.ssm

import f2.dsl.fnc.invoke
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import s2.automate.core.config.S2BatchProperties
import s2.automate.core.context.AutomateContext
import s2.sample.orderbook.sourcing.app.ssm.config.SpringTestBase
import s2.sample.orderbook.storing.app.ssm.OrderBookDeciderImpl
import s2.sample.orderbook.storing.app.ssm.config.OrderBookAutomateConfig
import s2.sample.subautomate.domain.OrderBookState
import s2.sample.subautomate.domain.orderBook.OrderBookCloseCommand
import s2.sample.subautomate.domain.orderBook.OrderBookClosedEvent
import s2.sample.subautomate.domain.orderBook.OrderBookCreateCommand
import s2.sample.subautomate.domain.orderBook.OrderBookCreatedEvent
import s2.sample.subautomate.domain.orderBook.OrderBookDecide
import s2.sample.subautomate.domain.orderBook.OrderBookPublishCommand
import s2.sample.subautomate.domain.orderBook.OrderBookPublishedEvent
import s2.sample.subautomate.domain.orderBook.OrderBookUpdateCommand
import s2.sample.subautomate.domain.orderBook.OrderBookUpdatedEvent
import s2.sample.subautomate.domain.orderBookAutomate

internal class OrderBookStoringDeciderImplTest: SpringTestBase() {

	@Autowired
	lateinit var create: OrderBookDecide<OrderBookCreateCommand, OrderBookCreatedEvent>

	@Autowired
	lateinit var update: OrderBookDecide<OrderBookUpdateCommand, OrderBookUpdatedEvent>

	@Autowired
	lateinit var publish: OrderBookDecide<OrderBookPublishCommand, OrderBookPublishedEvent>

	@Autowired
	lateinit var close: OrderBookDecide<OrderBookCloseCommand, OrderBookClosedEvent>

	@Autowired
	lateinit var orderBookAutomateConfig: OrderBookAutomateConfig

	@Autowired
	lateinit var orderBookDeciderImpl: OrderBookDeciderImpl

	@Test
	fun `should create order book`(): Unit = runTest {
		val event = orderBookDeciderImpl.orderBookCreateDecider()
			.invoke(OrderBookCreateCommand("TheNewOrderBook"))
 		orderBookDeciderImpl.orderBookUpdateDecider()
			 .invoke(OrderBookUpdateCommand(id = event.id, name = "TheNewOrderBookAfterUpdate"))
		orderBookDeciderImpl.orderBookPublishDecider().invoke(OrderBookPublishCommand(id = event.id))
		orderBookDeciderImpl.orderBookCloseDecider().invoke(OrderBookCloseCommand(id = event.id))
		val ssmAutomatePersister = orderBookAutomateConfig.aggregateRepository()
		val entity = ssmAutomatePersister.load(AutomateContext(
			automate = orderBookAutomate("storing-test-1"),
			S2BatchProperties()
		), event.id)
		Assertions.assertThat(entity?.name).isEqualTo("TheNewOrderBookAfterUpdate")
		Assertions.assertThat(entity?.status).isEqualTo(OrderBookState.Closed)
	}
	@Test
	fun `should create flow(24-6) of order book`(): Unit = runTest {
		(0..24).asFlow().map {
			OrderBookCreateCommand("TheNewOrderBook$it")
		}.let {
			orderBookDeciderImpl.orderBookCreateDecider().invoke(it)
		}.map { event ->
			OrderBookUpdateCommand(id = event.id, name = "TheNewOrderBook2")
		}.let {
			orderBookDeciderImpl.orderBookUpdateDecider().invoke(it)
		}.map { event ->
			OrderBookPublishCommand(id = event.id)
		}.let {
			orderBookDeciderImpl.orderBookPublishDecider().invoke(it)
		}.map { event ->
			OrderBookCloseCommand(id = event.id)
		}.let {
			orderBookDeciderImpl.orderBookCloseDecider().invoke(it)
		}.map { event ->
			val ssmAutomatePersister = orderBookAutomateConfig.aggregateRepository()
			val entity = ssmAutomatePersister.load(
					AutomateContext(automate = orderBookAutomate("storing-test-2"),
					S2BatchProperties()
				),
				event.id
			)
			Assertions.assertThat(entity?.name).isEqualTo("TheNewOrderBook2")
		}
	}

	@Test
	fun `should create flow(4-6) of order book`(): Unit = runTest {
		(0..4).asFlow().map {
			OrderBookCreateCommand("TheNewOrderBook$it")
		}.let {
			orderBookDeciderImpl.orderBookCreateDecider().invoke(it)
		}.map { event ->
			OrderBookUpdateCommand(id = event.id, name = "TheNewOrderBook2")
		}.let {
			orderBookDeciderImpl.orderBookUpdateDecider().invoke(it)
		}.map { event ->
			OrderBookPublishCommand(id = event.id)
		}.let {
			orderBookDeciderImpl.orderBookPublishDecider().invoke(it)
		}.map { event ->
			OrderBookCloseCommand(id = event.id)
		}.let {
			orderBookDeciderImpl.orderBookCloseDecider().invoke(it)
		}.toList().forEach { event ->
			val ssmAutomatePersister = orderBookAutomateConfig.aggregateRepository()
			val entity = ssmAutomatePersister.load(AutomateContext(
				automate = orderBookAutomate("storing-test-3"),
				S2BatchProperties()
			), event.id)
//			assertThat(entity?.name).isEqualTo("TheNewOrderBook2")
			assertThat(entity?.status).isEqualTo(OrderBookState.Closed)
		}
	}

	@Test
	fun `should flow event to build entity`(): Unit = runTest {
		val all = flowOf(
			OrderBookCreateCommand("TheNewOrderBook"),
			OrderBookCreateCommand("TheNewOrderBook1"),
			OrderBookCreateCommand("TheNewOrderBook2"),
			OrderBookCreateCommand("TheNewOrderBook3"),
			OrderBookCreateCommand("TheNewOrderBook4"),
		)

		val events = create.invoke(all).toList()
		assertThat(events).hasSize(5)
		events.forEach { event ->
			val ssmAutomatePersister = orderBookAutomateConfig.aggregateRepository()
			val entity = ssmAutomatePersister.load(
				AutomateContext(
					automate = orderBookAutomate("storing-test-4"),
					S2BatchProperties()
				),
				event.id
			)
			assertThat(entity?.name).startsWith("TheNewOrderBook")
			assertThat(entity?.status).isEqualTo(OrderBookState.Created)
		}
	}

}
