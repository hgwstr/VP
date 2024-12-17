package com.example.dsl

import com.example.test.CellInfoAct
import com.example.test.location.LocationAct
import com.example.test.network.WebSocketAct
import kotlinx.coroutines.*

class GeolocationSession(
    private val locationAct: LocationAct,
    private val webSocketAct: WebSocketAct,
    private val cellInfoAct: CellInfoAct
) {
    private var job: Job? = null

    fun startLocationUpdates() {
        locationAct.startLocationUpdates()
    }

    fun periodicallySendData(intervalMillis: Long = 5000, action: GeolocationSession.() -> Unit) {
        job = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                action()
                delay(intervalMillis)
            }
        }
    }

    fun sendToWebSocket() {
        webSocketAct.sendLocationData(locationAct)
    }

    fun showOnMap(showSignal: Boolean = false) {
        val latitude = locationAct.latitude.value
        val longitude = locationAct.longitude.value
        val rsrp = if (showSignal) cellInfoAct.getRsrp() else null

        println("Map Coordinates: Lat: $latitude, Lon: $longitude, RSRP: $rsrp")
    }

    fun stop() {
        locationAct.stopLocationUpdates()
        job?.cancel()
    }
}

fun geolocationSession(
    locationAct: LocationAct,
    webSocketAct: WebSocketAct,
    cellInfoAct: CellInfoAct,
    block: GeolocationSession.() -> Unit
) {
    val session = GeolocationSession(locationAct, webSocketAct, cellInfoAct)
    session.apply(block)
    session.stop()
}
