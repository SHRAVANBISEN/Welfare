import Data.Donation
import Repository.UserRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DonationViewModel(private val repository: UserRepository) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _donations = MutableStateFlow<List<Donation>>(emptyList())
    val donations: StateFlow<List<Donation>> get() = _donations

    private val _donationTitleState = MutableStateFlow("")
    val donationTitleState: StateFlow<String> get() = _donationTitleState

    private val _donationDescriptionState = MutableStateFlow("")
    val donationDescriptionState: StateFlow<String> get() = _donationDescriptionState

    private val _selectedLocation = MutableStateFlow<GeoPoint?>(null)
    val selectedLocation: StateFlow<GeoPoint?> get() = _selectedLocation

    constructor() : this(UserRepository(FirebaseAuth.getInstance(), FirebaseFirestore.getInstance())) {
        // Initialize repository with default Firebase instances if needed
    }

    fun loadDonations() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("donations")
            .whereEqualTo("useridd", userId)
            .get()
            .addOnSuccessListener { documents ->
                val donationList = documents.map { doc -> doc.toObject(Donation::class.java) }
                _donations.value = donationList
            }
    }

    fun loadDonationsByHostel(hostelName: String) {
        viewModelScope.launch {
            val userDocs = db.collection("users")
                .whereEqualTo("hostelName", hostelName)
                .get()
                .await()

            val userIds = userDocs.documents.mapNotNull { it.id }

            if (userIds.isNotEmpty()) {
                val donationDocs = db.collection("donations")
                    .whereIn("userId", userIds)
                    .get()
                    .await()

                val donationList = donationDocs.documents.mapNotNull { it.toObject(Donation::class.java) }
                _donations.value = donationList
            } else {
                _donations.value = emptyList()
            }
        }
    }

    // Add donation function
    fun addDonation(
        userEmail: String,
        userId: String,
        useridd: String,
        items: List<String>,
        address: String,
        location: GeoPoint?,
        pickupSchedule: String
    ) {
        val donation = Donation(
            userEmail = userEmail,
            items = items,
            address = address,
            location = location,
            pickupSchedule = pickupSchedule,
            userId = userId,
            useridd = useridd,
            createdAt = System.currentTimeMillis() // Timestamp for creation
        )

        db.collection("donations")
            .add(donation)
            .addOnSuccessListener {
                // Handle success, e.g., notify the user or clear input fields
            }
            .addOnFailureListener { exception ->
                // Handle failure, e.g., log the error or notify the user
            }
    }

    fun deleteDonation(donation: Donation) {
        db.collection("donations").document(donation.id)
            .delete()
            .addOnSuccessListener {
                loadDonations()
            }
    }

    // Update donation title
    fun onDonationTitleChanged(newTitle: String) {
        _donationTitleState.value = newTitle
    }

    // Update donation description
    fun onDonationDescriptionChanged(newDescription: String) {
        _donationDescriptionState.value = newDescription
    }

    // Update selected location
    fun updateSelectedLocation(geoPoint: GeoPoint) {
        _selectedLocation.value = geoPoint
    }
}
