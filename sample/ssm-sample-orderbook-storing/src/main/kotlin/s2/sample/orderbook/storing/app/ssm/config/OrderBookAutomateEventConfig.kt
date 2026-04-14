package s2.sample.orderbook.storing.app.ssm.config

import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import s2.sample.subautomate.domain.OrderBookState
import s2.sample.subautomate.domain.model.OrderBook
import s2.sample.subautomate.domain.model.OrderBookId
import s2.sample.subautomate.domain.orderBookAutomate
import s2.spring.automate.executor.S2AutomateExecutorSpring
import s2.spring.automate.ssm.S2SsmConfigurerAdapter
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.uri.ChaincodeUri
import ssm.chaincode.dsl.model.uri.from
import ssm.sdk.sign.extention.loadFromFile

@Configuration
class OrderBookAutomateEventConfig(
	private val orderBookS2Aggregate: OrderBookS2EventAggregate
)
	: S2SsmConfigurerAdapter<
		OrderBookState,
		OrderBookId,
		OrderBook,
		OrderBookS2EventAggregate
		>() {
	override fun automate() = orderBookAutomate("storing")

	override fun executor(): OrderBookS2EventAggregate = orderBookS2Aggregate

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
class OrderBookS2EventAggregate : S2AutomateExecutorSpring<OrderBookState, OrderBookId, OrderBook>()
