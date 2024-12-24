package Flow

import UI.DefaultScreen
import UI.LoginScreen
import UI.ReportGarbageScreen
import UI.SelectImage
import ViewModels.AuthViewModel
import ViewModels.ReportGarbageViewModel
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun NavigationGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,


    ) {
    val context = navController.context

    NavHost(navController = navController, startDestination = Screen.LoginScreen.route) {
        composable(Screen.DefaultScreen.route) {

            DefaultScreen(navController)
        }

        composable(Screen.LoginScreen.route) {
            val context = LocalContext.current
            LoginScreen(
                authViewModel = authViewModel,
                context = context,
                onNavigateToSignUp = { /*TODO*/ },
                onSignInSuccess = {   navController.navigate(Screen.DefaultScreen.route) {
                    popUpTo(Screen.LoginScreen.route) { inclusive = true }
                } }) {
                
            }
        }

        composable(Screen.CaptureImageScreen.route) {

            ReportGarbageScreen(navController )
        }
        composable(Screen.SelectImageScreen.route) {

            SelectImage(navController)
        }


    }
}