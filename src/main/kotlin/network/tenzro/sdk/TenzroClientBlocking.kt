package network.tenzro.sdk

import kotlinx.coroutines.runBlocking
import java.math.BigInteger

/**
 * Blocking facade over [TenzroClient] for plain Java callers (no coroutines needed).
 * Each call blocks the calling thread; do not call from an Android main thread.
 */
class TenzroClientBlocking(
    endpoint: String,
    chainId: Long,
) : AutoCloseable {
    private val client = TenzroClient(endpoint, chainId)

    fun getChainId(): Long = runBlocking { client.getChainId() }
    fun getBlockNumber(): Long = runBlocking { client.getBlockNumber() }
    fun getBalance(address: String): BigInteger = runBlocking { client.getBalance(address) }
    fun getNonce(address: String): Long = runBlocking { client.getNonce(address) }
    fun getGasPrice(): BigInteger = runBlocking { client.getGasPrice() }
    fun peerCount(): Long = runBlocking { client.peerCount() }
    fun getStatus(): NodeStatus = runBlocking { client.getStatus() }
    fun isConnected(): Boolean = runBlocking { client.isConnected() }
    fun verifyChainId() = runBlocking { client.verifyChainId() }

    override fun close() = client.close()
}
