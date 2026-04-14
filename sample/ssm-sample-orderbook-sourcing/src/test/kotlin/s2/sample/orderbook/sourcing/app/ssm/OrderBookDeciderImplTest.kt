package s2.sample.orderbook.sourcing.app.ssm

import f2.dsl.fnc.invoke
import java.util.UUID
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import s2.sample.orderbook.sourcing.app.ssm.config.SpringTestBase
import s2.sample.subautomate.domain.OrderBookState
import s2.sample.subautomate.domain.model.OrderBook
import s2.sample.subautomate.domain.model.OrderBookId
import s2.sample.subautomate.domain.orderBook.OrderBookCloseCommand
import s2.sample.subautomate.domain.orderBook.OrderBookClosedEvent
import s2.sample.subautomate.domain.orderBook.OrderBookCreateCommand
import s2.sample.subautomate.domain.orderBook.OrderBookCreatedEvent
import s2.sample.subautomate.domain.orderBook.OrderBookDecide
import s2.sample.subautomate.domain.orderBook.OrderBookEvent
import s2.sample.subautomate.domain.orderBook.OrderBookPublishCommand
import s2.sample.subautomate.domain.orderBook.OrderBookPublishedEvent
import s2.sample.subautomate.domain.orderBook.OrderBookUpdateCommand
import s2.sample.subautomate.domain.orderBook.OrderBookUpdatedEvent
import s2.sourcing.dsl.Loader
import s2.sourcing.dsl.event.EventRepository
import s2.spring.sourcing.ssm.PolymorphicEnumSerializer

internal class OrderBookDeciderImplTest: SpringTestBase() {

	@Autowired
	lateinit var create: OrderBookDecide<OrderBookCreateCommand, OrderBookCreatedEvent>

	@Autowired
	lateinit var update: OrderBookDecide<OrderBookUpdateCommand, OrderBookUpdatedEvent>

	@Autowired
	lateinit var publish: OrderBookDecide<OrderBookPublishCommand, OrderBookPublishedEvent>

	@Autowired
	lateinit var close: OrderBookDecide<OrderBookCloseCommand, OrderBookClosedEvent>

	@Autowired
	lateinit var eventStore: EventRepository<OrderBookEvent, OrderBookId>

	@Autowired
	lateinit var builder: Loader<OrderBookEvent, OrderBook, OrderBookId>

	@Autowired
	lateinit var orderBookDeciderImpl: OrderBookDeciderImpl

	@Test
	fun `should create order book`(): Unit = runTest {
		val event = orderBookDeciderImpl.orderBookCreateDecider()
			.invoke(OrderBookCreateCommand("TheNewOrderBook"))
 		orderBookDeciderImpl.orderBookUpdateDecider()
			 .invoke(OrderBookUpdateCommand(id = event.id, name = "TheNewOrderBook2"))
		orderBookDeciderImpl.orderBookPublishDecider().invoke(OrderBookPublishCommand(id = event.id))
		orderBookDeciderImpl.orderBookCloseDecider().invoke(OrderBookCloseCommand(id = event.id))
		val events = eventStore.load(event.id).toList()
		assertThat(events).hasSize(4)
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
			val events = eventStore.load(event.id).toList()
			assertThat(events).hasSize(4)
		}.collect()
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
		}.map { event ->
			val events = eventStore.load(event.id).toList()
			Assertions.assertThat(events).hasSize(4)
		}.toList()
	}

	@Test
	fun `should replay event to build entity`(): Unit = runTest {
		var exception: Exception? = null
		val event = create(OrderBookCreateCommand("TheNewOrderBook"))

		try {
			val updateCommand = (0..12).map {
				OrderBookUpdateCommand(id = event.id, name = "TheNewOrderBook$it")
			}.asFlow()
			update.invoke(updateCommand).collect()
		} catch (e: Exception) {
			exception = e
		}

		assertThat(exception)
			.isNotNull()
			.hasMessageContaining("Duplicate events detected: ${event.id}, cannot be processed due to SSM limitations.")
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
		events.map { event ->
			val loadedEvent = eventStore.load(event.id)
			val entity = builder.load(loadedEvent)
			assertThat(entity?.name).startsWith("TheNewOrderBook")
			assertThat(entity?.status).isEqualTo(OrderBookState.Created)
		}
	}

	@Test
	fun json() {
		val json = Json {
			serializersModule = SerializersModule {
				polymorphic( OrderBookState::class, PolymorphicEnumSerializer( OrderBookState.serializer()))
			}
		}

		val event: OrderBookEvent = OrderBookCreatedEvent(
			name = "test",
			id = UUID.randomUUID().toString(),
			OrderBookState.Created
		)

		val value = json.encodeToString(event)
		val eventParsed = json.decodeFromString(OrderBookEvent.serializer(), value)
		Assertions.assertThat(eventParsed::class).isEqualTo(OrderBookCreatedEvent::class)
	}
}
