package ssm.chaincode.spring.autoconfigure

import java.lang.reflect.Method
import org.springframework.aot.generate.GenerationContext
import org.springframework.aot.hint.ExecutableMode
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor
import org.springframework.beans.factory.aot.BeanFactoryInitializationCode
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.util.ReflectionUtils
import ssm.chaincode.dsl.config.SsmChaincodeProperties

class SsmChaincodeAutoConfigurationBeanFactoryInitializationAotProcessor : BeanFactoryInitializationAotProcessor {
    override fun processAheadOfTime(bf: ConfigurableListableBeanFactory): BeanFactoryInitializationAotContribution {
        return  BeanFactoryInitializationAotContribution {
                ctx: GenerationContext, _: BeanFactoryInitializationCode? ->
            val hints = ctx.runtimeHints
            val method: Method = ReflectionUtils.findMethod(
                SsmChaincodeAutoConfiguration::class.java,
                SsmChaincodeAutoConfiguration::ssmChaincodeProperties.name,
                SsmChaincodeProperties::class.java
            )!!
            hints.reflection().registerMethod(method, ExecutableMode.INVOKE)

            hints.reflection().registerType(SsmChaincodeProperties::class.java)
        }
    }
}
