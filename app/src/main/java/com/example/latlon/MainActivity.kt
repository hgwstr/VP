package com.example.latlon

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.latlon.ui.theme.LatLonTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import android.location.Location

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            LatLonTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LocationScreen(
                        modifier = Modifier.padding(innerPadding),
                        onRequestLocation = { getLocation() }
                    )
                }
            }
        }

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (!isGranted) {
                // Если разрешение не предоставлено, показать сообщение или управлять этим случаем
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        val task: Task<Location> = fusedLocationClient.lastLocation
        task.addOnSuccessListener { location: Location? ->
            location?.let {
                val lat = it.latitude
                val lon = it.longitude
            }
        }
    }

    @Composable
    fun updateUI(lat: Double, lon: Double) {
        LocationScreen(lat = lat, lon = lon, onRequestLocation = { getLocation() })
    }

}

@Composable
fun LocationScreen(
    lat: Double? = null,
    lon: Double? = null,
    modifier: Modifier = Modifier,
    onRequestLocation: () -> Unit
) {
    var latitude by remember { mutableStateOf(lat ?: 0.0) }
    var longitude by remember { mutableStateOf(lon ?: 0.0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Latitude: $latitude")
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Longitude: $longitude")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onRequestLocation() }) {
            Text(text = "Get Location")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun LocationScreenPreview() {
    LatLonTheme {
        LocationScreen(lat = 0.0, lon = 0.0, onRequestLocation = {})
    }
}
