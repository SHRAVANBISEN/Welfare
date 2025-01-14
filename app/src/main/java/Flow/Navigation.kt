package Flow

import DonationViewModel
import UI.DefaultScreen
import UI.DonationListScreen
import UI.GarbageReportFormScreen
import UI.LoginScreen
import UI.ReportGarbageScreen
import UI.SelectImage
import UI.SignupScreen
import ViewModels.AuthViewModel
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.garbageapp.ui.GarbageReportScreen
import eu.tutorials.mywishlistapp.DonationScreen

@Composable
fun NavigationGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    viewModel: DonationViewModel = viewModel(),



    ) {
    val context = navController.context

    NavHost(navController = navController, startDestination = Screen.LoginScreen.route) {
        composable(Screen.DefaultScreen.route) {

            DefaultScreen(navController ,authViewModel)
        }


        composable(Screen.ngodefault.route) {

            DonationListScreen(navController = navController ,authViewModel)
        }
        composable(Screen.muncipal.route) {

GarbageReportScreen(navController = navController ,authViewModel)      }


        composable(Screen.LoginScreen.route) {
            val context = LocalContext.current
            LoginScreen(
                authViewModel = authViewModel,
                context = context,
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignupScreen.route) {
                        popUpTo(Screen.LoginScreen.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onRoleXNavigate = {
                    navController.navigate(Screen.DefaultScreen.route) {
                        popUpTo(Screen.LoginScreen.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onRoleYNavigate = {
                    navController.navigate(Screen.muncipal.route) {
                        popUpTo(Screen.LoginScreen.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onRoleZNavigate = {
                    navController.navigate(Screen.ngodefault.route) {
                        popUpTo(Screen.LoginScreen.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = "${Screen.FormScreen.route}?imageUri={imageUri}",
            arguments = listOf(navArgument("imageUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val imageUriString = backStackEntry.arguments?.getString("imageUri")
            val imageUri = imageUriString?.let { Uri.parse(it) } ?: Uri.EMPTY
            GarbageReportFormScreen(navController, capturedImageUri = imageUri)
        }

        composable(Screen.SignupScreen.route) {
            SignupScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = { navController.navigate(Screen.LoginScreen.route)
                {
                    popUpTo(Screen.SignupScreen.route) { inclusive = true }
                }}
            )
        }
        composable(Screen.CaptureImageScreen.route) {

            ReportGarbageScreen(navController )
        }
        composable(Screen.donation.route) { backStackEntry ->
            // Get the email argument from the navigation backstack if it's passed
            val email = backStackEntry.arguments?.getString("email") ?: ""



            // Pass the email, viewModel, and navController to DonationScreen
            DonationScreen(
                viewModel = DonationViewModel(),

                navController = navController
            )
        }
        composable(Screen.SelectImageScreen.route) {

            SelectImage(navController)
        }


    }
}