package com.aegisnet.vpn.pipeline

class TlsFingerprintEngine {
    
    /**
     * Extracts the Server Name Indication (SNI) from a TLS ClientHello packet.
     * Parses the binary TLS record to find the SNI extension (type 0x0000).
     * Returns the host string if found, null otherwise.
     */
    fun extractSni(payload: ByteArray): String? {
        if (payload.size < 43) return null
        
        // TLS record header: ContentType(1) + Version(2) + Length(2)
        // ContentType must be Handshake (0x16)
        if (payload[0] != 0x16.toByte()) return null
        // Major version must be TLS (0x03)
        if (payload[1] != 0x03.toByte()) return null
        
        var offset = 5 // Skip TLS record header
        
        // Handshake header: Type(1) + Length(3)
        // Type must be ClientHello (0x01)
        if (offset >= payload.size || payload[offset] != 0x01.toByte()) return null
        offset += 4 // Skip handshake type (1) + length (3)
        
        // ClientHello: Version(2) + Random(32)
        offset += 2 + 32
        if (offset >= payload.size) return null
        
        // Session ID: Length(1) + Data(variable)
        val sessionIdLen = payload.getUByte(offset)
        offset += 1 + sessionIdLen
        if (offset + 2 > payload.size) return null
        
        // Cipher Suites: Length(2) + Data(variable)
        val cipherSuitesLen = payload.getUShort(offset)
        offset += 2 + cipherSuitesLen
        if (offset + 1 > payload.size) return null
        
        // Compression Methods: Length(1) + Data(variable)
        val compressionLen = payload.getUByte(offset)
        offset += 1 + compressionLen
        if (offset + 2 > payload.size) return null
        
        // Extensions: Length(2) + Data(variable)
        val extensionsLen = payload.getUShort(offset)
        offset += 2
        val extensionsEnd = offset + extensionsLen
        if (extensionsEnd > payload.size) return null
        
        // Iterate through extensions to find SNI (type 0x0000)
        while (offset + 4 <= extensionsEnd) {
            val extType = payload.getUShort(offset)
            val extLen = payload.getUShort(offset + 2)
            offset += 4
            
            if (extType == 0 && extLen > 0) {
                // SNI extension found — parse ServerNameList
                return parseSniExtension(payload, offset, extLen)
            }
            
            offset += extLen
        }
        
        return null
    }
    
    private fun parseSniExtension(payload: ByteArray, start: Int, length: Int): String? {
        var offset = start
        if (offset + 2 > payload.size) return null
        
        // ServerNameList length (2 bytes)
        val listLen = payload.getUShort(offset)
        offset += 2
        val listEnd = offset + listLen
        if (listEnd > payload.size || listEnd > start + length) return null
        
        while (offset + 3 <= listEnd) {
            val nameType = payload.getUByte(offset)
            offset += 1
            val nameLen = payload.getUShort(offset)
            offset += 2
            
            // NameType 0 = host_name
            if (nameType == 0 && nameLen > 0 && offset + nameLen <= listEnd) {
                return String(payload, offset, nameLen, Charsets.US_ASCII)
            }
            offset += nameLen
        }
        return null
    }
    
    private fun ByteArray.getUByte(offset: Int): Int {
        return this[offset].toInt() and 0xFF
    }
    
    private fun ByteArray.getUShort(offset: Int): Int {
        return ((this[offset].toInt() and 0xFF) shl 8) or (this[offset + 1].toInt() and 0xFF)
    }
}
