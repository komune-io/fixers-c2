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
                ctx.runtimeHints,
                SsmChaincodeAutoConfiguration::class.java,
                SsmChaincodeAutoConfiguration::ssmChaincodeProperties.name,
                SsmChaincodeConfiguration::class.java,
                SsmChaincodeProperties::class.java,
            )
        }
    }
}

/**
 * Registers the reflection hints needed to invoke a config `@Bean` method ahead of time:
 * the method itself ([ExecutableMode.INVOKE]) plus its properties class and any extra types.
 *
 * Deliberately duplicated in each ssm-spring starter: the starters share no Spring-aware
 * module in this repo, and a dedicated module for one helper would add a dependency edge.
 */
private fun registerConfigMethodHints(
    hints: RuntimeHints,
    configClass: Class<*>,
    methodName: String,
    propsClass: Class<*>,
    vararg extraTypes: Class<*>,
) {
    val method = ReflectionUtils.findMethod(configClass, methodName, propsClass)!!
    hints.reflection().registerMethod(method, ExecutableMode.INVOKE)
    hints.reflection().registerType(propsClass)
    extraTypes.forEach { hints.reflection().registerType(it) }
}
