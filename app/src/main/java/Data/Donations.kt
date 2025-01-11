package Data

import com.google.firebase.firestore.GeoPoint

data class Donation(
    val id: String = "", // Unique ID for the donation
    val userEmail: String = "", // Email of the donor
    val items: List<String> = emptyList(), // List of items to donate
    val address: String = "", // Address provided by the user
    val location: GeoPoint? = null, // GeoPoint for location
    val pickupSchedule: String = "", // Pickup schedule
    val createdAt: Long = System.currentTimeMillis() // Timestamp for donation creation
)
