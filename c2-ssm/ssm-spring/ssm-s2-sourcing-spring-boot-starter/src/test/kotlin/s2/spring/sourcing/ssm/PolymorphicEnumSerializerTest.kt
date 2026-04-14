package s2.spring.sourcing.ssm

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PolymorphicEnumSerializerTest {

	enum class TestState(val position: Int) {
		Created(0),
		Active(1),
		Closed(2)
	}

	object TestStateSerializer : KSerializer<TestState> {
		override val descriptor: SerialDescriptor =
			PrimitiveSerialDescriptor("TestState", PrimitiveKind.STRING)

		override fun serialize(encoder: Encoder, value: TestState) {
			encoder.encodeString(value.name)
		}

		override fun deserialize(decoder: Decoder): TestState {
			return TestState.valueOf(decoder.decodeString())
		}
	}

	private val serializer = PolymorphicEnumSerializer(TestStateSerializer)
	private val json = Json

	@Test
	fun `serialize wraps enum value in object structure`() {
		val encoded = json.encodeToString(serializer, TestState.Active)
		assertThat(encoded).contains("value")
		assertThat(encoded).contains("Active")
	}

	@Test
	fun `deserialize unwraps enum value from object structure`() {
		val encoded = json.encodeToString(serializer, TestState.Created)
		val decoded = json.decodeFromString(serializer, encoded)
		assertThat(decoded).isEqualTo(TestState.Created)
	}

	@Test
	fun `roundtrip preserves all enum values`() {
		TestState.entries.forEach { state ->
			val encoded = json.encodeToString(serializer, state)
			val decoded = json.decodeFromString(serializer, encoded)
			assertThat(decoded).isEqualTo(state)
		}
	}
}
