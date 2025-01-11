package ViewModels

import Data.User
import Extras.Injection
import Repository.UserRepository
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import Extras.Result
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository: UserRepository = UserRepository(
        FirebaseAuth.getInstance(),
        Injection.instance()
    )

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _authResult = MutableLiveData<Result<Boolean>?>()
    val authResult: LiveData<Result<Boolean>?> get() = _authResult

    private val _currentUser = MutableLiveData<Result<User>>()
    val currentUser: LiveData<Result<User>> get() = _currentUser

    private val _isUserLoggedIn = MutableLiveData<Boolean>()
    val isUserLoggedIn: LiveData<Boolean> get() = _isUserLoggedIn

    private val _isPrincipalUser = MutableLiveData<Boolean>()
    val isPrincipalUser: LiveData<Boolean> get() = _isPrincipalUser

    init {
        checkUserLoggedIn()
    }

    // Fetches current user data
    fun getCurrentUser() {
        viewModelScope.launch {
            _currentUser.value = userRepository.getCurrentUser()
        }
    }

    // Handles user login
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                setLoading(true)
                val result = userRepository.login(email, password)
                if (result is Result.Success) {
                    _isUserLoggedIn.value = true
                    val isPrincipal = isPrincipalUser(email, password)
                    setPrincipalUserStatus(isPrincipal)
                }
                _authResult.value = result
            } catch (e: Exception) {
                _authResult.value = Result.Error(e, "Login failed. Please try again.")
                Log.e(TAG, "Login failed", e)
            } finally {
                setLoading(false)
            }
        }
    }

    // Clears auth result to avoid stale data
    fun clearAuthResult() {
        _authResult.value = null
    }

    fun signUp(
        email: String,
        password: String,
        fullName: String,
        address: String,
        pinCode: String,
        city: String,
        district: String,
        role: String
    ) {
        viewModelScope.launch {
            setLoading(true)
            val result = userRepository.signUp(
                email = email,
                password = password,
                fullName = fullName,
                address = address,
                pinCode = pinCode,
                city = city,
                district = district,
                role = role
            )
            _authResult.value = result
            setLoading(false)
        }
    }


    // Checks if the user is already logged in
    fun checkUserLoggedIn() {
        val isLoggedIn = userRepository.isUserLoggedIn()
        _isUserLoggedIn.value = isLoggedIn
        _isPrincipalUser.value = if (isLoggedIn) {
            isPrincipalUserOnLaunch()
        } else {
            false
        }
    }

    // Logs out the current user
    fun logout() {
        userRepository.logout()
        _isUserLoggedIn.value = false
        setPrincipalUserStatus(false) // Clear principal status on logout
    }
    fun clearAllAuthStates() {
        _authResult.value = null
        _isUserLoggedIn.value = false
    }
    // Determines if the current user is a principal user (e.g., municipal corporation user)
    private fun setPrincipalUserStatus(isPrincipal: Boolean) {
        _isPrincipalUser.value = isPrincipal
        with(sharedPreferences.edit()) {
            putBoolean("isPrincipalUser", isPrincipal)
            apply()
        }
    }

    fun isPrincipalUser(email: String, password: String): Boolean {
        return email == principalUserEmail && password == principalUserPassword
    }

    // Retrieves principal user status on app launch
    fun isPrincipalUserOnLaunch(): Boolean {
        return sharedPreferences.getBoolean("isPrincipalUser", false)
    }

    // Updates the loading state
    private fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    companion object {
        private const val TAG = "AuthViewModel"

        // Credentials for the municipal corporation (or admin user)
        private const val principalUserEmail = "admin@municipal.com"
        private const val principalUserPassword = "admin123"
    }
    fun getCurrentUserRole(): String? {
        val user = FirebaseAuth.getInstance().currentUser
        return if (user != null) {
            val userEmail = user.email ?: return null
            // Fetch role from Firebase (assumes role is saved in user collection)
            userRepository.getUserRole(userEmail)
        } else null
    }

    fun signInWithGoogle(credential: com.google.firebase.auth.AuthCredential) {
        viewModelScope.launch {
            try {
                setLoading(true)
                val authResult = userRepository.signInWithGoogle(credential)
                if (authResult is Result.Success) {
                    _isUserLoggedIn.value = true
                    _authResult.value = authResult
                } else {
                    _authResult.value = Result.Error(Exception("Google Sign-In failed"), "Authentication failed.")
                }
            } catch (e: Exception) {
                _authResult.value = Result.Error(e, "Google Sign-In failed. Please try again.")
                Log.e(TAG, "Google Sign-In failed", e)
            } finally {
                setLoading(false)
            }
        }
    }

}

