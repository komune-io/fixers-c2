package s2.spring.core.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import ssm.chaincode.dsl.config.SsmBatchProperties

@Configuration
@ConditionalOnClass(SsmBatchProperties::class)
@EnableConfigurationProperties(S2Properties::class)
open class SsmBatchConfiguration {

	@Bean
	@Order(1)
	@ConditionalOnMissingBean(SsmBatchProperties::class)
	open fun ssmBatchProperties(s2Properties: S2Properties): SsmBatchProperties {
		return s2Properties.batch?.let {
			SsmBatchProperties(
				size = it.size,
				concurrency = it.concurrency
			)
		} ?: SsmBatchProperties()
	}
}
