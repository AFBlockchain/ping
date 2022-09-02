# Ping - Pong

A minimal CorDapp to test node's Corda Network connectivity. This CorDapp defines a `PingState` which records `ping` from a `pinger` and `ponger` from a `ponger`. The state must be `Ping`ed first, which then can be `Pong`ed. The `Pong` transaction requires the notary signature.

> **_NOTE:_**  The full testing requires two nodes having this CorDapp installed.

## Quick Start

Say there are two parties on the network (and some notary service).

Ping from PartyA to PartyB:
```shell
# In PartyA shell
flow start Ping ping: "Hello PartyB", to: PartyA
```
The flow returns an identifier of the bootstrapped `PingState`, for example `43251254-66c4-45ee-9e0f-360a8f7df078`.

Pong from PartyB to PartyA
```shell
# In PartyB shell
flow start Pong pong: "Hi! How are u doing, PartyA?", uuid: 43251254-66c4-45ee-9e0f-360a8f7df078
```

If both flow succeeded, the network connectivity should be good.

## CorDapp Signing

This CorDapp configured gradle for signing CorDapp jars for production deployment. To sign, configure [signing.properties](./signing.properties) to point gradle to the correct configuration. The `gradlew jar` command will automatically sign the contract jar. To verify, run:

```shell
jarsigner -verify -verbose -keystore <path-to-keystore> contracts/build/libs/contracts-0.1.jar
```
