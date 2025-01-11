package MapApi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

class MapPickerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MapPickerScreen(
                onLocationSelected = { location ->
                    val resultIntent = Intent().apply {
                        putExtra("latitude", location.latitude)
                        putExtra("longitude", location.longitude)
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerScreen(
    onLocationSelected: (LatLng) -> Unit
) {
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pick a Location") },
                actions = {
                    TextButton(
                        onClick = {
                            selectedLocation?.let {
                                onLocationSelected(it)
                            }
                        },
                        enabled = selectedLocation != null
                    ) {
                        Text("Confirm")
                    }
                }
            )
        },
        content = { padding ->
            Box(modifier = Modifier.padding(padding)) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(LatLng(28.6129, 77.2295), 15f)
                    },
                    onMapClick = { latLng ->
                        selectedLocation = latLng
                    }
                ) {
                    selectedLocation?.let {
                        Marker(
                            state = MarkerState(position = it),
                            title = "Selected Location"
                        )
                    }
                }
            }
        }
    )
}
