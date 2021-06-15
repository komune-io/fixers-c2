package ssm.f2.query

import kotlinx.coroutines.future.await
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ssm.dsl.query.SsmGetQueryFunction
import ssm.dsl.query.SsmGetResult
import ssm.f2.commons.ssmF2Function

@Configuration
class SsmGetQueryFunctionImpl {

	@Bean
	fun ssmGetQueryFunction(): SsmGetQueryFunction = ssmF2Function { cmd, ssmClient ->
		val ssm = ssmClient.getSsm(cmd.name).await().orElse( null)
		SsmGetResult(ssm )
	}

}