package com.example.garbageapp.ui

import Data.Report
import Flow.Screen
import ViewModels.AuthViewModel
import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.welfare.R
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun GarbageReportScreen(navController: NavController, authViewModel: AuthViewModel) {
    val reports = remember { mutableStateListOf<Report>() }
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    val scaffoldState = rememberScaffoldState()

    LaunchedEffect(Unit) {
        try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("garbage_reports")
                .get()
                .await()
            val fetchedReports = snapshot.documents.mapNotNull { it.toObject(Report::class.java) }
            reports.addAll(fetchedReports)
            isError = reports.isEmpty()
        } catch (e: Exception) {
            Log.e("GarbageReportScreen", "Error loading reports: ${e.message}")
            isError = true
        } finally {
            isLoading = false
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
            title = { androidx.compose.material3.Text(text = "Garbage Reports", color = colorResource(id = R.color.white)) },
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            }
            isError -> {
                Text(
                    "Failed to load reports or no reports available.",
                    color = Color.Gray,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(reports.size) { index ->
                        val report = reports[index]
                        SinglePostCard(report = report)
                    }
                }
            }
        }
    }
}

}

@Composable
fun SinglePostCard(report: Report) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(report.imageUrl),
                contentDescription = "Garbage Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )

            Text("Address:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(report.address, fontSize = 14.sp, color = Color.Gray)

            Text("Description:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(report.description, fontSize = 14.sp, color = Color.Gray)

            Text("Geolocation:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(report.geolocation, fontSize = 14.sp, color = Color.Gray)

            Text("Reported on:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(
                java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(report.timestamp),
                fontSize = 14.sp,
                color = Color.Gray
            )

            Text("User Details:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text("Email: ${report.useridd}", fontSize = 14.sp, color = Color.Gray)
            Text("ID: ${report.userId}", fontSize = 14.sp, color = Color.Gray)
        }
    }
}
