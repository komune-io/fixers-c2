package s2.sample.orderbook.storing.app.ssm.config

import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import s2.dsl.automate.builder.s2
import s2.sample.subautomate.domain.OrderBookState
import s2.sample.subautomate.domain.Role
import s2.sample.subautomate.domain.model.OrderBook
import s2.sample.subautomate.domain.model.OrderBookId
import s2.sample.subautomate.domain.orderBook.OrderBookCloseCommand
import s2.sample.subautomate.domain.orderBook.OrderBookCreateCommand
import s2.sample.subautomate.domain.orderBook.OrderBookPublishCommand
import s2.sample.subautomate.domain.orderBook.OrderBookUpdateCommand
import s2.spring.automate.executor.S2AutomateExecutorSpring
import s2.spring.automate.ssm.S2SsmConfigurerAdapter
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.uri.ChaincodeUri
import ssm.chaincode.dsl.model.uri.from
import ssm.sdk.sign.extention.loadFromFile

@Configuration
class OrderBookAutomateConfig(
	private val orderBookS2Aggregate: OrderBookS2Aggregate
)
	: S2SsmConfigurerAdapter<
		OrderBookState,
		OrderBookId,
		OrderBook,
		OrderBookS2Aggregate
		>() {
	override fun automate() = orderBookAutomate2("storing")

	override fun executor(): OrderBookS2Aggregate = orderBookS2Aggregate

	override fun entityType(): Class<OrderBook> {
		return OrderBook::class.java
	}

	override fun chaincodeUri(): ChaincodeUri {
		return ChaincodeUri.from(
			channelId = "sandbox",
			chaincodeId = "ssm",
		)
	}

	override fun signerAgent(): Agent {
		return Agent.loadFromFile("ssm-admin","user/ssm-admin")
	}

}

@Service
class OrderBookS2Aggregate : S2AutomateExecutorSpring<OrderBookState, OrderBookId, OrderBook>()


fun orderBookAutomate2(unique: String) = s2 {
	name = "S2OrderBook-$unique-2"
	transaction<OrderBookCreateCommand> {
		to = OrderBookState.Created
		role = Role
	}
	selfTransaction<OrderBookUpdateCommand> {
		states += OrderBookState.Created
		role = Role
	}
	transaction<OrderBookPublishCommand> {
		from = OrderBookState.Created
		to = OrderBookState.Published
		role = Role
	}
	transaction<OrderBookCloseCommand> {
		from = OrderBookState.Created
		to = OrderBookState.Closed
		role = Role
	}
	transaction<OrderBookCloseCommand> {
		from = OrderBookState.Published
		to = OrderBookState.Closed
		role = Role
	}
}
