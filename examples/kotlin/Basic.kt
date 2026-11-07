import kotlinx.coroutines.runBlocking
import network.tenzro.sdk.TenzroClient

fun main() = runBlocking {
    TenzroClient("http://127.0.0.1:28545", 13380).use { c ->
        c.verifyChainId()
        println("block ${c.getBlockNumber()}, status ${c.getStatus().state}")
        println("balance ${c.getBalance("0x0000000000000000000000000000000000000001")}")
    }
}
