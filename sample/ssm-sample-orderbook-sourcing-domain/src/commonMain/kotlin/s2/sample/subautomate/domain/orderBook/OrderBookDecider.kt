package s2.sample.subautomate.domain.orderBook

interface OrderBookDecider {
	fun orderBookCreateDecider(): OrderBookDecide<OrderBookCreateCommand, OrderBookCreatedEvent>
	fun orderBookUpdateDecider(): OrderBookDecide<OrderBookUpdateCommand, OrderBookUpdatedEvent>
	fun orderBookPublishDecider(): OrderBookDecide<OrderBookPublishCommand, OrderBookPublishedEvent>
	fun orderBookCloseDecider(): OrderBookDecide<OrderBookCloseCommand, OrderBookClosedEvent>
}
