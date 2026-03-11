package com.aegisnet.ui.firewall

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegisnet.database.dao.AppDomainRuleDao
import com.aegisnet.database.entity.AppDomainRule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DomainRuleViewModel @Inject constructor(
    private val ruleDao: AppDomainRuleDao
) : ViewModel() {

    private val _rules = MutableStateFlow<List<AppDomainRule>>(emptyList())
    val rules: StateFlow<List<AppDomainRule>> = _rules

    fun loadRules(appUid: Int) {
        viewModelScope.launch {
            ruleDao.getRulesForApp(appUid).collect {
                _rules.value = it
            }
        }
    }

    fun addRule(appUid: Int, domain: String, action: String, matchType: String) {
        viewModelScope.launch {
            ruleDao.insert(
                AppDomainRule(
                    appUid = appUid,
                    domain = domain,
                    action = action,
                    matchType = matchType
                )
            )
        }
    }

    fun deleteRule(rule: AppDomainRule) {
        viewModelScope.launch {
            ruleDao.delete(rule)
        }
    }
}
