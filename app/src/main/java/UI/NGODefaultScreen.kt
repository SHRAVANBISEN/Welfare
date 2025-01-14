package UI

import Flow.Screen
import ViewModels.AuthViewModel
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.welfare.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.tasks.await

// Updated Donation data class
data class Donation(
    val id: String = "", // Firestore-generated ID
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val items: List<String> = emptyList(),
    val location: GeoPoint? = null,
    val pickupSchedule: String = "",
    val userEmail: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun DonationListScreen(navController: NavController, authViewModel: AuthViewModel) {
    val donations = remember { mutableStateListOf<Donation>() }
    val scaffoldState = rememberScaffoldState()

    LaunchedEffect(Unit) {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val snapshot = firestore.collection("donations").get().await()
            val fetchedDonations = snapshot.documents.mapNotNull { document ->
                try {
                    // Map Firestore document to Donation object
                    val donation = document.toObject(Donation::class.java)
                    donation?.copy(id = document.id) // Add the Firestore document ID
                } catch (e: Exception) {
                    println("Error mapping document ${document.id}: ${e.message}")
                    null
                }
            }
            donations.addAll(fetchedDonations)
        } catch (e: Exception) {
            println("Error fetching donations: ${e.message}")
        }
    }
Scaffold( scaffoldState = scaffoldState,
    topBar = {
        CenterAlignedTopAppBar(
            modifier = Modifier.fillMaxWidth(),
            scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = colorResource(id = R.color.black)
            ),
            title = { androidx.compose.material3.Text(text = "Donations", color = colorResource(id = R.color.white)) },
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
    }) {
    if (donations.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No Donations Found",
                fontSize = 18.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(donations) { donation ->
                DonationCard(donation)
            }
        }
    }
}
}


@Composable
fun DonationCard(donation: Donation) {
    Card(
        elevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Address: ${donation.address}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = "Created At: ${formatTimestamp(donation.createdAt)}",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "Items: ${donation.items.joinToString(", ")}",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp
            )
            if (donation.location != null) {
                Text(
                    text = "Location: Lat: ${donation.location.latitude}, Lng: ${donation.location.longitude}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp
                )
            } else {
                Text(
                    text = "Location: Not provided",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Text(
                text = "Pickup Schedule: ${donation.pickupSchedule}",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp
            )
            Text(
                text = "User Email: ${donation.userEmail}",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

// Helper function to format timestamp
fun formatTimestamp(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}
