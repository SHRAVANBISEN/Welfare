package Repository


import Data.DetectionResult
import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.welfare.R
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.InputStream
import javax.inject.Inject

class GarbageRepository @Inject constructor(
    private val context: Context
) {

    suspend fun getAccessToken(): String? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream: InputStream = context.resources.openRawResource(R.raw.visionapi)
                val credentials = GoogleCredentials.fromStream(inputStream)
                    .createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))
                credentials.refreshIfExpired()
                credentials.accessToken.tokenValue
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun analyzeImage(uri: Uri, accessToken: String): DetectionResult {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()

                val imageBase64 = Base64.encodeToString(
                    context.contentResolver.openInputStream(uri)?.readBytes(),
                    Base64.NO_WRAP
                )

                val requestBody = """
                    {
                      "requests": [
                        {
                          "image": { "content": "$imageBase64" },
                          "features": [{ "type": "LABEL_DETECTION" }]
                        }
                      ]
                    }
                """.trimIndent()

                val request = Request.Builder()
                    .url("https://vision.googleapis.com/v1/images:annotate")
                    .addHeader("Authorization", "Bearer $accessToken")
                    .post(RequestBody.create("application/json".toMediaType(), requestBody))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                val labels = JSONArray(
                    JSONObject(responseBody).getJSONArray("responses")
                        .getJSONObject(0).getJSONArray("labelAnnotations").toString()
                )

                val garbageKeywords = listOf("garbage", "trash", "waste", "pollution", "dump", "landfill")
                var garbageCount = 0

                for (i in 0 until labels.length()) {
                    val description = labels.getJSONObject(i).getString("description").lowercase()
                    if (garbageKeywords.any { description.contains(it) }) {
                        garbageCount++
                    }
                }

                // Detection Logic: Require multiple garbage-related labels
                if (garbageCount >= 3) {
                    DetectionResult(true, "Garbage Detected in the Area!")
                } else {
                    DetectionResult(false, "No Significant Garbage Detected.")
                }

            } catch (e: Exception) {
                e.printStackTrace()
                DetectionResult(false, "Error: Unable to process image.")
            }
        }
    }

    fun uploadImageToCloud(uri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference.child("garbage_images/${System.currentTimeMillis()}.jpg")
        storageRef.putFile(uri).addOnSuccessListener {
            Log.d("FirebaseStorage", "Image uploaded successfully.")
        }.addOnFailureListener {
            Log.e("FirebaseStorage", "Failed to upload image: ${it.message}")
        }
    }
}
