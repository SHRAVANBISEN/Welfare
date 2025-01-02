package Flow

sealed class Screen(val route: String) {
    object LoginScreen : Screen("loginscreen")
    object SignupScreen : Screen("signupscreen")
    object CaptureImageScreen : Screen("main_screen")
    object DefaultScreen : Screen("Default_screen")
    object SelectImageScreen : Screen("SelectImage_screen")
    object FormScreen : Screen("FormScreen")









}