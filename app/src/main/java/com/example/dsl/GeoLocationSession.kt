package com.example.dsl

import com.example.test.CellInfoAct
import com.example.test.location.LocationAct
import com.example.test.network.WebSocketAct
import kotlinx.coroutines.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.*
import kotlinx.coroutines.CoroutineScope

// Главная DSL-функция
fun geolocationSession(
    locationAct: LocationAct,
    webSocketAct: WebSocketAct,
    cellInfoAct: CellInfoAct,
    content: GeolocationSessionScope.() -> Unit
): GeolocationSessionScope {
    val scope = GeolocationSessionScope(locationAct, webSocketAct, cellInfoAct)
    scope.content()
    return scope
}


// Основной scope DSL
class GeolocationSessionScope(
    private val locationAct: LocationAct,
    private val webSocketAct: WebSocketAct,
    private val cellInfoAct: CellInfoAct
) {
    private var navContent: @Composable () -> Unit = {}

    fun startLocationUpdates() {
        locationAct.startLocationUpdates()
    }

    fun periodicallySendData(intervalMillis: Long, sendData: () -> Unit) {
        webSocketAct.startPeriodicSend(intervalMillis, locationAct)
    }
    fun sendToWebSocket() {
        webSocketAct.sendLocationData(locationAct)
    }
    fun navigationUI(content: NavigationScope.() -> Unit) {
        val navScope = NavigationScope(locationAct, webSocketAct, cellInfoAct)
        navScope.content()
        this.navContent = navScope.build()
    }

    @Composable
    fun renderUI() {
        navContent()
    }
}

// Вложенный scope для UI
class NavigationScope(
    private val locationAct: LocationAct,
    private val webSocketAct: WebSocketAct,
    private val cellInfoAct: CellInfoAct
) {
    private val screens = mutableListOf<Screen>()

    fun bottomBar(content: BottomBarScope.() -> Unit) {
        val bottomBarScope = BottomBarScope()
        bottomBarScope.content()
        screens.addAll(bottomBarScope.getScreens())
    }

    fun build(): @Composable () -> Unit = {
        val navController = rememberNavController()

        Scaffold(
            bottomBar = {
                NavigationBar {
                    screens.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            selected = navController.currentDestination?.route == screen.route,
                            onClick = { navController.navigate(screen.route) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = screens.firstOrNull()?.route ?: "",
                Modifier.padding(innerPadding)
            ) {
                screens.forEach { screen ->
                    composable(screen.route) { screen.content() }
                }
            }
        }
    }
}

// Экран для навигации
data class Screen(
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val content: @Composable () -> Unit
)

// Вспомогательный scope для нижней панели
class BottomBarScope {
    private val screens = mutableListOf<Screen>()

    fun screen(route: String, icon: androidx.compose.ui.graphics.vector.ImageVector, content: @Composable () -> Unit) {
        screens.add(Screen(route, icon, content))
    }

    fun getScreens(): List<Screen> = screens
}

