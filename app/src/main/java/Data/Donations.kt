package Data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint

data class Donation(
    @DocumentId
    val id: String = "", // Firestore-generated unique ID
    val userEmail: String = "", // Email of the donor
    val items: List<String> = emptyList(), // List of items to donate
    val address: String = "", // Address provided by the user
    val location: GeoPoint? = null, // GeoPoint for the location
    val pickupSchedule: String = "", // Pickup schedule
    val userId: String = "", // Firebase Authentication email
    val useridd: String = "", // Firebase Authentication UID
    val createdAt: Long = System.currentTimeMillis() // Timestamp for creation
)
