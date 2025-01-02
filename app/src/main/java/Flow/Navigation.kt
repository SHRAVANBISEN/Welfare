package Flow

import UI.DefaultScreen
import UI.GarbageReportFormScreen
import UI.LoginScreen
import UI.ReportGarbageScreen
import UI.SelectImage
import ViewModels.AuthViewModel
import ViewModels.ReportGarbageViewModel
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

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
        composable(
            route = "${Screen.FormScreen.route}?imageUri={imageUri}",
            arguments = listOf(navArgument("imageUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val imageUriString = backStackEntry.arguments?.getString("imageUri")
            val imageUri = imageUriString?.let { Uri.parse(it) } ?: Uri.EMPTY
            GarbageReportFormScreen(navController, capturedImageUri = imageUri)
        }


        composable(Screen.CaptureImageScreen.route) {

            ReportGarbageScreen(navController )
        }
        composable(Screen.SelectImageScreen.route) {

            SelectImage(navController)
        }


    }
}