package s2.sample.orderbook.sourcing.app.ssm.config

import kotlin.reflect.KClass
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import s2.sample.orderbook.sourcing.app.ssm.OrderBookModelView
import s2.sample.subautomate.domain.OrderBookState
import s2.sample.subautomate.domain.model.OrderBook
import s2.sample.subautomate.domain.model.OrderBookId
import s2.sample.subautomate.domain.orderBook.OrderBookEvent
import s2.sample.subautomate.domain.orderBookAutomate
import s2.spring.automate.sourcing.S2AutomateDeciderSpring
import s2.spring.sourcing.ssm.PolymorphicEnumSerializer
import s2.spring.sourcing.ssm.S2SourcingSsmAdapter
import ssm.chaincode.dsl.model.Agent
import ssm.chaincode.dsl.model.uri.ChaincodeUri
import ssm.chaincode.dsl.model.uri.from
import ssm.sdk.sign.extention.loadFromFile

@Configuration
class OrderBookAutomateConfig(orderBookS2Aggregate: OrderBookS2Aggregate)
	: S2SourcingSsmAdapter<
		OrderBook,
		OrderBookState,
		OrderBookEvent,
		OrderBookId,
		OrderBookS2Aggregate
	>(orderBookS2Aggregate, OrderBookModelView()) {
	override fun automate() = orderBookAutomate("sourcing-ssm")

	override fun entityType(): KClass<OrderBookEvent> {
		return OrderBookEvent::class
	}

	override fun json(): Json = Json {
		serializersModule = SerializersModule {
			polymorphic(OrderBookState::class, PolymorphicEnumSerializer(OrderBookState.serializer()))
		}
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
class OrderBookS2Aggregate : S2AutomateDeciderSpring<OrderBook, OrderBookState, OrderBookEvent, OrderBookId>()
