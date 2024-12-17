package com.example.test.map

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

data class SignalPoint(val latitude: Double, val longitude: Double, val rsrp: Int)

@Composable
fun MapViewComposable(context: Context, latitude: Double?, longitude: Double?, rsrp: Int?) {
    val mapView = remember { MapView(context) }
    val signalPoints = remember { mutableStateListOf<SignalPoint>() }
    val maxPoints = 10
    var isInitialSetup by remember { mutableStateOf(true) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                mapView.onResume()
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                mapView.onPause()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            Configuration.getInstance().userAgentValue = context.packageName
            mapView.setTileSource(TileSourceFactory.MAPNIK)
            mapView.controller.setZoom(18.0)
            mapView.isTilesScaledToDpi = true
            mapView.controller.setCenter(GeoPoint(latitude ?: 0.0, longitude ?: 0.0)) // Установка начального положения
            mapView
        },
        update = {
            if (isInitialSetup && latitude != null && longitude != null) {
                mapView.controller.setCenter(GeoPoint(latitude, longitude))
                isInitialSetup = false
            }

            if (latitude != null && longitude != null && rsrp != null) {
                signalPoints.add(0, SignalPoint(latitude, longitude, rsrp)) // Добавляем в начало
                if (signalPoints.size > maxPoints) {
                    signalPoints.removeLast() // Удаляем лишние точки
                }
            }

            // Удаляем старые маркеры местоположений
            mapView.overlays.removeAll { it is Marker && it.title.startsWith("Signal Strength") }

            val currentZoom = mapView.zoomLevelDouble
            signalPoints.forEach { point ->
                val marker = Marker(mapView)
                marker.position = GeoPoint(point.latitude, point.longitude)
                marker.icon = mapRsrpToDrawable(context, point.rsrp, currentZoom)
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.title = "Signal Strength: ${point.rsrp}"
                mapView.overlays.add(marker)
            }

            if (latitude != null && longitude != null) {
                val geoPoint = GeoPoint(latitude, longitude)

                // Удаляем старый маркер местоположения, если он есть
                mapView.overlays.removeAll { it is Marker && it.title == "Текущее Положение" }

                // Создаем и добавляем маркер текущего местоположения
                val locationMarker = Marker(mapView)
                locationMarker.position = geoPoint
                locationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                locationMarker.title = "Текущее Положение"
                mapView.overlays.add(locationMarker)
            }

            mapView.invalidate()
        }
    )
}

// Обновленная функция для расчета радиуса, принимающая уровень зума
fun mapRsrpToRadius(rsrp: Int, zoom: Double): Dp {
    val baseRadius = (100 - rsrp).coerceAtLeast(100) // Базовый радиус
    val adjustedRadius = baseRadius / (1 shl zoom.toInt()) // Делим на 2^уровеньЗума для уменьшения радиуса при увеличении зума
    return adjustedRadius.dp.coerceAtLeast(100.dp)
}

fun mapRsrpToColor(rsrp: Int): Color {
    val maxRsrp = 100 // Это значение можно настроить на максимальный RSRP, который вы ожидаете
    val red = (255 * (maxRsrp - rsrp)).coerceAtMost(255).toInt()
    val green = (255 * (rsrp / maxRsrp.toDouble())).coerceAtMost(255.0).toInt()
    return Color(red, green, 0)
}

// Обновленная функция для возвращения Drawable
fun mapRsrpToDrawable(context: Context, rsrp: Int, zoom: Double): android.graphics.drawable.Drawable {
    val color = mapRsrpToColor(rsrp)
    val radius = mapRsrpToRadius(rsrp, zoom)

    val oval = OvalShape()
    val shapeDrawable = ShapeDrawable(oval).apply {
        paint.color = color.toArgb() // Set color of the circle
        intrinsicWidth = radius.value.toInt() * 2 // Set size dynamically based on RSRP
        intrinsicHeight = radius.value.toInt() * 2
    }
    return shapeDrawable
}