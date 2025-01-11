package Repository

import Data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import Extras.Result
import android.util.Log

class UserRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    // Sign up method for new users
    suspend fun signUp(
        email: String,
        password: String,
        fullName: String,
        address: String,
        pinCode: String,
        city: String,
        district: String,
        role: String = "general_user"
    ): Result<Boolean> = try {
        // Create a new user in Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password).await()

        // Create a User object with the specified role
        val user = User(
            id = email, // Use email as the document ID
            fullName = fullName,
            email = email,
            address = address,
            pinCode = pinCode,
            city = city,
            district = district,
            role = role,
            createdAt = System.currentTimeMillis()
        )

        // Save the user to the main "users" collection
        saveUserToFirestore(user)

        // Save the user to a role-specific collection
        saveUserToRoleSpecificCollection(user)

        Result.Success(true)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // Save the user data to the general "users" collection
    private suspend fun saveUserToFirestore(user: User) {
        firestore.collection("users").document(user.email).set(user).await()
    }

    // Save the user data to a role-specific collection
    private suspend fun saveUserToRoleSpecificCollection(user: User) {
        val roleCollection = when (user.role) {
            "Citizen" -> "citizens"
            "Municipal Corporation" -> "municipal_officials"
            "NGO" -> "ngos"
            else -> "others"
        }
        firestore.collection(roleCollection).document(user.email).set(user).await()
    }

    // Login method for existing users
    suspend fun login(email: String, password: String): Result<Boolean> = try {
        auth.signInWithEmailAndPassword(email, password).await()
        Result.Success(true)
    } catch (e: Exception) {
        Result.Error(e)
    }

    // Retrieve the currently authenticated user
    suspend fun getCurrentUser(): Result<User> = try {
        val email = auth.currentUser?.email ?: throw Exception("User not authenticated")
        val userDocument = firestore.collection("users").document(email).get().await()
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

        // Update in role-specific collection
        val userDocument = firestore.collection("users").document(userId).get().await()
        val user = userDocument.toObject(User::class.java)
        if (user != null) {
            saveUserToRoleSpecificCollection(user.copy(role = newRole))
        }
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
        val querySnapshot = firestore.collection("municipal_officials").get().await()
        val users = querySnapshot.toObjects(User::class.java)
        Result.Success(users)
    } catch (e: Exception) {
        Result.Error(e)
    }
    fun getUserRole(email: String): String? {
        // Query Firebase Database for the user's role
        val userDoc = FirebaseFirestore.getInstance()
            .collection("users")
            .document(email)

        var role: String? = null
        userDoc.get().addOnSuccessListener { document ->
            if (document != null) {
                role = document.getString("role")
            }
        }.addOnFailureListener { exception ->
            Log.e("UserRepository", "Error getting user role: ", exception)
        }
        return role
    }

    // Sign in with Google
    suspend fun signInWithGoogle(credential: com.google.firebase.auth.AuthCredential): Result<Boolean> {
        return try {
            auth.signInWithCredential(credential).await()
            Result.Success(true)
        } catch (e: Exception) {
            Result.Error(e, "Google Sign-In failed")
        }
    }
}
