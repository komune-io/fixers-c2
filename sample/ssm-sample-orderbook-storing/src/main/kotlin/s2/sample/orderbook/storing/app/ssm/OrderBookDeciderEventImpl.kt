package s2.sample.orderbook.storing.app.ssm

import f2.dsl.fnc.F2Function
import java.util.UUID
import org.springframework.stereotype.Service
import s2.sample.orderbook.storing.app.ssm.config.OrderBookS2EventAggregate
import s2.sample.subautomate.domain.OrderBookState
import s2.sample.subautomate.domain.model.OrderBook
import s2.sample.subautomate.domain.model.name
import s2.sample.subautomate.domain.model.status
import s2.sample.subautomate.domain.orderBook.OrderBookCloseCommand
import s2.sample.subautomate.domain.orderBook.OrderBookClosedEvent
import s2.sample.subautomate.domain.orderBook.OrderBookCreateCommand
import s2.sample.subautomate.domain.orderBook.OrderBookCreatedEvent
import s2.sample.subautomate.domain.orderBook.OrderBookDecide
import s2.sample.subautomate.domain.orderBook.OrderBookDecider
import s2.sample.subautomate.domain.orderBook.OrderBookPublishCommand
import s2.sample.subautomate.domain.orderBook.OrderBookPublishedEvent
import s2.sample.subautomate.domain.orderBook.OrderBookUpdateCommand
import s2.sample.subautomate.domain.orderBook.OrderBookUpdatedEvent

@Service
class OrderBookDeciderEventImpl(
	private val aggregate: OrderBookS2EventAggregate
) : OrderBookDecider {

	override fun orderBookCreateDecider()
		: OrderBookDecide<OrderBookCreateCommand, OrderBookCreatedEvent> = F2Function { msg ->
		aggregate.evolve(msg) { cmd ->
			val id = UUID.randomUUID().toString()
			OrderBook(
				id = id,
				name = cmd.name,
				status = OrderBookState.Created
			) to OrderBookCreatedEvent(id = id, name = cmd.name, state = OrderBookState.Created)
		}
	}

	override fun orderBookUpdateDecider()
		: OrderBookDecide<OrderBookUpdateCommand, OrderBookUpdatedEvent> = F2Function { msg ->
		aggregate.evolve(msg) { cmd, entity ->
			val ent = OrderBook.name.set(entity, cmd.name)
			val event =	OrderBookUpdatedEvent(id = cmd.id, name = cmd.name, state = OrderBookState.Created)
			ent to event
		}
	}

	override fun orderBookPublishDecider()
		: OrderBookDecide<OrderBookPublishCommand, OrderBookPublishedEvent> = F2Function { msg ->
		aggregate.evolve(msg) { cmd, entity ->
			OrderBook.status.set(entity, OrderBookState.Published) to OrderBookPublishedEvent(
				id = cmd.id,
				state = OrderBookState.Published
			)
		}
	}

	override fun orderBookCloseDecider()
		: OrderBookDecide<OrderBookCloseCommand, OrderBookClosedEvent> = F2Function { msg ->
		aggregate.evolve(msg) { cmd, entity ->
			OrderBook.status.set(entity, OrderBookState.Closed) to
				OrderBookClosedEvent(
					id = cmd.id,
					state = OrderBookState.Closed
				)
		}
	}
}
