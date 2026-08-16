package ssm.couchdb.spring.autoconfigure

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
import ssm.couchdb.dsl.config.SsmCouchdbConfig

class SsmCouchdbAotProcessorTest {

	@Test
	fun `aot contribution registers the couchdbConfig bean method, its types and constructors`() {
		val generationContext = DefaultGenerationContext(
			ClassNameGenerator(ClassName.get("ssm.couchdb.spring.autoconfigure", "AotTest")),
			InMemoryGeneratedFiles(),
		)

		SsmCouchdbAutoConfigurationBeanFactoryInitializationAotProcessor()
			.processAheadOfTime(DefaultListableBeanFactory())
			.applyTo(generationContext, stubInitializationCode())

		val reflection = generationContext.runtimeHints.reflection()
		val configHint = reflection.getTypeHint(TypeReference.of(SsmCouchdbAutoConfiguration::class.java))
		assertThat(configHint).isNotNull
		assertThat(configHint!!.methods().map { it.name }.toList())
			.contains(SsmCouchdbAutoConfiguration::couchdbConfig.name)
		assertThat(reflection.getTypeHint(TypeReference.of(SsmCouchdbProperties::class.java))).isNotNull
		val couchdbConfigHint = reflection.getTypeHint(TypeReference.of(SsmCouchdbConfig::class.java))
		assertThat(couchdbConfigHint).isNotNull
		assertThat(couchdbConfigHint!!.constructors().toList()).isNotEmpty
	}

	private fun stubInitializationCode() = object : BeanFactoryInitializationCode {
		override fun getMethods(): GeneratedMethods = error("not used by the contribution under test")
		override fun getClassName(): ClassName = ClassName.get("ssm.couchdb.spring.autoconfigure", "AotTestCode")
		override fun addInitializer(methodReference: MethodReference) = Unit
	}
}
