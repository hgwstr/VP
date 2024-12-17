package com.example.test.network

import android.content.Context
import kotlinx.coroutines.*
import okhttp3.*
import okio.ByteString
import com.example.test.location.LocationAct
import com.example.test.CellInfoAct

class WebSocketAct(private val context: Context) {
    private val client = OkHttpClient()
    private val request = Request.Builder()
        .url("https://hzforl-2a01-620-c199-8d01-3421-fb9a-ee61-ea46.ru.tuna.am")
        .build()
    private var webSocket: WebSocket? = null
    private val cellInfoAct = CellInfoAct(context)

    // CoroutineScope для управления периодическими задачами
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("WebSocket opened")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                println("Message received: $text")
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                println("Message received: ${bytes.hex()}")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                println("Closing WebSocket: $code $reason")
                webSocket.close(1000, null)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("WebSocket failure: ${t.message}")
            }
        })
    }

    // Функция для отправки данных (то, что уже есть)
    fun sendLocationData(locationAct: LocationAct) {
        val latitude = locationAct.latitude.value
        val longitude = locationAct.longitude.value
        val rsrp = cellInfoAct.getRsrp()

        if (latitude != null && longitude != null && rsrp != null) {
            val jsonData = """{"rsrp": $rsrp, "lat": $latitude, "lon": $longitude}"""
            webSocket?.send(jsonData)
        }
    }

    // Функция для периодической отправки данных
    fun startPeriodicSend(intervalMillis: Long, locationAct: LocationAct) {
        coroutineScope.launch {
            while (isActive) {
                sendLocationData(locationAct)
                delay(intervalMillis)
            }
        }
    }

    // Очистка ресурсов
    fun stopPeriodicSend() {
        coroutineScope.cancel()
    }
}

