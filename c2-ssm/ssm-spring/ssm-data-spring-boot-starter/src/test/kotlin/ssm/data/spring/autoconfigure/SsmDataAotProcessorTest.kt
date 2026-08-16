package ssm.data.spring.autoconfigure

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
import ssm.couchdb.dsl.config.SsmCouchdbConfig

class SsmDataAotProcessorTest {

	@Test
	fun `aot contribution registers both config bean methods, their types, constructors and resources`() {
		val generationContext = DefaultGenerationContext(
			ClassNameGenerator(ClassName.get("ssm.data.spring.autoconfigure", "AotTest")),
			InMemoryGeneratedFiles(),
		)

		SsmDataAutoConfigurationBeanFactoryInitializationAotProcessor()
			.processAheadOfTime(DefaultListableBeanFactory())
			.applyTo(generationContext, stubInitializationCode())

		val hints = generationContext.runtimeHints
		val reflection = hints.reflection()
		val configHint = reflection.getTypeHint(TypeReference.of(DataSsmAutoConfiguration::class.java))
		assertThat(configHint).isNotNull
		assertThat(configHint!!.methods().map { it.name }.toList()).contains(
			DataSsmAutoConfiguration::dataCouchdbSsmConfig.name,
			DataSsmAutoConfiguration::ssmChaincodeConfig.name,
		)
		listOf(
			SsmDataProperties::class.java,
			SsmCouchdbConfig::class.java,
			SsmChaincodeProperties::class.java,
		).forEach { type ->
			val typeHint = reflection.getTypeHint(TypeReference.of(type))
			assertThat(typeHint).isNotNull
			assertThat(typeHint!!.constructors().toList()).isNotEmpty
		}
		val patterns = hints.resources().resourcePatternHints()
			.flatMap { it.includes.stream() }
			.map { it.pattern }
			.toList()
		assertThat(patterns).contains("cloudant-parent.properties")
	}

	private fun stubInitializationCode() = object : BeanFactoryInitializationCode {
		override fun getMethods(): GeneratedMethods = error("not used by the contribution under test")
		override fun getClassName(): ClassName = ClassName.get("ssm.data.spring.autoconfigure", "AotTestCode")
		override fun addInitializer(methodReference: MethodReference) = Unit
	}
}
