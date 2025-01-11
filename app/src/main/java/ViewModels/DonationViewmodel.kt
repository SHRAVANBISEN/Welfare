package ViewModels

import Data.Donation
import Repository.DonationsRepository
import Repository.GarbageRepository
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DonationViewModel(private val context: Context) : ViewModel() {
    private val repository = DonationsRepository(context)

    private val _donationState = MutableStateFlow<DonationState>(DonationState.Idle)
    val donationState: StateFlow<DonationState> = _donationState

    fun saveDonation(email: String, donation: Donation) {
        viewModelScope.launch {
            _donationState.value = DonationState.Loading
            val success = repository.saveDonation(email, donation)
            _donationState.value = if (success) DonationState.Success else DonationState.Error
        }
    }
}

sealed class DonationState {
    object Idle : DonationState()
    object Loading : DonationState()
    object Success : DonationState()
    object Error : DonationState()
}
