package network.tenzro.sdk

/**
 * Something that can sign on behalf of an account.
 *
 * This is an interface only. This SDK ships NO implementation: reads do not need one, and
 * transaction building/signing is not part of this release.
 *
 * Intended backends (none implemented or tested here):
 *  - Android Keystore, hardware-backed where available, including StrongBox
 *    (`KeyGenParameterSpec.Builder#setIsStrongBoxBacked`). Note Keystore's supported curves are
 *    limited (commonly P-256); whether that matches the signature scheme Tenzro accepts must be
 *    checked before building one.
 *  - Credential Manager passkeys (`androidx.credentials`, WebAuthn assertions). The node has
 *    passkey-assertion ownership proofs, so a passkey backend would return the assertion
 *    data rather than a raw signature; the exact encoding is not modelled here.
 *  - Plain JVM keys (software keystore, HSM via PKCS#11).
 */
interface Signer {
    /** The account address this signer controls, `0x`-prefixed hex. */
    val address: String

    /** Sign [message] and return the scheme-specific signature bytes. May prompt for user presence. */
    suspend fun sign(message: ByteArray): ByteArray
}
