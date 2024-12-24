package UI

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
import kotlinx.coroutines.launch
import ViewModels.ReportGarbageViewModel
import ViewModels.ReportGarbageViewModelFactory

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
                imageCapture?.let { capture ->
                    captureImage(context, capture) { uri ->
                        uri?.let {
                            capturedImageUri = it
                            viewModel.processImage(it) // Send the image URI to the ViewModel for processing
                        }
                    }
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
            Image(painter = rememberImagePainter(it), contentDescription = null)
        }
    }
}

// Function to Start Camera
private fun startCamera(
    context: android.content.Context,
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

// Function to Capture Image
private fun captureImage(
    context: android.content.Context,
    imageCapture: ImageCapture,
    onImageCaptured: (Uri?) -> Unit
) {
    val photoFile = java.io.File(context.cacheDir, "${System.currentTimeMillis()}.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onImageCaptured(android.net.Uri.fromFile(photoFile))
            }

            override fun onError(exception: ImageCaptureException) {
                exception.printStackTrace()
                onImageCaptured(null)
            }
        }
    )
}
