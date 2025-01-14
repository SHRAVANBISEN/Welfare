package ViewModels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ReportGarbageViewModelFactory(private val context: Context) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReportGarbageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReportGarbageViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
