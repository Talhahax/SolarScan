package com.talha.solarscan.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talha.solarscan.data.remote.AnalyzeBillResponse
import com.talha.solarscan.repository.SolarRepository
import kotlinx.coroutines.launch

class SolarViewModel : ViewModel() {

    private val repository = SolarRepository()

    // Data LiveData - persists for DetailsFragment to display
    private val _recommendationLiveData = MutableLiveData<AnalyzeBillResponse?>()
    val recommendationLiveData: LiveData<AnalyzeBillResponse?> = _recommendationLiveData

    // Event LiveData - single-use for navigation
    private val _navigationEvent = MutableLiveData<Event<AnalyzeBillResponse>>()
    val navigationEvent: LiveData<Event<AnalyzeBillResponse>> = _navigationEvent

    private val _loadingLiveData = MutableLiveData<Boolean>()
    val loadingLiveData: LiveData<Boolean> = _loadingLiveData

    private val _errorLiveData = MutableLiveData<String?>()
    val errorLiveData: LiveData<String?> = _errorLiveData

    fun fetchRecommendation(billText: String, budget: Int? = null) {
        viewModelScope.launch {
            _loadingLiveData.postValue(true)

            val result = repository.fetchRecommendation(billText, budget)

            result.onSuccess { response ->
                // Store the data (for DetailsFragment)
                _recommendationLiveData.postValue(response)
                // Trigger navigation event (for ScannerFragment)
                _navigationEvent.postValue(Event(response))
                _errorLiveData.postValue(null)
            }

            result.onFailure { exception ->
                _recommendationLiveData.postValue(null)
                _errorLiveData.postValue(exception.message ?: "Unknown error")
            }

            _loadingLiveData.postValue(false)
        }
    }

    // Clear recommendation when starting fresh scan
    fun clearRecommendation() {
        _recommendationLiveData.value = null
    }
}

/**
 * Event wrapper class for single-use events like navigation
 * Prevents re-triggering when configuration changes or re-observing
 */
class Event<out T>(private val content: T) {

    private var hasBeenHandled = false

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}