package ssm.tx.config.spring.autoconfigure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.aot.generate.ClassNameGenerator
import org.springframework.aot.generate.DefaultGenerationContext
import org.springframework.aot.generate.GeneratedMethods
import org.springframework.aot.generate.InMemoryGeneratedFiles
import org.springframework.aot.generate.MethodReference
import org.springframework.aot.hint.TypeReference
import org.springframework.beans.factory.aot.BeanFactoryInitializationCode
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.javapoet.ClassName
import ssm.chaincode.dsl.config.SsmChaincodeProperties

class SsmTxAotProcessorTest {

	@Test
	fun `aot contribution registers the chaincodeSsmConfig bean method, its types and constructors`() {
		val generationContext = DefaultGenerationContext(
			ClassNameGenerator(ClassName.get("ssm.tx.config.spring.autoconfigure", "AotTest")),
			InMemoryGeneratedFiles(),
		)

		SsmTxAutoConfigurationBeanFactoryInitializationAotProcessor()
			.processAheadOfTime(DefaultListableBeanFactory())
			.applyTo(generationContext, stubInitializationCode())

		val reflection = generationContext.runtimeHints.reflection()
		val configHint = reflection.getTypeHint(TypeReference.of(SsmTxAutoConfiguration::class.java))
		assertThat(configHint).isNotNull
		val methodHint = configHint!!.methods().toList()
			.single { it.name == SsmTxAutoConfiguration::chaincodeSsmConfig.name }
		assertThat(methodHint.parameterTypes)
			.containsExactly(TypeReference.of(SsmTxProperties::class.java))
		assertThat(reflection.getTypeHint(TypeReference.of(SsmTxProperties::class.java))).isNotNull
		assertThat(reflection.getTypeHint(TypeReference.of(SsmChaincodeProperties::class.java))).isNotNull
		val signerHint = reflection.getTypeHint(
			TypeReference.of(SsmTxProperties.SignerAgentFileConfig::class.java)
		)
		assertThat(signerHint).isNotNull
		assertThat(signerHint!!.constructors().toList()).isNotEmpty
	}

	private fun stubInitializationCode() = object : BeanFactoryInitializationCode {
		override fun getMethods(): GeneratedMethods = error("not used by the contribution under test")
		override fun getClassName(): ClassName = ClassName.get("ssm.tx.config.spring.autoconfigure", "AotTestCode")
		override fun addInitializer(methodReference: MethodReference) = Unit
	}
}
