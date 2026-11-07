package network.tenzro.sdk

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.put
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.math.BigInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Minimal async JSON-RPC client for a Tenzro node. Only methods listed in the node's
 * `OPEN_METHODS` (no-auth reads) are exposed as typed calls; use [rpc] for anything else.
 *
 * Kotlin callers use the `suspend` functions. Java callers should use [TenzroClientBlocking].
 *
 * @param endpoint node JSON-RPC URL, e.g. `http://127.0.0.1:28545`
 * @param chainId the chain id you expect; see [verifyChainId]
 */
class TenzroClient @JvmOverloads constructor(
    val endpoint: String,
    val chainId: Long,
    private val http: OkHttpClient = OkHttpClient(),
) : AutoCloseable {

    private val json = Json { ignoreUnknownKeys = true }
    private val ids = AtomicLong(1)
    private val jsonType = "application/json".toMediaType()

    /** Raw JSON-RPC call. Returns the `result` element or throws [TenzroException]. */
    suspend fun rpc(method: String, params: List<String> = emptyList()): JsonElement {
        val body = buildJsonObject {
            put("jsonrpc", "2.0")
            put("id", ids.getAndIncrement())
            put("method", method)
            put("params", JsonArray(params.map { JsonPrimitive(it) }))
        }.toString()
        val request = Request.Builder().url(endpoint).post(body.toRequestBody(jsonType)).build()
        val text = http.newCall(request).await()
        val resp = try {
            json.decodeFromString(RpcResponse.serializer(), text)
        } catch (e: Exception) {
            throw TenzroException("Malformed JSON-RPC response from $method", cause = e)
        }
        resp.error?.let { throw TenzroException("$method failed: ${it.message}", it.code) }
        return resp.result ?: throw TenzroException("$method returned no result")
    }

    private suspend fun hex(method: String, params: List<String> = emptyList()): BigInteger {
        val s = (rpc(method, params) as? JsonPrimitive)?.contentOrNull
            ?: throw TenzroException("$method: expected a hex string result")
        return parseHex(s)
    }

    /** `eth_chainId` */
    suspend fun getChainId(): Long = hex("eth_chainId").toLong()

    /** `tenzro_blockNumber` */
    suspend fun getBlockNumber(): Long = hex("tenzro_blockNumber").toLong()

    /** `tenzro_getBalance`, in the chain's smallest unit. */
    suspend fun getBalance(address: String): BigInteger = hex("tenzro_getBalance", listOf(checkAddress(address)))

    /** `tenzro_getNonce` */
    suspend fun getNonce(address: String): Long = hex("tenzro_getNonce", listOf(checkAddress(address))).toLong()

    /** `eth_gasPrice`, in wei. */
    suspend fun getGasPrice(): BigInteger = hex("eth_gasPrice")

    /** `tenzro_peerCount` */
    suspend fun peerCount(): Long = hex("tenzro_peerCount").toLong()

    /** `tenzro_getNodeStatus` */
    suspend fun getStatus(): NodeStatus =
        try { json.decodeFromJsonElement(NodeStatus.serializer(), rpc("tenzro_getNodeStatus")) }
        catch (e: kotlinx.serialization.SerializationException) {
            throw TenzroException("tenzro_getNodeStatus: unexpected shape", cause = e)
        }

    /** True when `eth_chainId` answers. Never throws. */
    suspend fun isConnected(): Boolean = try { getChainId(); true } catch (e: Exception) { false }

    /** Throws [TenzroException] if the node's chain id differs from the one this client was built with. */
    suspend fun verifyChainId() {
        val actual = getChainId()
        if (actual != chainId) throw TenzroException("Chain id mismatch: expected $chainId, node reports $actual")
    }

    override fun close() {
        http.dispatcher.executorService.shutdown()
        http.connectionPool.evictAll()
    }

    internal companion object {
        private val ADDR = Regex("^0x[0-9a-fA-F]{40}$")
        fun checkAddress(a: String): String {
            require(ADDR.matches(a)) { "Expected a 0x-prefixed 20-byte hex address, got: $a" }
            return a
        }
        fun parseHex(s: String): BigInteger {
            val digits = s.removePrefix("0x")
            if (digits.isEmpty()) throw TenzroException("Empty hex quantity")
            return try { BigInteger(digits, 16) } catch (e: NumberFormatException) {
                throw TenzroException("Invalid hex quantity: $s", cause = e)
            }
        }
    }
}

private suspend fun Call.await(): String = suspendCancellableCoroutine { cont ->
    cont.invokeOnCancellation { cancel() }
    enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            cont.resumeWithException(TenzroException("Transport error: ${e.message}", cause = e))
        }
        override fun onResponse(call: Call, response: Response) {
            response.use {
                val text = it.body?.string().orEmpty()
                if (!it.isSuccessful) {
                    cont.resumeWithException(TenzroException("HTTP ${it.code}: ${text.take(200)}", it.code))
                } else cont.resume(text)
            }
        }
    })
}
