package network.tenzro.sdk

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/** Raised for transport failures, JSON-RPC error objects and malformed responses. */
class TenzroException(message: String, val code: Int? = null, cause: Throwable? = null) :
    RuntimeException(message, cause)

@Serializable
internal data class RpcError(val code: Int = 0, val message: String = "", val data: JsonElement? = null)

@Serializable
internal data class RpcResponse(
    val jsonrpc: String? = null,
    val id: JsonElement? = null,
    val result: JsonElement? = null,
    val error: RpcError? = null,
)

/**
 * Subset of the node's `tenzro_getNodeStatus` report. Unknown fields are ignored,
 * so newer nodes do not break this client.
 */
@Serializable
data class NodeStatus(
    val state: String,
    val roles: List<String> = emptyList(),
    @SerialName("health_status") val healthStatus: String? = null,
    @SerialName("uptime_secs") val uptimeSecs: Long = 0,
    @SerialName("block_height") val blockHeight: Long = 0,
    @SerialName("peer_count") val peerCount: Long = 0,
)
