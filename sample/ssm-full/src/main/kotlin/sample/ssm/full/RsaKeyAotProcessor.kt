package sample.ssm.full

import org.springframework.aot.generate.GenerationContext
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor
import org.springframework.beans.factory.aot.BeanFactoryInitializationCode
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory

class RsaKeyAotProcessor : BeanFactoryInitializationAotProcessor {

    override fun processAheadOfTime(bf: ConfigurableListableBeanFactory): BeanFactoryInitializationAotContribution {
        return  BeanFactoryInitializationAotContribution {
            ctx: GenerationContext, _: BeanFactoryInitializationCode? ->
            val hints = ctx.runtimeHints
            hints.resources().registerPattern("user/adam")
            hints.resources().registerPattern("user/adam.pub")
        }
    }
}
