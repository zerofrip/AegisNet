package com.aegisnet.vpn

import android.net.VpnService
import android.os.ParcelFileDescriptor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TunManager @Inject constructor() {

    private var vpnInterface: ParcelFileDescriptor? = null

    fun establish(builder: VpnService.Builder): Int? {
        // Configure standard TUN parameters
        builder.setSession("AegisNet")
            .setMtu(1500)
            .addAddress("172.19.0.1", 30)
            .addAddress("fdfe:dcba:9876::1", 126)
            .addRoute("0.0.0.0", 0)
            .addRoute("::", 0)
            .addDnsServer("198.18.0.1") // Route DNS to FakeDNS

        vpnInterface = builder.establish()
        return vpnInterface?.fd
    }

    fun close() {
        vpnInterface?.close()
        vpnInterface = null
    }

    fun isEstablished(): Boolean {
        return vpnInterface != null
    }
}
