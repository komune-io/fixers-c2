package ssm.couchdb.client.test

import java.net.InetSocketAddress
import java.net.Socket
import org.junit.jupiter.api.Assumptions

object CouchDbSandbox {

	private fun isReachable(): Boolean =
		runCatching {
			Socket().use { it.connect(InetSocketAddress("localhost", 5984), 1000) }
		}.isSuccess

	fun assumeAvailable() {
		Assumptions.assumeTrue(
			isReachable(),
			"CouchDB sandbox is not reachable on localhost:5984, skipping integration test"
		)
	}
}
