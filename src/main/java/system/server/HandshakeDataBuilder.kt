package system.server

import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.NetworkInterface
import java.util.ArrayList

/**
 * Generate the payload used in the QR code to establish a connection from a mobile device.
 */
class HandshakeDataBuilder(val keyGenerator: KeyGenerator) {
    var serverPort : Int? = null

    fun getAllIpAddresses() : List<String> {
        val ips = mutableListOf<String>()

        // Cycle through all interfaces
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val networkInterface = interfaces.nextElement() as NetworkInterface

            if (networkInterface.isLoopback || !networkInterface.isUp) {
                continue // Don't want to broadcast to the loopback interface
            }

            for (interfaceAddress in networkInterface.interfaceAddresses) {
                val address = interfaceAddress.address ?: continue
                if (address is Inet4Address) {
                    ips.add("${address.hostAddress}/${interfaceAddress.networkPrefixLength}")
                }
            }
        }

        return ips
    }

    fun getHandshakePayload() : String? {
        val ipAddressList = getAllIpAddresses()

        // Make sure the pc has at least one active ip address
        if (ipAddressList.isEmpty()) {
            return null
        }

        val ipAddresses = ipAddressList.joinToString(":")
        return "DOKEY;$ipAddresses;${keyGenerator.keyToString};$serverPort"
    }
}