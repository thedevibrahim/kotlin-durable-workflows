package thedevibrahim.workflows.test.interfaces

import io.kotest.mpp.bestName
import thedevibrahim.workflows.core.interfaces.KeyValueClient

interface TestClient {
    val dbName: String

    val keyValueClient: KeyValueClient

    val testKeyValueClient: TestKeyValueClient

    val name: String
        get() = "${keyValueClient::class.bestName()} [$dbName]"
}
