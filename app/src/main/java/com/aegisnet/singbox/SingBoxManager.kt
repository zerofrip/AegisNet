package com.aegisnet.singbox

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SingBoxManager @Inject constructor(
    private val singBoxController: SingBoxController,
    private val configGenerator: ConfigGenerator
) {
    private var isRunning = false

    fun start(tunFd: Int) {
        if (isRunning) return
        
        // Generate JSON dynamically
        val configJson = configGenerator.build()
        val errorMsg = singBoxController.startSingBox(configJson, tunFd)
        if (errorMsg.isEmpty()) {
            isRunning = true
        }
    }

    fun stop() {
        if (!isRunning) return
        singBoxController.stopSingBox()
        isRunning = false
    }

    fun isRunning(): Boolean {
        return isRunning
    }
}
