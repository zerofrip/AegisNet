package com.aegisnet.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aegisnet.ui.dashboard.DashboardScreen
import com.aegisnet.ui.dns.DnsSettingsScreen
import com.aegisnet.ui.filters.FilterListsScreen
import com.aegisnet.ui.routing.RoutingSettingsScreen
import com.aegisnet.ui.settings.LicensesScreen
import com.aegisnet.ui.settings.SettingsScreen
import com.aegisnet.ui.whitelist.WhitelistListsScreen
import com.aegisnet.ui.wireguard.WireGuardSettingsScreen

@Composable
fun AegisNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") {
            DashboardScreen(
                onNavigateToDns = { navController.navigate("dns") },
                onNavigateToFilters = { navController.navigate("filters") },
                onNavigateToWhitelist = { navController.navigate("whitelist") },
                onNavigateToRouting = { navController.navigate("routing") },
                onNavigateToWireGuard = { navController.navigate("wireguard") },
                onNavigateToLicenses = { navController.navigate("licenses") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("dns") { 
            DnsSettingsScreen(onNavigateBack = { navController.popBackStack() }) 
        }
        composable("filters") { 
            FilterListsScreen(onNavigateBack = { navController.popBackStack() }) 
        }
        composable("whitelist") { 
            WhitelistListsScreen(onNavigateBack = { navController.popBackStack() }) 
        }
        composable("routing") { 
            RoutingSettingsScreen(onNavigateBack = { navController.popBackStack() }) 
        }
        composable("wireguard") { 
            WireGuardSettingsScreen(onNavigateBack = { navController.popBackStack() }) 
        }
        composable("licenses") { 
            LicensesScreen(onNavigateBack = { navController.popBackStack() }) 
        }
        composable("settings") { 
            SettingsScreen(onNavigateBack = { navController.popBackStack() }) 
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "$title Screen Content")
    }
}
