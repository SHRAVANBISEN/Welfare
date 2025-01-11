package UI

import Flow.Screen
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.location.Location
import android.net.Uri
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import androidx.navigation.NavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import ViewModels.ReportGarbageViewModel
import ViewModels.ReportGarbageViewModelFactory
import java.io.File
import kotlin.coroutines.resume

@Composable
fun ReportGarbageScreen(
    navController: NavController,
) {
    val context = LocalContext.current
    val viewModel: ReportGarbageViewModel = viewModel(
        factory = ReportGarbageViewModelFactory(context)
    )
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    // State variables for the camera preview and image capture
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Observe the detection results from the ViewModel
    val detectionResult by viewModel.detectionResult.collectAsState()
    var isGarbageDetected by remember { mutableStateOf(false) }

    LaunchedEffect(detectionResult) {
        detectionResult?.let { result ->
            isGarbageDetected = result.isGarbageDetected
        }
    }

    // Initialize the camera
    LaunchedEffect(cameraProviderFuture) {
        startCamera(context, lifecycleOwner, previewView) { capture ->
            imageCapture = capture
        }
    }

    // UI layout
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Camera Preview
        AndroidView(
            factory = { previewView },
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Capture and Analyze Button
        Button(onClick = {
            coroutineScope.launch {
                if (checkLocationPermissions(context)) {
                    imageCapture?.let { capture ->
                        captureImageWithLocation(context, capture) { uri ->
                            uri?.let {
                                capturedImageUri = it
                                viewModel.processImage(it) // Send the image URI to the ViewModel for processing
                            }
                        }
                    }
                } else {
                    requestLocationPermissions(context)
                }
            }
        }) {
            Text("Capture and Analyze")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display Results
        detectionResult?.let { result ->
            Text(result.message)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display Captured Image
        capturedImageUri?.let {
            Image(painter = rememberImagePainter(it), contentDescription = null, modifier = Modifier.size(200.dp))
        }

        // Display Next Button if Garbage is Detected
        if (isGarbageDetected && capturedImageUri != null) {
            Button(onClick = { navController.navigate("${Screen.FormScreen.route}?imageUri=${capturedImageUri}") }) {
                Text("Next")
            }
        }
    }
}

// Function to Check Location Permissions
private fun checkLocationPermissions(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
}

// Function to Request Location Permissions
private fun requestLocationPermissions(context: Context) {
    // Handle permission request logic here (e.g., using ActivityResultLauncher in Compose)
}

// Function to Start Camera
private fun startCamera(
    context: Context,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    previewView: PreviewView,
    onImageCaptureReady: (ImageCapture) -> Unit
) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    cameraProviderFuture.addListener({
        val cameraProvider = cameraProviderFuture.get()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageCapture = ImageCapture.Builder().build()
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
            onImageCaptureReady(imageCapture)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }, ContextCompat.getMainExecutor(context))
}

// Function to Overlay Location on Captured Image
fun addLocationToImage(imageUri: Uri, context: Context, location: Location?): Uri? {
    try {
        val bitmap = android.graphics.BitmapFactory.decodeStream(context.contentResolver.openInputStream(imageUri))
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        val canvas = Canvas(mutableBitmap)
        val paint = Paint().apply {
            color = ContextCompat.getColor(context, android.R.color.holo_red_dark)
            textSize = 40f
            isAntiAlias = true
        }

        val locationText = if (location != null) {
            "Lat: ${location.latitude}, Lon: ${location.longitude}"
        } else {
            "Location: Not Available"
        }

        canvas.drawText(locationText, 20f, mutableBitmap.height - 50f, paint)

        // Save the modified image to a new file
        val photoFile = File(context.cacheDir, "location_${System.currentTimeMillis()}.jpg")
        val outputStream = photoFile.outputStream()
        mutableBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.close()

        return Uri.fromFile(photoFile)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

// Function to Capture Image with Location
private fun captureImageWithLocation(
    context: Context,
    imageCapture: ImageCapture,
    onImageCaptured: (Uri?) -> Unit
) {
    val photoFile = File(context.cacheDir, "${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val capturedUri = Uri.fromFile(photoFile)

                // Check if location permissions are granted
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val locationClient: FusedLocationProviderClient =
                        LocationServices.getFusedLocationProviderClient(context)

                    locationClient.lastLocation.addOnSuccessListener { location ->
                        val updatedImageUri = addLocationToImage(capturedUri, context, location)
                        onImageCaptured(updatedImageUri) // Return the updated image URI
                    }.addOnFailureListener {
                        onImageCaptured(capturedUri) // Return the image without location if location fetch fails
                    }
                } else {
                    // If permission is not granted, return the image without location
                    onImageCaptured(capturedUri)
                }
            }

            override fun onError(exception: ImageCaptureException) {
                exception.printStackTrace()
                onImageCaptured(null)
            }
        }
    )
}