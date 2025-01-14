package UI

import Data.Report
import Extras.GarbageClusterManager
import Extras.saveClustersToFirestore
import MapApi.MapPickerActivity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GarbageReportFormScreen(navController: NavController, capturedImageUri: Uri) {
    var address by remember { mutableStateOf("") }
    var problemDescription by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Map Picker Integration
    val mapPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data
            latitude = data?.getDoubleExtra("latitude", 0.0)?.toString() ?: ""
            longitude = data?.getDoubleExtra("longitude", 0.0)?.toString() ?: ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Report Garbage",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color(0xFF1E88E5)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Address Input
        TextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(containerColor = Color.White)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Image Preview
        if (capturedImageUri != Uri.EMPTY) {
            Image(
                painter = rememberImagePainter(capturedImageUri),
                contentDescription = "Captured Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.LightGray)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Problem Description Input
        TextField(
            value = problemDescription,
            onValueChange = { problemDescription = it },
            label = { Text("Problem Description") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(containerColor = Color.White)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Latitude & Longitude Inputs
        TextField(
            value = latitude,
            onValueChange = { latitude = it },
            label = { Text("Latitude") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(containerColor = Color.White),
            enabled = false
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = longitude,
            onValueChange = { longitude = it },
            label = { Text("Longitude") },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.textFieldColors(containerColor = Color.White),
            enabled = false
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Map Picker Button
        Button(
            onClick = {
                val intent = Intent(context, MapPickerActivity::class.java)
                mapPickerLauncher.launch(intent)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
        ) {
            Text(text = "Pick Location", color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save and Discard Buttons
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    val geolocation = "Latitude: $latitude, Longitude: $longitude"
                    saveReportToFirestore(context, capturedImageUri, address, problemDescription, geolocation) { success ->
                        if (success) {
                            navController.popBackStack()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
            ) {
                Text(text = "Save", color = Color.White)
            }

            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
            ) {
                Text(text = "Discard", color = Color.White)
            }
        }
    }
}

fun saveReportToFirestore(
    context: Context,
    imageUri: Uri,
    address: String,
    description: String,
    geolocation: String,
    onComplete: (Boolean) -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance().reference.child("garbage_reports/${System.currentTimeMillis()}.jpg")

    val userId = getAuthenticatedUserId()
    val userEmail = getAuthenticatedUserEmail()

    val uploadTask = storage.putFile(imageUri)
    uploadTask.continueWithTask { task ->
        if (!task.isSuccessful) {
            task.exception?.let { throw it }
        }
        storage.downloadUrl
    }.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val downloadUri = task.result

            val report = Report(
                imageUrl = downloadUri.toString(),
                address = address,
                description = description,
                geolocation = geolocation,
                userId = userId,
                useridd = userEmail
            )

            firestore.collection("garbage_reports")
                .add(report)
                .addOnSuccessListener {
                    Toast.makeText(context, "Report saved successfully", Toast.LENGTH_SHORT).show()

                    // Fetch reports and update clusters
                    firestore.collection("garbage_reports")
                        .get()
                        .addOnSuccessListener { snapshot ->
                            val reports = snapshot.documents.mapNotNull { doc ->
                                doc.getString("geolocation")?.let { parseGeolocation(it) }
                            }

                            val clusterManager = GarbageClusterManager()
                            val clusters = clusterManager.clusterReports(reports, radius = 0.05) // Adjust radius as needed

                            saveClustersToFirestore(clusters) { clusterSaveSuccess ->
                                if (clusterSaveSuccess) {
                                    Toast.makeText(context, "Clusters updated successfully", Toast.LENGTH_SHORT).show()
                                    onComplete(true)
                                } else {
                                    Toast.makeText(context, "Failed to update clusters", Toast.LENGTH_SHORT).show()
                                    onComplete(false)
                                }
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Failed to fetch reports", Toast.LENGTH_SHORT).show()
                            onComplete(false)
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to save report", Toast.LENGTH_SHORT).show()
                    onComplete(false)
                }
        } else {
            Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
            onComplete(false)
        }
    }
}

fun getAuthenticatedUserId(): String {
    val auth = FirebaseAuth.getInstance()
    return auth.currentUser?.uid ?: "user123" // Dummy user ID for testing
}

fun getAuthenticatedUserEmail(): String {
    val auth = FirebaseAuth.getInstance()
    return auth.currentUser?.email ?: "user123@gmail.com" // Dummy email for testing
}

fun parseGeolocation(geolocation: String): LatLng? {
    val parts = geolocation.split(", ")
    val lat = parts.getOrNull(0)?.split(":")?.get(1)?.toDoubleOrNull()
    val lng = parts.getOrNull(1)?.split(":")?.get(1)?.toDoubleOrNull()
    return if (lat != null && lng != null) LatLng(lat, lng) else null
}
