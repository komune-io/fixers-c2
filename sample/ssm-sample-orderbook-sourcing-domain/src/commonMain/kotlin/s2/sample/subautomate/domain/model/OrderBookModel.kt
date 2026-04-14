package s2.sample.subautomate.domain.model

import arrow.optics.optics
import s2.dsl.automate.model.WithS2Id
import s2.dsl.automate.model.WithS2State
import s2.sample.subautomate.domain.OrderBookState

typealias OrderBookId = String

@optics
data class OrderBook(
	val id: OrderBookId,
	val name: String,
	val status: OrderBookState
): WithS2Id<OrderBookId>, WithS2State<OrderBookState> {
	override fun s2Id() = id
	override fun s2State() = status
	companion object
}
