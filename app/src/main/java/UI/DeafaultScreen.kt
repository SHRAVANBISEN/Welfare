package UI

import Flow.Screen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.google.api.DistributionOrBuilder

@Composable

fun DefaultScreen(navController: NavController) {
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
    }
}