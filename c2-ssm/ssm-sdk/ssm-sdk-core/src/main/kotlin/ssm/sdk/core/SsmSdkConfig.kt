package ssm.sdk.core

import java.io.FileInputStream
import java.io.IOException
import java.util.Properties
import ssm.sdk.sign.FileUtils

class SsmSdkConfig(
	val baseUrl: String,
	val cloudEventsSource: String = DEFAULT_CLOUDEVENTS_SOURCE,
) {

	companion object {
		private const val SSM_REST_URL = "ssm.rest.url"
		private const val SSM_CLOUDEVENTS_SOURCE = "ssm.cloudevents.source"
		const val DEFAULT_CLOUDEVENTS_SOURCE = "/io.komune.c2/sdk"

		@Throws(IOException::class)
		fun fromConfigFile(filename: String): SsmSdkConfig {
			val file = FileUtils.getUrl(filename)
			val props = Properties()
			props.load(FileInputStream(file.file))
			val baseUrl = props.getProperty(SSM_REST_URL)
			val cloudEventsSource = props.getProperty(SSM_CLOUDEVENTS_SOURCE) ?: DEFAULT_CLOUDEVENTS_SOURCE
			return SsmSdkConfig(baseUrl, cloudEventsSource)
		}
	}
}
