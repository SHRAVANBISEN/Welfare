package UI

import Flow.Screen
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import ViewModels.ReportGarbageViewModel
import ViewModels.ReportGarbageViewModelFactory
import androidx.compose.ui.platform.LocalContext

@Composable
fun SelectImage(
    navController: NavController,
) {
    val context = LocalContext.current
    val viewModel: ReportGarbageViewModel = viewModel(
        factory = ReportGarbageViewModelFactory(context)
    )
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val detectionResult by viewModel.detectionResult.collectAsState() // Observe the detection result
    var isGarbagedetected by remember { mutableStateOf(false) } // Track garbage detection

    // Launcher to select an image from the mobile files
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri // Save the selected image URI
        uri?.let {
            viewModel.processImage(it) // Pass the selected image to the ViewModel for processing
        }
    }

    // Update detection state when detection result changes
    LaunchedEffect(detectionResult) {
        detectionResult?.let { result ->
            isGarbagedetected = result.isGarbageDetected
        }
    }

    // UI Layout
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Select Image Button
        Button(onClick = { launcher.launch("image/*") }) {
            Text("Select Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display Detection Result
        detectionResult?.let {
            Text(it.message) // Display result from ViewModel
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display the Selected Image
        imageUri?.let {
            Image(
                painter = rememberImagePainter(it),
                contentDescription = "Selected Image",
                modifier = Modifier.size(200.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Show "Next" Button if garbage is detected
        if (isGarbagedetected && imageUri != null) {
            Button(onClick = {
                navController.navigate("${Screen.FormScreen.route}?imageUri=${imageUri}")
            }) {
                Text("Next")
            }
        }
    }
}
