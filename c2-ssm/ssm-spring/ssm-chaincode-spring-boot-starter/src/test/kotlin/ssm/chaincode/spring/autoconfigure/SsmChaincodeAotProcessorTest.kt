package ssm.chaincode.spring.autoconfigure

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

class SsmChaincodeAotProcessorTest {

	@Test
	fun `aot contribution registers the ssmChaincodeProperties bean method and its types`() {
		val generationContext = DefaultGenerationContext(
			ClassNameGenerator(ClassName.get("ssm.chaincode.spring.autoconfigure", "AotTest")),
			InMemoryGeneratedFiles(),
		)

		SsmChaincodeAutoConfigurationBeanFactoryInitializationAotProcessor()
			.processAheadOfTime(DefaultListableBeanFactory())
			.applyTo(generationContext, stubInitializationCode())

		val reflection = generationContext.runtimeHints.reflection()
		val configHint = reflection.getTypeHint(TypeReference.of(SsmChaincodeAutoConfiguration::class.java))
		assertThat(configHint).isNotNull
		val methodHint = configHint!!.methods().toList()
			.single { it.name == SsmChaincodeAutoConfiguration::ssmChaincodeProperties.name }
		assertThat(methodHint.parameterTypes)
			.containsExactly(TypeReference.of(SsmChaincodeConfiguration::class.java))
		assertThat(reflection.getTypeHint(TypeReference.of(SsmChaincodeConfiguration::class.java))).isNotNull
		assertThat(reflection.getTypeHint(TypeReference.of(SsmChaincodeProperties::class.java))).isNotNull
	}

	private fun stubInitializationCode() = object : BeanFactoryInitializationCode {
		override fun getMethods(): GeneratedMethods = error("not used by the contribution under test")
		override fun getClassName(): ClassName = ClassName.get("ssm.chaincode.spring.autoconfigure", "AotTestCode")
		override fun addInitializer(methodReference: MethodReference) = Unit
	}
}
