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

    private val _recommendationLiveData = MutableLiveData<AnalyzeBillResponse?>()
    val recommendationLiveData: LiveData<AnalyzeBillResponse?> = _recommendationLiveData

    private val _loadingLiveData = MutableLiveData<Boolean>()
    val loadingLiveData: LiveData<Boolean> = _loadingLiveData

    private val _errorLiveData = MutableLiveData<String?>()
    val errorLiveData: LiveData<String?> = _errorLiveData

    fun fetchRecommendation(billText: String, budget: Int? = null) {
        viewModelScope.launch {
            _loadingLiveData.postValue(true)

            val result = repository.fetchRecommendation(billText, budget)

            result.onSuccess { response ->
                _recommendationLiveData.postValue(response)
                _errorLiveData.postValue(null)
            }

            result.onFailure { exception ->
                _recommendationLiveData.postValue(null)
                _errorLiveData.postValue(exception.message ?: "Unknown error")
            }

            _loadingLiveData.postValue(false)
        }
    }

    // Clear recommendation after navigation
    fun clearRecommendation() {
        _recommendationLiveData.value = null
    }
}