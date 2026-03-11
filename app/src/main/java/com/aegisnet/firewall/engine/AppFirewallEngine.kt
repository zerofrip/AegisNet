package com.aegisnet.firewall.engine

import com.aegisnet.firewall.UIDResolver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppFirewallEngine @Inject constructor(
    private val uidResolver: UIDResolver,
    private val routingDecisionEngine: RoutingDecisionEngine,
    private val trafficLogger: TrafficLogger
) {

    /**
     * Entry point for the VPN Packet Pipeline to determine the fate of a packet.
     * 
     * @param protocol IP Protocol number (6 for TCP, 17 for UDP)
     * @param sourceIp Source IP Address (from packet header)
     * @param sourcePort Source Port (from packet header)
     * @param destIp Destination IP Address (from packet header)
     * @param destPort Destination Port (from packet header)
     * @param domain Domain name extracted from SNI or DNS (if available)
     * @param packetSize Size of the packet in bytes
     * 
     * @return The routing action to take (BLOCK, BYPASS, DIRECT, WIREGUARD)
     */
    fun processPacket(
        protocol: Int,
        sourceIp: String,
        sourcePort: Int,
        destIp: String,
        destPort: Int,
        domain: String?,
        packetSize: Long
    ): RouteAction {
        
        // 1. Identify which App generated this packet
        val uid = uidResolver.resolveUID(protocol, sourceIp, sourcePort, destIp, destPort)
        
        // Unknown app or internal VPN connection
        if (uid < 0) {
            return RouteAction.WIREGUARD 
        }

        // 2. Decide the route based on App Firewall rules
        val action = routingDecisionEngine.decideRoute(uid, protocol, destPort, domain)
        
        // 3. Log the connection if it's new (heuristic: only log if we have a domain or specific ports)
        if (domain != null || destPort in listOf(80, 443, 53)) {
             trafficLogger.logConnection(
                 uid = uid,
                 domain = domain ?: destIp,
                 ip = destIp,
                 action = action.name
             )
        }

        // 4. Record traffic stats.
        // For simplicity in this demo, we assume the packetSize is upload traffic
        trafficLogger.recordTraffic(uid, uploadBytes = packetSize, downloadBytes = 0)

        return action
    }
}
