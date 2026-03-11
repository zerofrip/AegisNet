package com.aegisnet.firewall

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.net.InetSocketAddress
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UIDResolver @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val connectivityManager: ConnectivityManager = 
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
    private val packageManager: PackageManager = context.packageManager
    
    // Cache map for UID -> PackageName to avoid repeated PM queries
    private val uidPackageCache = mutableMapOf<Int, String>()

    /**
     * Resolves the UID that owns the given network connection.
     * Uses Android 10+ APIs for connection ownership.
     */
    fun resolveUID(protocol: Int, sourceIp: String, sourcePort: Int, destIp: String, destPort: Int): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val proto = if (protocol == 6) android.system.OsConstants.IPPROTO_TCP else android.system.OsConstants.IPPROTO_UDP
                val localAddr = InetSocketAddress(sourceIp, sourcePort)
                val remoteAddr = InetSocketAddress(destIp, destPort)
                
                return connectivityManager.getConnectionOwnerUid(proto, localAddr, remoteAddr)
            } catch (e: Exception) {
                Log.e("UIDResolver", "Failed to resolve UID for connection", e)
            }
        }
        return -1
    }

    /**
     * Gets the main package name for a given UID.
     */
    fun getPackageNameForUid(uid: Int): String? {
        if (uid < 0) return null
        
        return uidPackageCache.getOrPut(uid) {
            val packages = packageManager.getPackagesForUid(uid)
            packages?.firstOrNull() ?: "unknown"
        }.takeIf { it != "unknown" }
    }
    
    /**
     * Clears the cache when packages are added/removed.
     */
    fun invalidateCache() {
        uidPackageCache.clear()
    }
}
