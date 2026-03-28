package com.handylab.testapplication.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.handylab.testapplication.ui.viewmodel.SpringAppViewModel

@Composable
fun MapScreen(viewModel: SpringAppViewModel) {
    val blossomList by viewModel.blossomList.collectAsState()

    // Default map position to center of South Korea
    val koreaCenter = LatLng(36.5, 127.5)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(koreaCenter, 6.5f)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        blossomList.forEach { info ->
            Marker(
                state = MarkerState(position = LatLng(info.lat, info.lng)),
                title = info.cityName,
                snippet = "개화: ${info.bloomDate} | 만발: ${info.fullBloomDate} | 상태: ${info.status}"
            )
        }
    }
}
