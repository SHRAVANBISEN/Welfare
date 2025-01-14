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

    private val _userRole = MutableLiveData<String?>()
    val userRole: LiveData<String?> get() = _userRole

    private val _userHomeLocation = MutableLiveData<Result<Pair<Double, Double>>>()
    val userHomeLocation: LiveData<Result<Pair<Double, Double>>> get() = _userHomeLocation

    fun fetchUserHomeLocation() {
        viewModelScope.launch {
            _userHomeLocation.value = userRepository.getUserHomeLocation()
        }
    }

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
                    fetchUserRole(email) // Fetch the user's role upon successful login
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
        role: String,
        homeLatitude: Double?,
        homeLongitude: Double?
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
                role = role,
                homeLatitude = homeLatitude,
                homeLongitude = homeLongitude
            )
            _authResult.value = result
            setLoading(false)
        }
    }

    // Checks if the user is already logged in
    fun checkUserLoggedIn() {
        val isLoggedIn = userRepository.isUserLoggedIn()
        _isUserLoggedIn.value = isLoggedIn
        if (isLoggedIn) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            currentUser?.email?.let { fetchUserRole(it) }
        }
    }

    // Logs out the current user
    fun logout() {
        userRepository.logout()
        _isUserLoggedIn.value = false
        _userRole.value = null // Clear role on logout
    }

    fun clearAllAuthStates() {
        _authResult.value = null
        _isUserLoggedIn.value = false
        _userRole.value = null
    }
    private fun fetchUserRole(email: String) {
        viewModelScope.launch {
            try {
                setLoading(true)
                when (val roleResult = userRepository.getUserRole(email)) {
                    is Result.Success -> _userRole.value = roleResult.data
                    is Result.Error -> {
                        Log.e(TAG, "Error fetching user role: ${roleResult.message}")
                        _userRole.value = null
                    }

                    Result.Loading -> TODO()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception occurred while fetching user role", e)
                _userRole.value = null
            } finally {
                setLoading(false)
            }
        }
    }


    // Updates the loading state
    private fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    companion object {
        private const val TAG = "AuthViewModel"
    }

    fun signInWithGoogle(credential: com.google.firebase.auth.AuthCredential) {
        viewModelScope.launch {
            try {
                setLoading(true)
                val authResult = userRepository.signInWithGoogle(credential)
                if (authResult is Result.Success) {
                    _isUserLoggedIn.value = true
                    _authResult.value = authResult
                    val email = FirebaseAuth.getInstance().currentUser?.email
                    if (email != null) fetchUserRole(email)
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
