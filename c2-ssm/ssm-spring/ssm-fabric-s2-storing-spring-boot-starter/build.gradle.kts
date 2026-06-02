plugins {
	alias(catalogue.plugins.fixers.gradle.kotlin.jvm)
	alias(catalogue.plugins.fixers.gradle.publish)
	alias(catalogue.plugins.kotlin.spring)
	alias(catalogue.plugins.kotlin.kapt)
}

dependencies {
	// The persister + S2 adapter + existing F2 function impls live here (transitively).
	// We provide alternative SsmTxService / SsmQueryService beans backed by Fabric;
	// the F2 impls from sibling tx starters reuse them via @ConditionalOnMissingBean.
	api(project(":c2-ssm:ssm-spring:ssm-s2-storing-spring-boot-starter"))

	// In-process Fabric runtime: FabricGatewayClient + TxOutcome.
	api(project(":c2-chaincode:chaincode-api:chaincode-api-fabric"))

	// @ConfigurationProperties("coop") + FabricConfigLoader autoconfig.
	api(project(":c2-chaincode:chaincode-api:chaincode-api-config"))

	implementation(libs.spring.boot.autoconfigure)
	kapt(libs.spring.boot.configuration.processor)

	testImplementation(libs.bundles.test)
	testImplementation("io.mockk:mockk:1.13.13")
}
