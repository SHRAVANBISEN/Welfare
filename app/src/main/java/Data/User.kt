package Data

data class User(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val address: String = "",
    val pinCode: String = "",
    val city: String = "",
    val district: String = "",
    val role: String = "Citizen",
    val phoneNumber: String = "",
    val organizationName: String = "",
    val isVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val homeLatitude: Double? = null,
    val homeLongitude: Double? = null
)
