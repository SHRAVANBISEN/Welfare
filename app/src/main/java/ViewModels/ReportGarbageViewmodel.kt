package ViewModels


import Data.DetectionResult
import Repository.GarbageRepository
import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReportGarbageViewModel(private val context: Context) : ViewModel(){

    private val repository = GarbageRepository(context)

    private val _detectionResult = MutableStateFlow<DetectionResult?>(null)
    val detectionResult: StateFlow<DetectionResult?> = _detectionResult

    fun processImage(uri: Uri) {
        viewModelScope.launch {
            _detectionResult.value = DetectionResult(false, "Scanning...")

            val token = repository.getAccessToken()
            if (token != null) {
                val result = repository.analyzeImage(uri, token)
                _detectionResult.value = result

                if (result.isGarbageDetected) {
                    repository.uploadImageToCloud(uri)
                }
            } else {
                _detectionResult.value = DetectionResult(false, "Error: Unable to fetch token.")
            }
        }
    }
}
