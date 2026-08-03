package ssm.data.spring.autoconfigure

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.aot.generate.ClassNameGenerator
import org.springframework.aot.generate.DefaultGenerationContext
import org.springframework.aot.generate.InMemoryGeneratedFiles
import org.springframework.aot.hint.TypeReference
import org.springframework.aot.generate.GeneratedMethods
import org.springframework.aot.generate.MethodReference
import org.springframework.beans.factory.aot.BeanFactoryInitializationCode
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.javapoet.ClassName
import ssm.chaincode.dsl.config.SsmChaincodeProperties
import ssm.couchdb.dsl.config.SsmCouchdbConfig

class SsmDataAotProcessorTest {

	@Test
	fun `registers reflection and resource hints for native image`() {
		val processor = SsmDataAutoConfigurationBeanFactoryInitializationAotProcessor()
		val contribution = processor.processAheadOfTime(DefaultListableBeanFactory())
		val generationContext = DefaultGenerationContext(
			ClassNameGenerator(ClassName.get("ssm.data.spring.autoconfigure", "AotTestTarget")),
			InMemoryGeneratedFiles(),
		)

		contribution.applyTo(generationContext, stubInitializationCode())

		val reflection = generationContext.runtimeHints.reflection()
		assertThat(reflection.getTypeHint(TypeReference.of(SsmDataProperties::class.java))).isNotNull
		assertThat(reflection.getTypeHint(TypeReference.of(SsmCouchdbConfig::class.java))).isNotNull
		assertThat(reflection.getTypeHint(TypeReference.of(SsmChaincodeProperties::class.java))).isNotNull
		assertThat(reflection.getTypeHint(TypeReference.of(DataSsmAutoConfiguration::class.java))).isNotNull
		val resourcePatterns = generationContext.runtimeHints.resources().resourcePatternHints().toList()
			.flatMap { hint -> hint.includes.map { it.pattern } }
		assertThat(resourcePatterns).contains("cloudant-parent.properties")
	}

	private fun stubInitializationCode(): BeanFactoryInitializationCode = object : BeanFactoryInitializationCode {
		override fun getMethods(): GeneratedMethods = throw UnsupportedOperationException()
		override fun getClassName(): ClassName = ClassName.get("aot", "Stub")
		override fun addInitializer(reference: MethodReference) = Unit
	}
}
