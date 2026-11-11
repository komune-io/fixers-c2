package ssm.data.spring.autoconfigure

import org.springframework.aot.generate.GenerationContext
import org.springframework.aot.hint.ExecutableMode
import org.springframework.aot.hint.RuntimeHints
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor
import org.springframework.beans.factory.aot.BeanFactoryInitializationCode
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.util.ReflectionUtils
import ssm.chaincode.dsl.config.SsmChaincodeProperties
import ssm.couchdb.dsl.config.SsmCouchdbConfig

class SsmDataAutoConfigurationBeanFactoryInitializationAotProcessor : BeanFactoryInitializationAotProcessor {
    override fun processAheadOfTime(bf: ConfigurableListableBeanFactory): BeanFactoryInitializationAotContribution {
        return BeanFactoryInitializationAotContribution {
                ctx: GenerationContext, _: BeanFactoryInitializationCode? ->
            val hints = ctx.runtimeHints
            registerConfigMethodHints(
                hints = hints,
                configClass = DataSsmAutoConfiguration::class.java,
                methodName = DataSsmAutoConfiguration::dataCouchdbSsmConfig.name,
                propsClass = SsmDataProperties::class.java,
            )
            registerConfigMethodHints(
                hints = hints,
                configClass = DataSsmAutoConfiguration::class.java,
                methodName = DataSsmAutoConfiguration::ssmChaincodeConfig.name,
                propsClass = SsmDataProperties::class.java,
                extraTypes = listOf(SsmCouchdbConfig::class.java, SsmChaincodeProperties::class.java),
            )
            listOf(
                SsmCouchdbConfig::class.java,
                SsmDataProperties::class.java,
                SsmChaincodeProperties::class.java,
            ).forEach { type ->
                type.declaredConstructors.forEach {
                    hints.reflection().registerConstructor(it, ExecutableMode.INVOKE)
                }
            }

            hints.resources().registerPattern("cloudant-parent.properties")
        }
    }
}

// Duplicated in each ssm-spring starter: they share no Spring-aware module, and one helper is no reason to add one.
private fun registerConfigMethodHints(
    hints: RuntimeHints,
    configClass: Class<*>,
    methodName: String,
    propsClass: Class<*>,
    extraTypes: List<Class<*>> = emptyList(),
) {
    val method = checkNotNull(ReflectionUtils.findMethod(configClass, methodName, propsClass)) {
        "No @Bean method $methodName(${propsClass.simpleName}) on ${configClass.name}"
    }
    hints.reflection().registerMethod(method, ExecutableMode.INVOKE)
    hints.reflection().registerType(propsClass)
    extraTypes.forEach { hints.reflection().registerType(it) }
}
