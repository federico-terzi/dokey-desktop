package tests

import system.server.HandshakeDataBuilder

fun main(args: Array<String>) {
    val handshakeDataBuilder = HandshakeDataBuilder()
    println(handshakeDataBuilder.getAllIpAddresses())
}