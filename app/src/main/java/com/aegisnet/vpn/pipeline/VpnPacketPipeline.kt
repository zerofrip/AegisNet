package com.aegisnet.vpn.pipeline

import android.util.Log
import com.aegisnet.filter.http.HttpRuleEngine
import com.aegisnet.firewall.engine.AppFirewallEngineFacade
import com.aegisnet.firewall.engine.RouteAction
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VpnPacketPipeline @Inject constructor(
    private val firewallFacade: AppFirewallEngineFacade,
    private val httpRuleEngine: HttpRuleEngine
) {
    private val packetParser = SimplePacketParser()
    private val tlsEngine = TlsFingerprintEngine()

    fun onPacketReceived(packet: ByteArray, length: Int): PacketAction {
        val info = packetParser.parse(packet, length) 
            ?: return PacketAction.ALLOW // Pass through unrecognized packets

        var domain: String? = null
        
        // Try to extract domain from DNS (Port 53) or SNI (Port 443)
        if (info.destPort == 443 && info.rawPayload != null) {
            domain = tlsEngine.extractSni(info.rawPayload)
        } else if (info.destPort == 80 && info.rawPayload != null) {
            val payloadStr = String(info.rawPayload)
            val hostLine = payloadStr.split("\r\n").find { it.startsWith("Host: ") }
            domain = hostLine?.substringAfter("Host: ")?.trim()
        }

        // 1. App Firewall Layer (UID detection & Routing)
        val routeAction = firewallFacade.processPacket(
            info.protocol,
            info.sourceIp,
            info.sourcePort,
            info.destIp,
            info.destPort,
            domain,
            info.payloadSize
        )

        // Drop immediately if firewall says BLOCK
        if (routeAction == RouteAction.BLOCK) {
            return PacketAction.DROP(info.protocol)
        }
        
        // 2. HTTP Rule Engine Layer (Deep Packet Inspection)
        if (httpRuleEngine.isEnabled && domain != null) {
            // Reconstruct a URL for the HTTP Engine to match 
            // Ideally we'd have the full path, but for HTTPS we only have the domain
            val scheme = if (info.destPort == 443) "https" else "http"
            val urlToMatch = "$scheme://$domain/"
            
            val match = httpRuleEngine.match(urlToMatch, domain)
            if (match != null) {
                Log.d("VpnPipeline", "Blocked by HTTP Rule: ${match.pattern}")
                
                // Return appropriate blocking action
                return if (info.destPort == 53) {
                    PacketAction.DNS_BLOCK
                } else if (info.protocol == 6) {
                    PacketAction.TCP_RST
                } else {
                    PacketAction.DROP(info.protocol)
                }
            }
        }
        
        return PacketAction.ALLOW
    }
}

sealed class PacketAction {
    object ALLOW : PacketAction()
    data class DROP(val protocol: Int) : PacketAction()
    object TCP_RST : PacketAction()
    object DNS_BLOCK : PacketAction() // For returning 0.0.0.0
}
