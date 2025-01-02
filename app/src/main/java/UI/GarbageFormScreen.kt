package UI

import Data.Report
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

@Composable
fun GarbageReportFormScreen(navController: NavController, capturedImageUri: Uri) {
    var address by remember { mutableStateOf("") }
    var problemDescription by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Fetch geolocation (mock or implement location fetching logic here)
    val geolocation = "Latitude: 37.7749, Longitude: -122.4194" // Example coordinates

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text("Report Garbage")

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth()
        )
        if (capturedImageUri != Uri.EMPTY) {
            Image(
                painter = rememberImagePainter(capturedImageUri),
                contentDescription = "Captured Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = problemDescription,
            onValueChange = { problemDescription = it },
            label = { Text("Problem Description") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Geolocation: $geolocation")

        Spacer(modifier = Modifier.height(16.dp))

        // Save and Discard Buttons
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = {
                saveReportToFirestore(context, capturedImageUri, address, problemDescription, geolocation)
                navController.popBackStack()
            }) {
                Text("Save")
            }
            Button(onClick = { navController.popBackStack() }) {
                Text("Discard")
            }
        }
    }
}
fun saveReportToFirestore(
    context: Context,
    imageUri: Uri,
    address: String,
    description: String,
    geolocation: String
) {
    val firestore = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance().reference.child("garbage_reports/${System.currentTimeMillis()}.jpg")

    // Upload image to Firebase Storage
    val uploadTask = storage.putFile(imageUri)
    uploadTask.continueWithTask { task ->
        if (!task.isSuccessful) {
            task.exception?.let { throw it }
        }
        storage.downloadUrl
    }.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val downloadUri = task.result

            // Create a Report instance
            val report = Report(
                imageUrl = downloadUri.toString(),
                address = address,
                description = description,
                geolocation = geolocation
            )

            // Save report to Firestore
            firestore.collection("garbage_reports")
                .add(report)
                .addOnSuccessListener {
                    Toast.makeText(context, "Report saved successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to save report", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
        }
    }
}
