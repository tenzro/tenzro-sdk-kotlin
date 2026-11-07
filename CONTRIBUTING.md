# Contributing

1. Open an issue first for anything larger than a small fix.
2. JDK 17+. Build and test: `./gradlew build`.
3. Tests that need a live node read `TENZRO_RPC_URL` (and optionally
   `TENZRO_CHAIN_ID`, default 13380) and are skipped when it is unset.
   Offline tests use MockWebServer and always run.
4. Only add typed methods for RPCs that are in the node's open (no-auth) set, and
   add a test against a live node for each. Do not add an implementation of
   `Signer` unless it has been tested on a real device.
5. By contributing you agree your work is licensed under Apache-2.0.
