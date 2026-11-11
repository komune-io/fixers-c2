package s2.sample.orderbook.sourcing.app.ssm

import s2.sample.subautomate.domain.OrderBookState
import s2.sample.subautomate.domain.model.OrderBook
import s2.sample.subautomate.domain.orderbook.OrderBookClosedEvent
import s2.sample.subautomate.domain.orderbook.OrderBookCreatedEvent
import s2.sample.subautomate.domain.orderbook.OrderBookEvent
import s2.sample.subautomate.domain.orderbook.OrderBookPublishedEvent
import s2.sample.subautomate.domain.orderbook.OrderBookUpdatedEvent
import s2.sourcing.dsl.view.View

class OrderBookModelView: View<OrderBookEvent, OrderBook> {

    override suspend fun evolve(event: OrderBookEvent, model: OrderBook?): OrderBook? = when(event) {
        is OrderBookCreatedEvent -> created(event)
        is OrderBookClosedEvent -> closed(model, event)
        is OrderBookPublishedEvent -> published(model, event)
        is OrderBookUpdatedEvent -> updated(model, event)
    }

    private fun closed(entity: OrderBook?, event: OrderBookClosedEvent): OrderBook? =
        entity?.copy(status = event.state)

    private fun published(entity: OrderBook?, event: OrderBookPublishedEvent): OrderBook? =
        entity?.copy(status = event.state)

    private fun updated(entity: OrderBook?, event: OrderBookUpdatedEvent): OrderBook? =
        entity?.copy(name = event.name)

    private fun created(event: OrderBookCreatedEvent): OrderBook {
        return OrderBook(
            id = event.id,
            name = event.name,
            status = OrderBookState.Created
        )
    }
}
