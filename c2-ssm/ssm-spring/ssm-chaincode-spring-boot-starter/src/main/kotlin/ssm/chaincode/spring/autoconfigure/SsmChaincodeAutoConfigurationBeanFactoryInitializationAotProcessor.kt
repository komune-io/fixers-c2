package ssm.chaincode.spring.autoconfigure

import org.springframework.aot.generate.GenerationContext
import org.springframework.aot.hint.ExecutableMode
import org.springframework.aot.hint.RuntimeHints
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor
import org.springframework.beans.factory.aot.BeanFactoryInitializationCode
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.util.ReflectionUtils
import ssm.chaincode.dsl.config.SsmChaincodeProperties

class SsmChaincodeAutoConfigurationBeanFactoryInitializationAotProcessor : BeanFactoryInitializationAotProcessor {
    override fun processAheadOfTime(bf: ConfigurableListableBeanFactory): BeanFactoryInitializationAotContribution {
        return BeanFactoryInitializationAotContribution {
                ctx: GenerationContext, _: BeanFactoryInitializationCode? ->
            // The @Bean method's parameter is SsmChaincodeConfiguration (not SsmChaincodeProperties,
            // which is its return type) — findMethod matches on parameter types.
            registerConfigMethodHints(
                hints = ctx.runtimeHints,
                configClass = SsmChaincodeAutoConfiguration::class.java,
                methodName = SsmChaincodeAutoConfiguration::ssmChaincodeProperties.name,
                propsClass = SsmChaincodeConfiguration::class.java,
                extraTypes = listOf(SsmChaincodeProperties::class.java),
            )
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
