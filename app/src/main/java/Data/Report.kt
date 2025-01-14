package Data
data class Report(
    val imageUrl: String = "",
    val address: String = "",
    val description: String = "",
    val geolocation: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    var userId: String = "",
    var useridd  :String = ""

    )
