package s2.sample.orderbook.sourcing.app.ssm

import java.util.UUID
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Service
import s2.sample.orderbook.sourcing.app.ssm.config.OrderBookS2Aggregate
import s2.sample.subautomate.domain.OrderBookState
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
        aggregate.init { cmd ->
            OrderBookCreatedEvent(id = UUID.randomUUID().toString(), name = cmd.name, state = OrderBookState.Created)
        }

    @Bean
    override fun orderBookUpdateDecider(): OrderBookDecide<OrderBookUpdateCommand, OrderBookUpdatedEvent> =
        aggregate.decide { cmd, _ ->
            OrderBookUpdatedEvent(id = cmd.id, name = cmd.name, state = OrderBookState.Created)
        }

    @Bean
    override fun orderBookPublishDecider(): OrderBookDecide<OrderBookPublishCommand, OrderBookPublishedEvent> =
        aggregate.decide { cmd, _ ->
            OrderBookPublishedEvent(
                id = cmd.id,
                state = OrderBookState.Published
            )
        }

    @Bean
    override fun orderBookCloseDecider(): OrderBookDecide<OrderBookCloseCommand, OrderBookClosedEvent> =
        aggregate.decide { cmd, _ ->
            OrderBookClosedEvent(
                id = cmd.id,
                state = OrderBookState.Closed
            )
        }
}
