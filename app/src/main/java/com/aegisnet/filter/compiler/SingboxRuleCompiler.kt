package com.aegisnet.filter.compiler

import com.aegisnet.filter.engine.RuleType
import com.aegisnet.filter.engine.UnifiedRule
import org.json.JSONArray
import org.json.JSONObject

/**
 * Maps high-performance UnifiedRule structures directly into Sing-Box declarative route payloads.
 */
object SingboxRuleCompiler {
    
    fun compile(rule: UnifiedRule): JSONObject? {
        val outJson = JSONObject()
        val outboundTag = if (rule.exception) "direct" else "block"
        
        when (rule.type) {
            RuleType.EXACT_DOMAIN -> {
                outJson.put("domain", JSONArray().put(rule.value))
            }
            RuleType.DOMAIN_SUFFIX -> {
                outJson.put("domain_suffix", JSONArray().put(rule.value))
            }
            RuleType.DOMAIN_KEYWORD -> {
                outJson.put("domain_keyword", JSONArray().put(rule.value))
            }
            RuleType.URL_PATH -> {
                // Not supported natively in basic sing-box DNS rules, but we can treat it as a Keyword/Suffix fallback
                // or just ignore if it's strictly HTTP layer.
                return null
            }
            else -> return null
        }
        
        outJson.put("outbound", outboundTag)
        return outJson
    }

    /**
     * Batch compiles thousands of AegisNet structs into compressed Sing-Box Routing rules.
     */
    fun compileBatch(rules: List<UnifiedRule>): JSONArray {
        val array = JSONArray()
        rules.forEach { 
            val json = compile(it)
            if (json != null) {
                array.put(json)
            }
        }
        return array
    }
}
