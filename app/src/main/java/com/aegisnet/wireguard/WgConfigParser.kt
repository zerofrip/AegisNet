package com.aegisnet.wireguard

import com.aegisnet.database.entity.WgProfile

object WgConfigParser {

    /**
     * Parses a WireGuard .conf content into a WgProfile entity.
     * Name defaults to filename without extension.
     */
    fun parse(content: String, name: String = "Imported Profile"): WgProfile? {
        val lines = content.lines()
        
        var privateKey = ""
        var publicKey = ""
        var endpoint = ""
        var allowedIps = "0.0.0.0/0, ::/0"
        var dns = "1.1.1.1"
        var mtu = 1280

        var currentSection = ""

        for (line in lines) {
            val trimmed = line.substringBefore("#").trim()
            if (trimmed.isEmpty()) continue

            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                currentSection = trimmed.substring(1, trimmed.length - 1).lowercase()
                continue
            }

            val parts = trimmed.split("=", limit = 2)
            if (parts.size != 2) continue

            val key = parts[0].trim().lowercase()
            val value = parts[1].trim()

            when (currentSection) {
                "interface" -> {
                    when (key) {
                        "privatekey" -> privateKey = value
                        "dns" -> dns = value
                        "mtu" -> mtu = value.toIntOrNull() ?: 1280
                        // address might be needed if mapped internally, but sing-box wg outbound uses local_address
                    }
                }
                "peer" -> {
                    when (key) {
                        "publickey" -> publicKey = value
                        "endpoint" -> endpoint = value
                        "allowedips" -> allowedIps = value
                    }
                }
            }
        }

        if (privateKey.isEmpty() || publicKey.isEmpty() || endpoint.isEmpty()) {
            return null // Invalid config
        }

        return WgProfile(
            name = name,
            privateKey = privateKey,
            publicKey = publicKey,
            endpoint = endpoint,
            allowedIps = allowedIps,
            dns = dns,
            mtu = mtu,
            isActive = false
        )
    }
}
