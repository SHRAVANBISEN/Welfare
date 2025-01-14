package Extras


import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.pow
import kotlin.math.sqrt

data class Cluster(var center: LatLng, val reports: MutableList<LatLng>)

class GarbageClusterManager {
    private val clusters = mutableListOf<Cluster>()

    fun clusterReports(reports: List<LatLng>, radius: Double): List<Cluster> {
        clusters.clear()

        reports.forEach { report ->
            var addedToCluster = false

            // Check if the report belongs to an existing cluster
            clusters.forEach { cluster ->
                if (distance(cluster.center, report) <= radius) {
                    cluster.reports.add(report)
                    updateClusterCenter(cluster)
                    addedToCluster = true
                }
            }

            // If not added, create a new cluster
            if (!addedToCluster) {
                clusters.add(Cluster(center = report, reports = mutableListOf(report)))
            }
        }

        return clusters
    }

    private fun distance(point1: LatLng, point2: LatLng): Double {
        val latDiff = point1.latitude - point2.latitude
        val lngDiff = point1.longitude - point2.longitude
        return sqrt(latDiff.pow(2) + lngDiff.pow(2))
    }

    private fun updateClusterCenter(cluster: Cluster) {
        val avgLat = cluster.reports.map { it.latitude }.average()
        val avgLng = cluster.reports.map { it.longitude }.average()
        cluster.center = LatLng(avgLat, avgLng)
    }
}
fun saveClustersToFirestore(clusters: List<Cluster>, onComplete: (Boolean) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    val clusterCollection = firestore.collection("garbage_clusters")

    clusterCollection.get().addOnSuccessListener { querySnapshot ->
        // Delete existing clusters
        querySnapshot.documents.forEach { it.reference.delete() }

        // Add new clusters
        clusters.forEach { cluster ->
            val clusterData = mapOf(
                "centerLat" to cluster.center.latitude,
                "centerLng" to cluster.center.longitude,
                "reportCount" to cluster.reports.size
            )
            clusterCollection.add(clusterData)
        }

        onComplete(true)
    }.addOnFailureListener {
        onComplete(false)
    }
}

fun listenForWarnings(userLatLng: LatLng, radius: Double, onWarning: (Boolean) -> Unit) {
    val firestore = FirebaseFirestore.getInstance()
    val clusterCollection = firestore.collection("garbage_clusters")

    clusterCollection.addSnapshotListener { snapshot, error ->
        if (error != null || snapshot == null) {
            onWarning(false)
            return@addSnapshotListener
        }

        var isInWarningZone = false
        for (document in snapshot.documents) {
            val centerLat = document.getDouble("centerLat") ?: continue
            val centerLng = document.getDouble("centerLng") ?: continue
            val reportCount = document.getLong("reportCount") ?: continue

            val center = LatLng(centerLat, centerLng)
            if (distance(userLatLng, center) <= radius && reportCount > 5) { // Adjust threshold
                isInWarningZone = true
                break
            }
        }

        onWarning(isInWarningZone)
    }
}

private fun distance(point1: LatLng, point2: LatLng): Double {
    val latDiff = point1.latitude - point2.latitude
    val lngDiff = point1.longitude - point2.longitude
    return sqrt(latDiff.pow(2) + lngDiff.pow(2))
}