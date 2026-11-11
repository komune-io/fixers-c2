package ssm.tx.config.spring.autoconfigure

import org.springframework.aot.generate.GenerationContext
import org.springframework.aot.hint.ExecutableMode
import org.springframework.aot.hint.RuntimeHints
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor
import org.springframework.beans.factory.aot.BeanFactoryInitializationCode
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.util.ReflectionUtils
import ssm.chaincode.dsl.config.SsmChaincodeProperties

class SsmTxAutoConfigurationBeanFactoryInitializationAotProcessor : BeanFactoryInitializationAotProcessor {
    override fun processAheadOfTime(bf: ConfigurableListableBeanFactory): BeanFactoryInitializationAotContribution {
        return BeanFactoryInitializationAotContribution {
                ctx: GenerationContext, _: BeanFactoryInitializationCode? ->
            val hints = ctx.runtimeHints
            registerConfigMethodHints(
                hints = hints,
                configClass = SsmTxAutoConfiguration::class.java,
                methodName = SsmTxAutoConfiguration::chaincodeSsmConfig.name,
                propsClass = SsmTxProperties::class.java,
                extraTypes = listOf(SsmChaincodeProperties::class.java),
            )
            SsmTxProperties.SignerAgentFileConfig::class.java.declaredConstructors.forEach {
                hints.reflection().registerConstructor(it, ExecutableMode.INVOKE)
            }
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
