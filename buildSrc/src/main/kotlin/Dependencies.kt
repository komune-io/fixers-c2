object PluginVersions {
	const val kotlin = "1.5.30"
	const val fixers = "experimental-SNAPSHOT"

	const val springBoot = "2.5.3"

	const val npmPublish = "1.0.4"
}

object Versions {
	const val springBoot = PluginVersions.springBoot
	const val springFramework = "5.3.4"

	const val cloudant = "0.0.24"

	const val bouncycastleVersion = "1.61"
	const val okhttpVersion = "3.14.0"
	const val retrofitVersion = "2.5.0"
	const val jacksonKotlin = "2.10.2"

	const val junit = "5.7.0"
	const val junitPlateform = "1.8.1"
	const val assertj = "3.15.0"

	const val slf4j = "1.7.30"


	const val coroutines = "1.5.1"

	const val f2 = "experimental-SNAPSHOT"
	const val d2 = "0.1.1-SNAPSHOT"


	const val cucumber = "6.11.0"
}

object Dependencies {
	object Jvm {
		val junit = arrayOf(
			"org.junit.jupiter:junit-jupiter:${Versions.junit}",
			"org.junit.jupiter:junit-jupiter-api:${Versions.junit}",
			"org.junit.platform:junit-platform-suite:${Versions.junitPlateform}",
			"org.assertj:assertj-core:${Versions.assertj}"
		)
	}

}
