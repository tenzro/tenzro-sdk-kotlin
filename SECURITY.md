# Security policy

## Reporting a vulnerability

Please report security issues privately. Do not open a public issue, pull
request or discussion for an unpatched vulnerability.

- Email **eng@tenzro.com**, or
- use GitHub private vulnerability reporting ("Report a vulnerability" under
  the Security tab) on this repository.

Include what you found, how to reproduce it, the version or commit, and the
impact you expect. Please do not test against other people's accounts or nodes,
and do not disclose the issue publicly until a fix is available.

## Scope

This repository: the Kotlin/JVM client. It currently performs unauthenticated
reads only and holds no keys. The `Signer` interface has no implementation here.

## Supported versions

Security fixes go into the latest release only (pre-1.0).
