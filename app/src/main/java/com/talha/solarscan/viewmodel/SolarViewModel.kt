package com.talha.solarscan.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.talha.solarscan.data.local.DatabaseHelper
import com.talha.solarscan.data.local.SolarRecommendation
import com.talha.solarscan.repository.SolarRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SolarViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SolarRepository()
    private val dbHelper = DatabaseHelper.getInstance(application)

    // Event LiveData - ONLY for navigation (single-use)
    private val _navigationEvent = MutableLiveData<Event<Long>>() // Now contains bill ID
    val navigationEvent: LiveData<Event<Long>> = _navigationEvent

    private val _loadingLiveData = MutableLiveData<Boolean>()
    val loadingLiveData: LiveData<Boolean> = _loadingLiveData

    private val _errorLiveData = MutableLiveData<String?>()
    val errorLiveData: LiveData<String?> = _errorLiveData

    fun fetchRecommendation(billText: String, budget: Int? = null) {
        viewModelScope.launch {
            _loadingLiveData.postValue(true)

            val result = repository.fetchRecommendation(billText, budget)

            result.onSuccess { response ->
                // Save to database
                val billId = withContext(Dispatchers.IO) {
                    // Insert bill
                    val bill = com.talha.solarscan.bill.Bill(
                        id = 0,
                        units = response.parsedFields.unitsKWh ?: 0,
                        cost = response.parsedFields.totalCost ?: 0,
                        billingDate = response.parsedFields.billingDate,
                        createdAt = System.currentTimeMillis()
                    )
                    val insertedBillId = dbHelper.insertBill(bill)

                    // Insert recommendation
                    val recommendation = SolarRecommendation(
                        suggestedSystemKw = response.recommendation.suggestedSystemKw,
                        estMonthlyProductionKwh = response.recommendation.estMonthlyProductionKwh,
                        estMonthlySavings = response.recommendation.estMonthlySavings,
                        approxInstallCost = response.recommendation.approxInstallCost,
                        paybackYears = response.recommendation.paybackYears,
                        co2ReductionTonsPerYear = response.recommendation.co2ReductionTonsPerYear,
                        percentOffset = response.recommendation.percentOffset,
                        notes = response.recommendation.notes,
                        assumptions = response.assumptions,
                        unitsKWh = response.parsedFields.unitsKWh ?: 0
                    )
                    dbHelper.insertRecommendation(insertedBillId, recommendation)

                    insertedBillId
                }

                // Trigger navigation with bill ID
                _navigationEvent.postValue(Event(billId))
                _errorLiveData.postValue(null)
            }

            result.onFailure { exception ->
                _errorLiveData.postValue(exception.message ?: "Unknown error")
            }

            _loadingLiveData.postValue(false)
        }
    }

    // Helper method to check if a bill has recommendations
    fun hasRecommendation(billId: Long): Boolean {
        return dbHelper.hasRecommendation(billId)
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