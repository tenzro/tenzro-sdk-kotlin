package network.tenzro.sdk

import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import java.math.BigInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MockTest {
    private fun withServer(body: String, code: Int = 200, f: suspend (TenzroClient, MockWebServer) -> Unit) {
        MockWebServer().use { s ->
            s.enqueue(MockResponse().setResponseCode(code).setBody(body))
            s.start()
            TenzroClient(s.url("/").toString(), 13380).use { c -> runBlocking { f(c, s) } }
        }
    }

    @Test fun parsesChainId() = withServer("""{"jsonrpc":"2.0","id":1,"result":"0x3444"}""") { c, s ->
        assertEquals(13380L, c.getChainId())
        val req = s.takeRequest()
        assertTrue(req.body.readUtf8().contains("\"eth_chainId\""))
    }

    @Test fun parsesBigBalance() =
        withServer("""{"jsonrpc":"2.0","id":1,"result":"0xde0b6b3a7640000"}""") { c, _ ->
            assertEquals(BigInteger("1000000000000000000"), c.getBalance("0x" + "00".repeat(19) + "01"))
        }

    @Test fun surfacesRpcError() =
        withServer("""{"jsonrpc":"2.0","id":1,"error":{"code":-32601,"message":"nope"}}""") { c, _ ->
            val e = assertFailsWith<TenzroException> { c.getBlockNumber() }
            assertEquals(-32601, e.code)
        }

    @Test fun surfacesHttpError() = withServer("denied", 403) { c, _ ->
        assertFailsWith<TenzroException> { c.getChainId() }
    }

    @Test fun chainMismatchDetected() = withServer("""{"jsonrpc":"2.0","id":1,"result":"0x1"}""") { c, _ ->
        assertFailsWith<TenzroException> { c.verifyChainId() }
    }

    @Test fun rejectsBadAddress() = withServer("{}") { c, _ ->
        assertFailsWith<IllegalArgumentException> { c.getBalance("0x123") }
    }

    @Test fun isConnectedFalseOnGarbage() = withServer("not json") { c, _ ->
        assertEquals(false, c.isConnected())
    }
}
