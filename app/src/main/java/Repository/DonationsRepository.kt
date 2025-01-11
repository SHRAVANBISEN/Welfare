package Repository

import Data.Donation
import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DonationsRepository @Inject constructor(
    private val context: Context
) {
    // Proper initialization of FirebaseFirestore
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Function to save donation data
    suspend fun saveDonation(email: String, donation: Donation): Boolean {
        return try {
            val documentRef = firestore.collection("donations").document(email)
            documentRef.collection("items").add(donation).await() // Assuming 'await()' is being used with coroutines
            true
        } catch (e: Exception) {
            false
        }
    }
}
