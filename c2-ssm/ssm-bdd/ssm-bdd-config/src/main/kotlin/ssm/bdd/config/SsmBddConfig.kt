package ssm.bdd.config

import ssm.chaincode.dsl.config.SsmBatchProperties
import ssm.chaincode.dsl.config.SsmChaincodeProperties
import ssm.chaincode.dsl.model.uri.ChaincodeUri
import ssm.couchdb.dsl.config.SsmCouchdbConfig
import ssm.data.dsl.config.DataSsmConfig

object SsmBddConfig {

	private const val SEPARATOR_SLASH = "//////////////////////////////////////////////"
	private const val SEPARATOR_STAR = "**********************************************"

	object Commune {
		object Chaincode {
			const val url = "http://peer0.pr-commune.Komune.io"
			const val chaincodeUri = "chaincode:sandbox:ssm"
		}
		object Couchdb {
			const val url = "http://peer0.pr-commune.Komune.io:5984"
			const val username = "COMMUNE_COUCHDB_USERNAME"
			const val password = "COMMUNE_COUCHDB_PASSWORD"
		}
		const val ssmAgent = "COMMUNE_SSM_AGENT"
		const val ssmAgentKey = "COMMUNE_SSM_AGENT_KEY"
	}

	object Local {
		object Chaincode {
			const val url = "http://localhost:9090"
			const val chaincodeUri = "chaincode:sandbox:ssm"
		}
		object Couchdb {
			const val url = "http://localhost:5984"
			const val username = "couchdb"
			const val password = "couchdb"
		}

	}

	fun String.orIfGitlab(value: String): String {
		return if (System.getenv("SPRING_PROFILES_ACTIVE") == "gitlab") {
			println(SEPARATOR_SLASH)
			println(SEPARATOR_SLASH)
			println(SEPARATOR_SLASH)
			value
		} else {
			println(SEPARATOR_STAR)
			println(SEPARATOR_STAR)
			println(SEPARATOR_STAR)
			this
		}
	}

	fun String.orIfGitlabEnv(value: String): String {
		return if (getEnv("SPRING_PROFILES_ACTIVE") == "gitlab") {
			println(SEPARATOR_SLASH)
			println(SEPARATOR_SLASH)
			println(SEPARATOR_SLASH)
			getEnv(value)
		} else {
			this
		}
	}

	object Chaincode {
		val url: String
			get() {
				return Local.Chaincode.url
					.orIfGitlab(Commune.Chaincode.url)
			}
		val chaincodeUri: ChaincodeUri
			get() {
				return ChaincodeUri(
					Local.Chaincode.chaincodeUri
						.orIfGitlab(Commune.Chaincode.chaincodeUri)
				)
			}
		val config: SsmChaincodeProperties
			get() {
				return SsmChaincodeProperties(url = url)
			}
	}

	object Data {
		val config = DataSsmConfig(
			couchdb = Couchdb.config,
			chaincode = Chaincode.config,
			batch = SsmBatchProperties()
		)
	}

	object Couchdb {
		val url: String
			get() {
				return Local.Couchdb.url
					.orIfGitlab(Commune.Couchdb.url)
			}
		val username: String
			get() {
				return Local.Couchdb.username.orIfGitlab(
					getEnv(Commune.Couchdb.username)
				)
			}
		val password: String
			get() {
				return Local.Couchdb.password
					.orIfGitlabEnv(Commune.Couchdb.password)
			}

		val config: SsmCouchdbConfig
			get() {
				return SsmCouchdbConfig(
					url = url,
					username = username,
					password = password,
					serviceName = "ssm-couchdb-test"
				)
			}
	}

	private fun getEnv(value: String): String {
		val envValue = System.getenv(value)
		return if (envValue != null) {
			envValue
		} else {
			println("Env parameter[$value] is null")
			""
		}
	}

	object Key {
		val admin: Pair<String, String>
			get() {
				return "ssm-admin".orIfGitlabEnv(Commune.ssmAgent) to "local/admin/ssm-admin".orIfGitlabEnv(Commune.ssmAgentKey)
			}
		val user: Pair<String, String>
			get() {
				return "ssm-admin".orIfGitlabEnv(Commune.ssmAgent) to "local/admin/ssm-admin".orIfGitlabEnv(Commune.ssmAgentKey)
			}
	}
}
