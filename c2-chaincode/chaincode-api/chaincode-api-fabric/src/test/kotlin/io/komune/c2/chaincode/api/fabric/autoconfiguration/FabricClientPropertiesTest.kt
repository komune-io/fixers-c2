package io.komune.c2.chaincode.api.fabric.autoconfiguration

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource

/**
 * Unit tests for FabricClientProperties — default value, setter validation, and real
 * Spring Binder binding of `coop.fabric.parallelism` (the same mechanism
 * @EnableConfigurationProperties uses at boot, without a full application context).
 */
class FabricClientPropertiesTest {

    @Test
    fun `defaults to 256 when nothing is configured`() {
        assertThat(FabricClientProperties().parallelism).isEqualTo(256)
    }

    @Test
    fun `accepts the lower bound of 1`() {
        val properties = FabricClientProperties().apply { parallelism = 1 }
        assertThat(properties.parallelism).isEqualTo(1)
    }

    @Test
    fun `rejects zero and negative values with a property-named message`() {
        assertThatThrownBy { FabricClientProperties().parallelism = 0 }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("coop.fabric.parallelism")
            .hasMessageContaining("was 0")

        assertThatThrownBy { FabricClientProperties().parallelism = -8 }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("was -8")
    }

    @Test
    fun `a rejected value leaves the previous value in place`() {
        val properties = FabricClientProperties()
        runCatching { properties.parallelism = 0 }
        assertThat(properties.parallelism).isEqualTo(256)
    }

    @Test
    fun `binds coop fabric parallelism from configuration`() {
        val source = MapConfigurationPropertySource(mapOf("coop.fabric.parallelism" to "512"))
        val bound = Binder(source).bind("coop.fabric", FabricClientProperties::class.java).get()
        assertThat(bound.parallelism).isEqualTo(512)
    }

    @Test
    fun `binding a non-positive value fails at configuration time, not at dispatcher creation`() {
        val source = MapConfigurationPropertySource(mapOf("coop.fabric.parallelism" to "0"))
        assertThatThrownBy { Binder(source).bind("coop.fabric", FabricClientProperties::class.java).get() }
            .hasStackTraceContaining("coop.fabric.parallelism must be >= 1")
    }

    @Test
    fun `an absent property binds to the default`() {
        val source = MapConfigurationPropertySource(emptyMap<String, String>())
        val bound = Binder(source).bindOrCreate("coop.fabric", FabricClientProperties::class.java)
        assertThat(bound.parallelism).isEqualTo(256)
    }
}
