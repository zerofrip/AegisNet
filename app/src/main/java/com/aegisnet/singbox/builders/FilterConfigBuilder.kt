package com.aegisnet.singbox.builders

import com.aegisnet.filter.engine.UnifiedRule
import com.aegisnet.filter.compiler.SingboxRuleCompiler
import org.json.JSONArray
import org.json.JSONObject

/**
 * Specialized builder executing High-Performance `SingboxRuleCompiler` instructions over UnifiedRules.
 */
object FilterConfigBuilder {
    
    fun build(unifiedRules: List<UnifiedRule>): JSONArray {
        // Utilizing the bridge compiler engineered natively in `com.aegisnet.filter.compiler`.
        // Converts memory-optimzied UnifiedRule structs down to the physical JSON syntax arrays.
        return SingboxRuleCompiler.compileBatch(unifiedRules)
    }
}
