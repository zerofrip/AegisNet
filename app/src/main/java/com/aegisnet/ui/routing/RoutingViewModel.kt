package com.aegisnet.ui.routing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aegisnet.database.dao.RoutingRuleDao
import com.aegisnet.database.entity.RoutingRule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutingViewModel @Inject constructor(
    private val routingRuleDao: RoutingRuleDao
) : ViewModel() {

    val rules = routingRuleDao.getAll()

    fun addRule(type: String, value: String, target: String) {
        viewModelScope.launch(Dispatchers.IO) {
            routingRuleDao.insert(
                RoutingRule(type = type, value = value, target = target, isEnabled = true)
            )
        }
    }

    fun toggleRule(rule: RoutingRule) {
        viewModelScope.launch(Dispatchers.IO) {
            routingRuleDao.insert(rule.copy(isEnabled = !rule.isEnabled))
        }
    }

    fun deleteRule(rule: RoutingRule) {
        viewModelScope.launch(Dispatchers.IO) {
            routingRuleDao.delete(rule)
        }
    }
}
