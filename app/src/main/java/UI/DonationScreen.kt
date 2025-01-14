package eu.tutorials.mywishlistapp

import DonationViewModel
import Flow.Screen
import MapApi.MapPickerActivity
import ViewModels.AuthViewModel
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DonationScreen(
    navController: NavController,
    viewModel: DonationViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val systemUiController = rememberSystemUiController()
    val statusBarColor = Color(0xFF008577)
    LaunchedEffect(true) {
        systemUiController.setStatusBarColor(
            color = statusBarColor,
            darkIcons = false
        )
    }

    val donationTitleState by viewModel.donationTitleState.collectAsState()
    val donationDescriptionState by viewModel.donationDescriptionState.collectAsState()
    val items = remember { mutableStateListOf<String>() }
    var newItemText by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var selectedCoordinatesText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val activity = LocalContext.current as Activity

    // Create a launcher for the MapPickerActivity
    val locationPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val latitude = result.data?.getDoubleExtra("latitude", 0.0) ?: 0.0
            val longitude = result.data?.getDoubleExtra("longitude", 0.0) ?: 0.0
            selectedLocation = GeoPoint(latitude, longitude)
            selectedCoordinatesText = "Lat: $latitude, Lng: $longitude"
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Add Donation",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = statusBarColor
                )
            )
        },
        containerColor = Color(0xFFF1F1F1)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = donationTitleState,
                onValueChange = { viewModel.onDonationTitleChanged(it) },
                label = { Text("Donation Title") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = statusBarColor,
                    cursorColor = statusBarColor
                )
            )

            OutlinedTextField(
                value = donationDescriptionState,
                onValueChange = { viewModel.onDonationDescriptionChanged(it) },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = statusBarColor,
                    cursorColor = statusBarColor
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newItemText,
                    onValueChange = { newItemText = it },
                    label = { Text("Add Item") },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = statusBarColor,
                        cursorColor = statusBarColor
                    )
                )
                Button(
                    onClick = {
                        if (newItemText.isNotBlank()) {
                            items.add(newItemText)
                            newItemText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = statusBarColor),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Add", color = Color.White)
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                items.forEach { item ->
                    Text(
                        text = "• $item",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 8.dp),
                        color = Color.DarkGray
                    )
                }
            }

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = statusBarColor,
                    cursorColor = statusBarColor
                )
            )

            OutlinedTextField(
                value = selectedCoordinatesText,
                onValueChange = {},
                label = { Text("Selected Coordinates") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = statusBarColor,
                    cursorColor = statusBarColor
                ),
                readOnly = true
            )
 Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val intent = Intent(activity, MapPickerActivity::class.java)
                    locationPickerLauncher.launch(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Pick Location", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (donationTitleState.isNotBlank() && donationDescriptionState.isNotBlank()) {
                        val userId = getUserIdFromAuth()
                        val useridd = getUserIdFromAuthh()

                        if (selectedLocation == null) {
                            Toast.makeText(
                                navController.context,
                                "Please select a location",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }

                        viewModel.addDonation(
                            userEmail = userId,
                            userId = userId,
                            useridd = useridd,
                            items = items,
                            address = address,
                            location = selectedLocation!!,
                            pickupSchedule = "2025-01-15 10:00 AM"
                        )

                        scope.launch {
                            navController.navigateUp()
                            Toast.makeText(
                                navController.context,
                                "Donation added successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            navController.context,
                            "Please fill all required fields",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = statusBarColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add Donation", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

fun getUserIdFromAuth(): String {
    val auth = FirebaseAuth.getInstance()
    return auth.currentUser?.email ?: "user123@gmail.com"
}

fun getUserIdFromAuthh(): String {
    val auth = FirebaseAuth.getInstance()
    return auth.currentUser?.uid ?: "user123"
}
