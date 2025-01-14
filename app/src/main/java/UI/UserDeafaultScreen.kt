package UI

import Flow.Screen
import ViewModels.AuthViewModel
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.example.welfare.R
import Extras.Result
import androidx.compose.runtime.livedata.observeAsState
import com.google.firebase.firestore.FirebaseFirestore
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun DefaultScreen(navController: NavController, authViewModel: AuthViewModel) {
    val systemUiController = rememberSystemUiController()
    val statusBarColor = Color.Black

    LaunchedEffect(true) {
        systemUiController.setStatusBarColor(color = statusBarColor, darkIcons = true)
    }

    val scaffoldState = rememberScaffoldState()
    var warningMessage by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }

    // Observe user home location
    val userHomeLocationResult by authViewModel.userHomeLocation.observeAsState()

    // Fetch home location on screen load
    LaunchedEffect(Unit) {
        authViewModel.fetchUserHomeLocation()
    }

    // Handle changes to the user home location result
    LaunchedEffect(userHomeLocationResult) {
        when (userHomeLocationResult) {
            is Result.Success -> {
                val (homeLat, homeLng) = (userHomeLocationResult as Result.Success).data
                fetchClusterWarnings(FirebaseFirestore.getInstance(), homeLat, homeLng) { warning ->
                    warningMessage = warning
                    loading = false
                }
            }
            is Result.Error -> {
                warningMessage = "Error: ${(userHomeLocationResult as Result.Error).message}"
                loading = false
            }
            else -> {
                loading = true
            }
        }
    }

    // UI
    androidx.compose.material.Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorResource(id = R.color.black)
                ),
                title = { Text(text = "Home", color = colorResource(id = R.color.white)) },
                actions = {
                    androidx.compose.material.IconButton(onClick = {
                        authViewModel.logout()
                        navController.navigate(Screen.LoginScreen.route) {
                            popUpTo(Screen.DefaultScreen.route) { inclusive = true }
                            popUpTo(0)
                        }
                    }) {
                        androidx.compose.material.Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (loading) {
                CircularProgressIndicator()
            } else {
                Text(
                    text = warningMessage ?: "No issues detected in your area.",
                    color = if (warningMessage?.contains("Warning") == true) Color.Red else Color.Green,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Navigation Buttons
            Button(onClick = { navController.navigate(Screen.CaptureImageScreen.route) }) {
                Text(text = "Image Capture")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { navController.navigate(Screen.SelectImageScreen.route) }) {
                Text(text = "Image Selection")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { navController.navigate(Screen.donation.route) }) {
                Text(text = "Donation")
            }
        }
    }
}



fun fetchClusterWarnings(
    firestore: FirebaseFirestore,
    userHomeLat: Double,
    userHomeLng: Double,
    onResult: (String?) -> Unit
) {
    val warningRadius = 5.0 // 5 km

    firestore.collection("garbage_clusters")
        .get()
        .addOnSuccessListener { snapshot ->
            val warnings = snapshot.documents.mapNotNull { doc ->
                val centerLat = doc.getDouble("centerLat")
                val centerLng = doc.getDouble("centerLng")

                if (centerLat != null && centerLng != null) {
                    val distance = calculateDistance(userHomeLat, userHomeLng, centerLat, centerLng)
                    if (distance <= warningRadius) {
                        "\"Warning: High garbage density near your home (within $warningRadius km)! Increased risk " +
                                "of airborne diseases such as cholera, typhoid, dysentery, and respiratory infections.\"\n"
                    } else null
                } else null
            }
            onResult(warnings.firstOrNull() ?: "No issues detected in your area.")
        }
        .addOnFailureListener {
            onResult("Error fetching cluster data.")
        }
}

fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371.0 // Earth's radius in kilometers
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return earthRadius * c
}
