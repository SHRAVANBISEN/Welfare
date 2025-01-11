package UI

import Flow.Screen
import ViewModels.AuthViewModel
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material.*

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.example.welfare.R

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "UnusedMaterialScaffoldPaddingParameter")
@Composable

fun DefaultScreen(navController: NavController, authViewModel: AuthViewModel,) {
    val systemUiController = rememberSystemUiController()
    val statusBarColor = Color.Black
    LaunchedEffect(true) {
        systemUiController.setStatusBarColor(
            color = statusBarColor,
            darkIcons = true
        )
    }
    val scaffoldState = rememberScaffoldState()

    Scaffold( scaffoldState = scaffoldState,
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorResource(id = R.color.black)
                ),
                title = { Text(text = "lbs Problems", color = colorResource(id = R.color.white)) },
                actions = {
                    IconButton(onClick = {
                        authViewModel.logout()
                        navController.navigate(Screen.DefaultScreen.route) {
                            popUpTo(Screen.DefaultScreen.route) { inclusive = true }
                            popUpTo(0) // Ensure to pop to the root
                        }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
                    }
                }
            )
        }) {

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Button(onClick = { navController.navigate(Screen.CaptureImageScreen.route)}) {

                Text(text = " image capture ")

            }

            Button(onClick = {navController.navigate(Screen.SelectImageScreen.route) }) {
                Text(text = "image selection ")


            }
            Button(onClick = {navController.navigate(Screen.donation.route) }) {
                Text(text = "donation")


            }
        }
    }

}