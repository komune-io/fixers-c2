package s2.sample.orderbook.sourcing.app.ssm.config

import org.springframework.stereotype.Service
import s2.sample.subautomate.domain.OrderBookState
import s2.sample.subautomate.domain.model.OrderBook
import s2.sample.subautomate.domain.model.OrderBookId
import s2.sample.subautomate.domain.orderBook.OrderBookEvent
import s2.spring.automate.sourcing.S2AutomateDeciderSpring

@Service
class OrderBookS2Aggregate : S2AutomateDeciderSpring<OrderBook, OrderBookState, OrderBookEvent, OrderBookId>()
