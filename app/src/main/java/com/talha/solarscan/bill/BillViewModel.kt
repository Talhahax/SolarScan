package com.talha.solarscan.bill

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.talha.solarscan.bill.BillRepository
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
        Log.d("BillViewModel", "========== SAVE BILL ==========")
        Log.d("BillViewModel", "Bill to save: $bill")
        viewModelScope.launch {
            try {
                val id = repository.insertBill(bill)
                Log.d("BillViewModel", "✅ Bill saved with ID: $id")
                _savingStatus.postValue(id > 0)

                Log.d("BillViewModel", "Loading latest bill...")
                loadLatestBill()

                Log.d("BillViewModel", "Loading all bills...")
                loadAllBills()
            } catch (e: Exception) {
                Log.e("BillViewModel", "❌ Error saving bill", e)
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
        Log.d("BillViewModel", "========== LOAD ALL BILLS ==========")
        viewModelScope.launch {
            try {
                val bills = repository.getAllBills()
                Log.d("BillViewModel", "✅ Loaded ${bills.size} bills from repository")
                bills.forEachIndexed { index, bill ->
                    Log.d("BillViewModel", "Bill $index: units=${bill.units}, cost=${bill.cost}, date=${bill.billingDate}")
                }
                _allBillsLiveData.postValue(bills)
                Log.d("BillViewModel", "Posted bills to LiveData")
            } catch (e: Exception) {
                Log.e("BillViewModel", "❌ Error loading bills", e)
                _allBillsLiveData.postValue(emptyList())
            }
        }
    }
}