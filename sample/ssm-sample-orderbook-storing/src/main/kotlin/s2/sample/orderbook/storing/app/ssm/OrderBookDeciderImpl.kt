package s2.sample.orderbook.storing.app.ssm

import java.util.UUID
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service
import s2.sample.orderbook.storing.app.ssm.config.OrderBookS2Aggregate
import s2.sample.subautomate.domain.OrderBookState
import s2.sample.subautomate.domain.model.OrderBook
import s2.sample.subautomate.domain.orderbook.OrderBookCloseCommand
import s2.sample.subautomate.domain.orderbook.OrderBookClosedEvent
import s2.sample.subautomate.domain.orderbook.OrderBookCreateCommand
import s2.sample.subautomate.domain.orderbook.OrderBookCreatedEvent
import s2.sample.subautomate.domain.orderbook.OrderBookDecide
import s2.sample.subautomate.domain.orderbook.OrderBookDecider
import s2.sample.subautomate.domain.orderbook.OrderBookPublishCommand
import s2.sample.subautomate.domain.orderbook.OrderBookPublishedEvent
import s2.sample.subautomate.domain.orderbook.OrderBookUpdateCommand
import s2.sample.subautomate.domain.orderbook.OrderBookUpdatedEvent

@Service
class OrderBookDeciderImpl(
    private val aggregate: OrderBookS2Aggregate
) : OrderBookDecider {

    @Bean
    override fun orderBookCreateDecider(): OrderBookDecide<OrderBookCreateCommand, OrderBookCreatedEvent> =
        aggregate.evolve { cmd ->
            val id = UUID.randomUUID().toString()
            OrderBook(
                id = id,
                name = cmd.name,
                status = OrderBookState.Created
            ) to OrderBookCreatedEvent(id = id, name = cmd.name, state = OrderBookState.Created)
        }

    @Bean
    override fun orderBookUpdateDecider(): OrderBookDecide<OrderBookUpdateCommand, OrderBookUpdatedEvent> =
        aggregate.evolve { cmd, entity ->
            val ent = entity.copy(name = cmd.name)
            val event = OrderBookUpdatedEvent(id = cmd.id, name = cmd.name, state = OrderBookState.Created)
            ent to event
        }

    @Bean
    override fun orderBookPublishDecider(): OrderBookDecide<OrderBookPublishCommand, OrderBookPublishedEvent> =
        aggregate.evolve { cmd, entity ->
            entity.copy(status = OrderBookState.Published) to OrderBookPublishedEvent(
                id = cmd.id,
                state = OrderBookState.Published
            )
        }

    @Bean
    override fun orderBookCloseDecider(): OrderBookDecide<OrderBookCloseCommand, OrderBookClosedEvent> =
        aggregate.evolve { cmd, entity ->
            entity.copy(status = OrderBookState.Closed) to
                    OrderBookClosedEvent(
                        id = cmd.id,
                        state = OrderBookState.Closed
                    )
        }

}
