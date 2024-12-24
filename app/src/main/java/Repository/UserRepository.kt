package Repository



import Data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import Extras.Result
class UserRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    // Sign up method for new users
    suspend fun signUp(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        role: String = "general_user",
        phoneNumber: String = "",
        organizationName: String = ""
    ): Result<Boolean> = try {
        // Create a new user in Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password).await()

        // Create a User object with the specified role
        val userId = auth.currentUser?.uid ?: throw Exception("User ID is null")
        val user = User(
            id = userId,
            firstName = firstName,
            lastName = lastName,
            email = email,
            role = role,
            phoneNumber = phoneNumber,
            organizationName = organizationName,
            createdAt = System.currentTimeMillis()
        )

        // Save the user to Firestore
        saveUserToFirestore(user)
        Result.Success(true)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // Login method for existing users
    suspend fun login(email: String, password: String): Result<Boolean> = try {
        auth.signInWithEmailAndPassword(email, password).await()
        Result.Success(true)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // Save the user data to Firestore
    private suspend fun saveUserToFirestore(user: User) {
        firestore.collection("users").document(user.id).set(user).await()
    }

    // Retrieve the currently authenticated user
    suspend fun getCurrentUser(): Result<User> = try {
        val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
        val userDocument = firestore.collection("users").document(userId).get().await()
        val user = userDocument.toObject(User::class.java)
        if (user != null) {
            Result.Success(user)
        } else {
            Result.Error(Exception("User data not found"))
        }
    } catch (e: Exception) {
        Result.Error(e)
    }

    // Check if the current user is logged in
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    // Log out the current user
    fun logout() {
        auth.signOut()
    }

    // Update user role (e.g., for verification purposes)
    suspend fun updateUserRole(userId: String, newRole: String): Result<Boolean> = try {
        firestore.collection("users").document(userId)
            .update("role", newRole).await()
        Result.Success(true)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // Update verification status for a municipal official
    suspend fun updateVerificationStatus(userId: String, isVerified: Boolean): Result<Boolean> = try {
        firestore.collection("users").document(userId)
            .update("isVerified", isVerified).await()
        Result.Success(true)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // Retrieve all general users (for admin or monitoring purposes)
    suspend fun getAllGeneralUsers(): Result<List<User>> = try {
        val querySnapshot = firestore.collection("users")
            .whereEqualTo("role", "general_user").get().await()
        val users = querySnapshot.toObjects(User::class.java)
        Result.Success(users)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // Retrieve all municipal officials (for admin or monitoring purposes)
    suspend fun getAllMunicipalOfficials(): Result<List<User>> = try {
        val querySnapshot = firestore.collection("users")
            .whereEqualTo("role", "municipal_official").get().await()
        val users = querySnapshot.toObjects(User::class.java)
        Result.Success(users)
    } catch (e: Exception) {
        Result.Error(e)
    }

    suspend fun signInWithGoogle(credential: com.google.firebase.auth.AuthCredential): Result<Boolean> {
        return try {
            val authResult = auth.signInWithCredential(credential).await()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e, "Google Sign-In failed")
        }
    }

}
