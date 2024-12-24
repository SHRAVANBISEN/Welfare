package Data


data class User(
    val id: String = "", // Unique identifier for the user (e.g., Firebase UID)
    val firstName: String = "",
    val lastName: String = "", // Optional field for full name support
    val email: String = "",
    var role: String = "general_user", // User role: "general_user" or "municipal_official"
    var isVerified: Boolean = false, // Whether the user's role is verified (e.g., for municipal officials)
    var phoneNumber: String = "", // Optional field for phone verification
    var organizationName: String = "", // For municipal officials (e.g., department name)
    var profileImageUrl: String = "", // URL for the user's profile image
    val createdAt: Long = System.currentTimeMillis(), // Timestamp for user registration
    var lastLoginAt: Long = 0L, // Timestamp for the last login
    val reportsSubmitted: Int = 0, // Number of reports submitted by the user
    val assignedReports: Int = 0 // Number of reports assigned to municipal officials
)
