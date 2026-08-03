package ssm.couchdb.spring.autoconfigure

import java.lang.reflect.Method
import org.springframework.aot.generate.GenerationContext
import org.springframework.aot.hint.ExecutableMode
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor
import org.springframework.beans.factory.aot.BeanFactoryInitializationCode
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.util.ReflectionUtils
import ssm.couchdb.dsl.config.SsmCouchdbConfig

class SsmCouchdbAutoConfigurationBeanFactoryInitializationAotProcessor : BeanFactoryInitializationAotProcessor {
    override fun processAheadOfTime(bf: ConfigurableListableBeanFactory): BeanFactoryInitializationAotContribution {
        return  BeanFactoryInitializationAotContribution {
                ctx: GenerationContext, _: BeanFactoryInitializationCode? ->
            val hints = ctx.runtimeHints
            val method: Method = ReflectionUtils.findMethod(
                SsmCouchdbAutoConfiguration::class.java,
                SsmCouchdbAutoConfiguration::couchdbConfig.name,
                SsmCouchdbProperties::class.java
            )!!
            hints.reflection().registerMethod(method, ExecutableMode.INVOKE)

            hints.reflection().registerType(SsmCouchdbProperties::class.java)
            hints.reflection().registerType(SsmCouchdbConfig::class.java)

            SsmCouchdbConfig::class.java.declaredConstructors.forEach {
                hints.reflection().registerConstructor(it, ExecutableMode.INVOKE)
            }
        }
    }
}
