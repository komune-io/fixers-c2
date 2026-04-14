package s2.sample.subautomate.domain.orderBook

import f2.dsl.fnc.F2Function
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import s2.dsl.automate.Evt
import s2.dsl.automate.S2Command
import s2.dsl.automate.S2InitCommand
import s2.dsl.automate.model.WithS2Id
import s2.dsl.automate.model.WithS2State
import s2.sample.subautomate.domain.OrderBookState
import s2.sample.subautomate.domain.model.OrderBookId

@Serializable
data class OrderBookCreateCommand(
	val name: String
) : S2InitCommand

@Serializable
@SerialName("OrderBookCreatedEvent")
data class OrderBookCreatedEvent(
	val name: String,
	val id: OrderBookId,
	val state: OrderBookState
) : OrderBookEvent() {
	override fun s2Id() = id
	override fun s2State() = state
}

@Serializable
@SerialName("OrderBookUpdateCommand")
data class OrderBookUpdateCommand(
	val name: String,
	override val id: OrderBookId
) : OrderBookCommand()

@Serializable
@SerialName("OrderBookUpdatedEvent")
data class OrderBookUpdatedEvent(
	val name: String,
	val id: OrderBookId,
	val state: OrderBookState
) : OrderBookEvent() {
	override fun s2Id() = id
	override fun s2State() = state
}

@Serializable
@SerialName("OrderBookPublishCommand")
data class OrderBookPublishCommand(override val id: OrderBookId) : OrderBookCommand()

@Serializable
@SerialName("OrderBookPublishedEvent")
data class OrderBookPublishedEvent(
	val id: OrderBookId,
	val state: OrderBookState
) : OrderBookEvent() {
	override fun s2Id() = id
	override fun s2State() = state
}

@Serializable
@SerialName("OrderBookCloseCommand")
data class OrderBookCloseCommand(override val id: OrderBookId) : OrderBookCommand()

@Serializable
@SerialName("OrderBookClosedEvent")
data class OrderBookClosedEvent(
	val id: OrderBookId, val state: OrderBookState
) : OrderBookEvent() {
	override fun s2Id() = id
	override fun s2State() = state
}

@Serializable
sealed class OrderBookEvent : Evt, WithS2Id<OrderBookId>, WithS2State<OrderBookState>

@Serializable
sealed class OrderBookCommand : S2Command<OrderBookId>


typealias OrderBookDecide<COMMAND, EVENT> = F2Function<COMMAND, EVENT>
