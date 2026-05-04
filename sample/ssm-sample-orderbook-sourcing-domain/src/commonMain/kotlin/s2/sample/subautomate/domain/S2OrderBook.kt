package s2.sample.subautomate.domain

import kotlinx.serialization.Serializable
import s2.dsl.automate.S2Role
import s2.dsl.automate.S2State
import s2.dsl.automate.builder.s2
import s2.sample.subautomate.domain.orderBook.OrderBookCloseCommand
import s2.sample.subautomate.domain.orderBook.OrderBookClosedEvent
import s2.sample.subautomate.domain.orderBook.OrderBookCreateCommand
import s2.sample.subautomate.domain.orderBook.OrderBookCreatedEvent
import s2.sample.subautomate.domain.orderBook.OrderBookPublishCommand
import s2.sample.subautomate.domain.orderBook.OrderBookPublishedEvent
import s2.sample.subautomate.domain.orderBook.OrderBookUpdateCommand
import s2.sample.subautomate.domain.orderBook.OrderBookUpdatedEvent

object Role: S2Role

@Serializable
enum class OrderBookState(override var position: Int): S2State {
    Created(0),
    Published(1),
    Closed(2)
}

fun orderBookAutomate(unique: String) = s2 {
    name = "S2OrderBook-$unique"
    transaction<OrderBookCreateCommand> {
        to = OrderBookState.Created
        role = Role
        evt = OrderBookCreatedEvent::class
    }
    selfTransaction<OrderBookUpdateCommand> {
        states += OrderBookState.Created
        role = Role
        evt = OrderBookUpdatedEvent::class
    }
    transaction<OrderBookPublishCommand> {
        from = OrderBookState.Created
        to = OrderBookState.Published
        role = Role
        evt = OrderBookPublishedEvent::class
    }
    transaction<OrderBookCloseCommand> {
        from = OrderBookState.Created
        to = OrderBookState.Closed
        role = Role
        evt = OrderBookClosedEvent::class
    }
    transaction<OrderBookCloseCommand> {
        from = OrderBookState.Published
        to = OrderBookState.Closed
        role = Role
        evt = OrderBookClosedEvent::class
    }
}
