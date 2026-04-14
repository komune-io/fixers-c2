package s2.sample.subautomate.domain.model

import arrow.optics.optics

typealias OrderId = String

@optics
data class Order(
	val id: OrderId,
) {
	companion object
}
