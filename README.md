# tenzro-sdk-kotlin

The official Tenzro SDK for Kotlin and Java: a coroutine-based JSON-RPC client for the [Tenzro Network](https://tenzro.com) (chain id 13380, `0x3444`), usable from Java through a blocking facade.

## Install

Not yet on Maven Central — install via JitPack or a git dependency until it is:

```kotlin
repositories { maven("https://jitpack.io") }
dependencies { implementation("com.github.tenzro:tenzro-sdk-kotlin:main-SNAPSHOT") }
```
Pin a commit hash or tag instead of `main-SNAPSHOT` for reproducible builds. `./gradlew publishToMavenLocal` plus `mavenLocal()` with `network.tenzro:tenzro-sdk-kotlin:0.1.0` also works.

## Quickstart (Kotlin)

```kotlin
TenzroClient("http://127.0.0.1:28545", chainId = 13380).use { c ->
    c.verifyChainId()
    println(c.getBlockNumber()); println(c.getBalance("0x...")); println(c.getStatus())
}
```
See `examples/kotlin/Basic.kt`.

## Quickstart (Java)

```java
TenzroClientBlocking c = new TenzroClientBlocking("http://127.0.0.1:28545", 13380);
System.out.println(c.getStatus());
```
`TenzroClientBlocking` runs each call with `runBlocking` rather than `@JvmStatic` wrappers, since suspend functions aren't callable from Java directly — don't call it on an Android main thread. See `examples/java/BasicJava.java`.

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

Signing is deliberately external: `Signer` is an interface only (naming Android Keystore/StrongBox and Credential Manager passkeys in its docs), with no implementation that holds a raw key.

## Android, JVM, and Kotlin Multiplatform

This ships as a plain JVM module, which also runs on Android (OkHttp, kotlinx.serialization and coroutines all support it; the only non-Android-trivial type is `BigInteger`). A move to Kotlin Multiplatform makes sense once iOS/JS/native targets are wanted — it would swap OkHttp for ktor-client and `BigInteger` for a multiplatform bignum — and is a small migration given the client is only HTTP + JSON, but isn't needed yet.

## Compatibility

JUnit 5 tests pass against a live Tenzro node (chain id, status, balance/nonce/gas-price reads, typed errors, the Java facade). Android itself hasn't been exercised on a device; the signer implementation and a Compose sample both need one to build correctly. Not yet on Maven Central.

## Links

[tenzro-network](https://github.com/tenzro/tenzro-network) (the protocol and node) and the other official SDKs in the [tenzro](https://github.com/tenzro) organization (TypeScript, Rust, Python, Go, C/C++, Swift, .NET).

License: Apache-2.0.
