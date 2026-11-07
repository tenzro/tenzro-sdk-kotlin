package network.tenzro.sdk

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.BeforeEach
import java.math.BigInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Runs against a real node when TENZRO_RPC_URL is set; otherwise every test is skipped. */
class LiveNodeTest {
    private lateinit var client: TenzroClient
    private val zero = "0x0000000000000000000000000000000000000001"

    @BeforeEach fun setUp() {
        val url = System.getenv("TENZRO_RPC_URL")
        assumeTrue(!url.isNullOrBlank(), "TENZRO_RPC_URL not set; skipping live node tests")
        client = TenzroClient(url!!, System.getenv("TENZRO_CHAIN_ID")?.toLong() ?: 13380L)
    }

    @Test fun chainId() = runBlocking {
        assertEquals(client.chainId, client.getChainId())
        client.verifyChainId()
        println("chainId=${client.getChainId()}")
    }

    @Test fun blockNumber() = runBlocking {
        val n = client.getBlockNumber(); println("blockNumber=$n"); assertTrue(n > 0)
    }

    @Test fun balanceNonceGas() = runBlocking {
        val b = client.getBalance(zero); println("balance($zero)=$b")
        assertTrue(b >= BigInteger.ZERO)
        println("nonce=${client.getNonce(zero)} gasPrice=${client.getGasPrice()}")
        assertTrue(client.getGasPrice() > BigInteger.ZERO)
    }

    @Test fun status() = runBlocking {
        val s = client.getStatus(); println("status=$s")
        assertEquals("Running", s.state)
        assertTrue(client.isConnected())
    }

    @Test fun javaFacade() {
        val j = TenzroClientBlocking(System.getenv("TENZRO_RPC_URL")!!, client.chainId)
        j.use { println("blocking chainId=${it.getChainId()}"); assertEquals(client.chainId, it.getChainId()) }
    }
}
