package com.talha.solarscan.repository

import android.content.Context
import com.talha.solarscan.data.local.Bill
import com.talha.solarscan.data.local.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BillRepository(context: Context) {

    private val dbHelper = DatabaseHelper.getInstance(context)

    suspend fun insertBill(bill: Bill): Long = withContext(Dispatchers.IO) {
        dbHelper.insertBill(bill)
    }

    suspend fun getLatestBill(): Bill? = withContext(Dispatchers.IO) {
        dbHelper.getLatestBill()
    }

    suspend fun getAllBills(): List<Bill> = withContext(Dispatchers.IO) {
        dbHelper.getAllBills()
    }
}