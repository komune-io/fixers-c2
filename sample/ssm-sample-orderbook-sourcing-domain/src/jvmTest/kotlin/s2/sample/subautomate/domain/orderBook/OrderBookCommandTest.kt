package s2.sample.subautomate.domain.orderBook

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import s2.sample.subautomate.domain.OrderBookState
import s2.sample.subautomate.domain.model.OrderBookId

class OrderBookCommandTest {

	@Test
	fun `OrderBookCreatedEvent uses OrderBookId for s2Id`() {
		val event = OrderBookCreatedEvent(name = "test", id = "book-1", state = OrderBookState.Created)
		val id: OrderBookId = event.s2Id()
		assertThat(id).isEqualTo("book-1")
	}

	@Test
	fun `OrderBookUpdatedEvent uses OrderBookId for s2Id`() {
		val event = OrderBookUpdatedEvent(name = "test", id = "book-2", state = OrderBookState.Created)
		val id: OrderBookId = event.s2Id()
		assertThat(id).isEqualTo("book-2")
	}

	@Test
	fun `OrderBookPublishedEvent uses OrderBookId for s2Id`() {
		val event = OrderBookPublishedEvent(id = "book-3", state = OrderBookState.Published)
		val id: OrderBookId = event.s2Id()
		assertThat(id).isEqualTo("book-3")
	}

	@Test
	fun `OrderBookClosedEvent uses OrderBookId for s2Id`() {
		val event = OrderBookClosedEvent(id = "book-4", state = OrderBookState.Closed)
		val id: OrderBookId = event.s2Id()
		assertThat(id).isEqualTo("book-4")
	}

	@Test
	fun `all events share OrderBookId type through sealed class`() {
		val events: List<OrderBookEvent> = listOf(
			OrderBookCreatedEvent(name = "a", id = "id-1", state = OrderBookState.Created),
			OrderBookUpdatedEvent(name = "b", id = "id-2", state = OrderBookState.Created),
			OrderBookPublishedEvent(id = "id-3", state = OrderBookState.Published),
			OrderBookClosedEvent(id = "id-4", state = OrderBookState.Closed),
		)
		val ids: List<OrderBookId> = events.map { it.s2Id() }
		assertThat(ids).containsExactly("id-1", "id-2", "id-3", "id-4")
	}
}
