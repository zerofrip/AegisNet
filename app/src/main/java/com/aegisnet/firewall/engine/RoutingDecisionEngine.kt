package com.aegisnet.firewall.engine

import javax.inject.Inject
import javax.inject.Singleton

enum class RouteAction {
    DIRECT,
    WIREGUARD,
    BLOCK,
    BYPASS
}

@Singleton
class RoutingDecisionEngine @Inject constructor(
    private val appRuleManager: AppRuleManager
) {

    /**
     * Decides how to route a specific packet based on its origin UID and target.
     * 
     * @param uid The Android UID of the app originating the traffic
     * @param protocol UDP (17) or TCP (6)
     * @param destPort Destination port
     * @param domain The resolved domain name (if SNI/DNS sniffing available, else null)
     */
    fun decideRoute(uid: Int, protocol: Int, destPort: Int, domain: String?): RouteAction {
        // Default action if no rules apply
        var finalAction = RouteAction.WIREGUARD
        
        val routingRule = appRuleManager.getRoutingRule(uid)

        // 1. Check App-level Routing Mode (Direct, WG, Block, Bypass)
        if (routingRule != null) {
            if (routingRule.bypassVpn) return RouteAction.BYPASS
            
            finalAction = when (routingRule.routeMode) {
                "DIRECT" -> RouteAction.DIRECT
                "BLOCK" -> RouteAction.BLOCK
                else -> RouteAction.WIREGUARD
            }
            
            // 2. Check QUIC Blocking (UDP 443)
            if (routingRule.blockQuic && protocol == 17 && destPort == 443) {
                return RouteAction.BLOCK
            }
        }

        // 3. Domain Rules (Highest Priority)
        // Only evaluate if we have a domain and packet is not already bypassed
        if (domain != null && finalAction != RouteAction.BYPASS) {
            val domainAction = appRuleManager.matchDomain(uid, domain)
            when (domainAction) {
                "BLOCK" -> return RouteAction.BLOCK
                "ALLOW" -> {
                    // Allow rule overrides blocking, but respects routing mode
                    if (finalAction == RouteAction.BLOCK) {
                        finalAction = RouteAction.WIREGUARD // Revert to safe default
                    }
                }
            }
        }

        return finalAction
    }
}
