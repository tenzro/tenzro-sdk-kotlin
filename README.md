# tenzro-sdk-kotlin

Minimal Kotlin/JVM client for a [Tenzro](https://tenzro.com) node's JSON-RPC. Coroutines (`suspend`), kotlinx.serialization, OkHttp. Usable from Java through a blocking facade.

Status: 0.1.0, read-only. Not published to Maven Central.

## Install (JitPack / git, until Maven Central publishing)

```kotlin
repositories { maven("https://jitpack.io") }
dependencies { implementation("com.github.tenzro:tenzro-sdk-kotlin:main-SNAPSHOT") }
```
Pin a commit hash or tag instead of `main-SNAPSHOT` for reproducible builds. (JitPack resolution has not been exercised yet; the library builds and `publishToMavenLocal` is configured, so `./gradlew publishToMavenLocal` then `mavenLocal()` with `network.tenzro:tenzro-sdk-kotlin:0.1.0` also works.)

## Kotlin

```kotlin
TenzroClient("http://127.0.0.1:28545", chainId = 13380).use { c ->
    c.verifyChainId()
    println(c.getBlockNumber()); println(c.getBalance("0x...")); println(c.getStatus())
}
```
See `examples/kotlin/Basic.kt`.

## Java

Choice: a separate blocking facade, `TenzroClientBlocking` (runs each call with `runBlocking`), rather than `@JvmStatic` wrappers, because suspend functions are not callable from Java and the facade keeps one obvious entry point. Do not call it on an Android main thread. See `examples/java/BasicJava.java`.

## Methods

| Kotlin | RPC |
|---|---|
| `getChainId` | `eth_chainId` |
| `getBlockNumber` | `tenzro_blockNumber` |
| `getBalance(address)` | `tenzro_getBalance` |
| `getNonce(address)` | `tenzro_getNonce` |
| `getGasPrice` | `eth_gasPrice` |
| `peerCount` | `tenzro_peerCount` |
| `getStatus` | `tenzro_getNodeStatus` |
| `isConnected`, `verifyChainId` | helpers on `eth_chainId` |
| `rpc(method, params)` | any method, string params only |

All typed methods are in the node's no-auth `OPEN_METHODS`. Naming follows `tenzro-ts-sdk`.

## Signing

`Signer` is an interface only (KDoc mentions Android Keystore/StrongBox and Credential Manager passkeys). There is no implementation: none could be tested without an Android device. No transaction building/signing exists yet.

## Android vs. JVM vs. Kotlin Multiplatform

Recommendation (reasoned, not built): keep this JVM module, which runs on Android too (OkHttp, kotlinx.serialization and coroutines all support Android; the library has no `java.*` APIs beyond `BigInteger`). Android compatibility has not been tested here. Move to KMP only if iOS/JS/native is wanted: OkHttp is JVM/Android-only, so a KMP `commonMain` would swap in ktor-client (OkHttp engine on JVM/Android, Darwin on iOS), and `BigInteger` would need a multiplatform replacement (e.g. ionspin bignum). Since the client is only HTTP + JSON, that migration is small; the multiplatform path was not built or tested.

## Tests

`./gradlew test` always runs MockWebServer tests. Set `TENZRO_RPC_URL` (and optionally `TENZRO_CHAIN_ID`) to also run live-node tests; they are skipped when unset.

## Not included

Compose sample (no Android build environment to verify it), signer implementations, transaction submission, Maven Central publishing.

License: Apache-2.0.
