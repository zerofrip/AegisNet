package com.aegisnet.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import com.aegisnet.singbox.SingBoxManager
import com.aegisnet.firewall.engine.AppFirewallEngineFacade
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AegisVpnService : VpnService() {

    @Inject
    lateinit var singBoxManager: SingBoxManager

    @Inject
    lateinit var firewallEngine: AppFirewallEngineFacade

    private var vpnInterface: ParcelFileDescriptor? = null
    
    companion object {
        const val ACTION_START = "com.aegisnet.vpn.START"
        const val ACTION_STOP = "com.aegisnet.vpn.STOP"
        const val ACTION_UPDATE_ROUTES = "com.aegisnet.vpn.UPDATE_ROUTES"
        private const val NOTIFICATION_CHANNEL_ID = "aegis_vpn_channel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startVpn()
            ACTION_STOP -> stopVpn()
            ACTION_UPDATE_ROUTES -> {
                if (singBoxManager.isRunning()) {
                    setupVpnInterface()
                }
            }
        }
        return START_STICKY
    }

    private fun startVpn() {
        if (singBoxManager.isRunning()) return

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        setupVpnInterface()
        
        // Pass the file descriptor to the top-level Manager handling JNI and ConfigGenerator
        vpnInterface?.let {
            singBoxManager.start(it.fd)
        }
    }

    private fun stopVpn() {
        if (singBoxManager.isRunning()) {
            singBoxManager.stop()
        }
        
        vpnInterface?.close()
        vpnInterface = null
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun setupVpnInterface() {
        val builder = Builder()
            .setSession("AegisNet")
            .setMtu(1500)
            .addAddress("172.19.0.1", 30) // IPv4
            .addAddress("fdfe:dcba:9876::1", 126) // IPv6
            .addRoute("0.0.0.0", 0)
            .addRoute("::", 0)

        // Apply per-app proxy based on firewall rules
        val bypassedApps = firewallEngine.getBypassedApps()
        val blockedApps = firewallEngine.getBlockedApps()
        
        bypassedApps.forEach { packageName ->
            try {
                builder.addDisallowedApplication(packageName)
            } catch (e: PackageManager.NameNotFoundException) {
                // App uninstalled
            }
        }
        
        // We let blocked apps through the TUN so the AppFirewallEngine can log their attempts before dropping them
        
        try {
            vpnInterface = builder.establish()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // generateSingboxConfig() logic has been moved inside SingBoxManager -> ConfigGenerator

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "AegisNet VPN Status",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, launchIntent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("AegisNet is active")
            .setContentText("Privacy firewall routing and filtering traffic")
            .setSmallIcon(android.R.drawable.ic_secure)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }
}
