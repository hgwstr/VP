package com.example.test

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import com.example.test.ui.theme.TestTheme
import com.example.test.location.LocationAct
import com.example.test.network.WebSocketAct
import com.example.test.ui.Interface
import com.google.android.gms.location.LocationServices
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.example.test.map.MapViewComposable
import androidx.compose.runtime.collectAsState
import org.osmdroid.config.Configuration

import com.example.dsl.geolocationSession

class MainActivity : ComponentActivity() {
    private lateinit var locationAct: LocationAct
    private lateinit var webSocketAct: WebSocketAct
    private lateinit var cellInfoAct: CellInfoAct

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        locationAct = LocationAct(this, LocationServices.getFusedLocationProviderClient(this))
        webSocketAct = WebSocketAct(this)
        cellInfoAct = CellInfoAct(this)

        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (isGranted) {
                    locationAct.getLocation()
                }
            }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Режим полного экрана
        enableEdgeToEdge()

        setContent {
            TestTheme {
                // DSL для запуска геолокации, отправки данных и навигации
                geolocationSession(
                    locationAct = locationAct,
                    webSocketAct = webSocketAct,
                    cellInfoAct = cellInfoAct
                ) {
                    startLocationUpdates() // Начинаем получать локацию
                    periodicallySendData(intervalMillis = 5000){
                        sendToWebSocket()
                    } // Отправляем данные каждые 5 секунд

                    navigationUI {
                        bottomBar {
                            // Экран "Location"
                            screen("location", Icons.Filled.LocationOn) {
                                Interface(
                                    locationAct = locationAct,
                                    webSocketAct = webSocketAct,
                                    lifecycleOwner = this@MainActivity
                                )
                            }

                            // Экран "Map"
                            screen("map", Icons.Filled.Home) {
                                MapScreen(locationAct = locationAct, context = this@MainActivity)
                            }
                        }
                    }
                }.renderUI() // Теперь вызываем renderUI() корректно
            }
        }

    }
    @Composable
    fun MapScreen(locationAct: LocationAct, context: Context) {
        val latitude = locationAct.latitude.collectAsState().value
        val longitude = locationAct.longitude.collectAsState().value

        if (latitude != null && longitude != null) {
            MapViewComposable(context = context, latitude = latitude, longitude = longitude, rsrp = -85)
        } else {
            Text("Загрузка геолокации...", style = MaterialTheme.typography.bodyLarge)
        }
    }

}