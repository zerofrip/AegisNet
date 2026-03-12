package com.aegisnet.firewall.engine

import android.content.Context
import android.content.pm.PackageManager
import com.aegisnet.firewall.UIDResolver
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class AppFirewallEngineFacade @Inject constructor(
    private val uidResolver: UIDResolver,
    private val routingDecisionEngine: RoutingDecisionEngine,
    private val trafficLogger: TrafficLogger,
    private val appRuleManager: AppRuleManager,
    @ApplicationContext private val context: Context
) {

    fun processPacket(
        protocol: Int,
        sourceIp: String,
        sourcePort: Int,
        destIp: String,
        destPort: Int,
        domain: String?,
        packetSize: Long
    ): RouteAction {
        val uid = uidResolver.resolveUID(protocol, sourceIp, sourcePort, destIp, destPort)
        
        if (uid < 0) {
            return RouteAction.WIREGUARD 
        }

        val action = routingDecisionEngine.decideRoute(uid, protocol, destPort, domain)
        
        if (domain != null || destPort in listOf(80, 443, 53)) {
             trafficLogger.logConnection(
                 uid = uid,
                 domain = domain ?: destIp,
                 ip = destIp,
                 action = action.name
             )
        }

        trafficLogger.recordTraffic(uid, uploadBytes = packetSize, downloadBytes = 0)

        return action
    }
    
    fun getBypassedApps(): List<String> {
        val packageManager = context.packageManager
        val bypassed = mutableListOf<String>()
        
        appRuleManager.getAllRoutingRules().forEach { rule ->
            if (rule.bypassVpn) {
                val packageName = uidResolver.getPackageNameForUid(rule.appUid)
                if (packageName != null) bypassed.add(packageName)
            }
        }
        return bypassed
    }
    
    fun getBlockedApps(): List<String> {
        val packageManager = context.packageManager
        val blocked = mutableListOf<String>()
        
        appRuleManager.getAllRoutingRules().forEach { rule ->
            if (rule.routeMode == "BLOCK") {
                val packageName = uidResolver.getPackageNameForUid(rule.appUid)
                if (packageName != null) blocked.add(packageName)
            }
        }
        return blocked
    }
}
