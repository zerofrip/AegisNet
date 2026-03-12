package com.aegisnet.vpn.pipeline

data class PacketInfo(
    val protocol: Int, // 6 for TCP, 17 for UDP
    val sourceIp: String,
    val sourcePort: Int,
    val destIp: String,
    val destPort: Int,
    val payloadSize: Long,
    val rawPayload: ByteArray? = null
)

interface PacketParser {
    /**
     * Parses a raw IP packet (IPv4 or IPv6) and extracts routing info.
     */
    fun parse(packet: ByteArray, length: Int): PacketInfo?
}

class SimplePacketParser : PacketParser {
    override fun parse(packet: ByteArray, length: Int): PacketInfo? {
        if (length < 1) return null
        val version = (packet[0].toInt() shr 4) and 0x0F
        return when (version) {
            4 -> parseIPv4(packet, length)
            6 -> parseIPv6(packet, length)
            else -> null
        }
    }

    private fun parseIPv4(packet: ByteArray, length: Int): PacketInfo? {
        if (length < 20) return null
        val headerLen = (packet[0].toInt() and 0x0F) * 4
        if (length < headerLen) return null

        val protocol = packet[9].toInt() and 0xFF
        val srcIp = formatIPv4(packet, 12)
        val destIp = formatIPv4(packet, 16)
        val (srcPort, destPort) = extractPorts(packet, length, headerLen, protocol)

        val payloadStart = headerLen + transportHeaderSize(protocol)
        val payload = if (length > payloadStart) packet.copyOfRange(payloadStart, length) else null
        return PacketInfo(protocol, srcIp, srcPort, destIp, destPort, (length - headerLen).toLong(), payload)
    }

    private fun parseIPv6(packet: ByteArray, length: Int): PacketInfo? {
        if (length < 40) return null // IPv6 fixed header is 40 bytes
        val protocol = packet[6].toInt() and 0xFF
        val srcIp = formatIPv6(packet, 8)
        val destIp = formatIPv6(packet, 24)
        val (srcPort, destPort) = extractPorts(packet, length, 40, protocol)

        val payloadStart = 40 + transportHeaderSize(protocol)
        val payload = if (length > payloadStart) packet.copyOfRange(payloadStart, length) else null
        return PacketInfo(protocol, srcIp, srcPort, destIp, destPort, (length - 40).toLong(), payload)
    }

    private fun formatIPv4(packet: ByteArray, offset: Int): String {
        return "${packet[offset].toInt() and 0xFF}.${packet[offset + 1].toInt() and 0xFF}" +
               ".${packet[offset + 2].toInt() and 0xFF}.${packet[offset + 3].toInt() and 0xFF}"
    }

    private fun formatIPv6(packet: ByteArray, offset: Int): String {
        return (0 until 8).joinToString(":") { i ->
            val hi = packet[offset + i * 2].toInt() and 0xFF
            val lo = packet[offset + i * 2 + 1].toInt() and 0xFF
            "%02x%02x".format(hi, lo)
        }
    }

    /** Returns (srcPort, destPort) for TCP/UDP; (0, 0) for other protocols. */
    private fun extractPorts(packet: ByteArray, length: Int, transportOffset: Int, protocol: Int): Pair<Int, Int> {
        if ((protocol == 6 || protocol == 17) && length >= transportOffset + 4) {
            val src = ((packet[transportOffset].toInt() and 0xFF) shl 8) or (packet[transportOffset + 1].toInt() and 0xFF)
            val dst = ((packet[transportOffset + 2].toInt() and 0xFF) shl 8) or (packet[transportOffset + 3].toInt() and 0xFF)
            return src to dst
        }
        return 0 to 0
    }

    private fun transportHeaderSize(protocol: Int): Int = when (protocol) {
        6 -> 20  // TCP minimum header
        17 -> 8  // UDP header
        else -> 0
    }
}
