package com.talha.solarscan.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.talha.solarscan.data.local.Bill
import com.talha.solarscan.repository.BillRepository
import kotlinx.coroutines.launch

class BillViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BillRepository(application)

    private val _latestBillLiveData = MutableLiveData<Bill?>()
    val latestBillLiveData: LiveData<Bill?> = _latestBillLiveData

    private val _allBillsLiveData = MutableLiveData<List<Bill>>()
    val allBillsLiveData: LiveData<List<Bill>> = _allBillsLiveData

    private val _savingStatus = MutableLiveData<Boolean>()
    val savingStatus: LiveData<Boolean> = _savingStatus

    fun saveBill(bill: Bill) {
        viewModelScope.launch {
            try {
                val id = repository.insertBill(bill)
                _savingStatus.postValue(id > 0)
                loadLatestBill()
            } catch (e: Exception) {
                _savingStatus.postValue(false)
            }
        }
    }

    fun loadLatestBill() {
        viewModelScope.launch {
            try {
                val bill = repository.getLatestBill()
                _latestBillLiveData.postValue(bill)
            } catch (e: Exception) {
                _latestBillLiveData.postValue(null)
            }
        }
    }

    fun loadAllBills() {
        viewModelScope.launch {
            try {
                val bills = repository.getAllBills()
                _allBillsLiveData.postValue(bills)
            } catch (e: Exception) {
                _allBillsLiveData.postValue(emptyList())
            }
        }
    }
}