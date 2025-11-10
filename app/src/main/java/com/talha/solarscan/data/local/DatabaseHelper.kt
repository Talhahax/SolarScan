package com.talha.solarscan.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.talha.solarscan.bill.Bill

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "energy_bills.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_BILLS = "bills"

        private const val COL_ID = "id"
        private const val COL_UNITS = "units"
        private const val COL_COST = "cost"
        private const val COL_BILLING_DATE = "billing_date"
        private const val COL_CREATED_AT = "created_at"

        @Volatile
        private var INSTANCE: DatabaseHelper? = null

        fun getInstance(context: Context): DatabaseHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DatabaseHelper(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_BILLS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_UNITS INTEGER NOT NULL,
                $COL_COST INTEGER NOT NULL,
                $COL_BILLING_DATE TEXT,
                $COL_CREATED_AT INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BILLS")
        onCreate(db)
    }

    fun insertBill(bill: Bill): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_UNITS, bill.units)
            put(COL_COST, bill.cost)
            put(COL_BILLING_DATE, bill.billingDate)
            put(COL_CREATED_AT, bill.createdAt)
        }
        return db.insert(TABLE_BILLS, null, values)
    }

    fun getLatestBill(): Bill? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_BILLS,
            null,
            null,
            null,
            null,
            null,
            "$COL_CREATED_AT DESC",
            "1"
        )

        return if (cursor.moveToFirst()) {
            Bill(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                units = cursor.getInt(cursor.getColumnIndexOrThrow(COL_UNITS)),
                cost = cursor.getInt(cursor.getColumnIndexOrThrow(COL_COST)),
                billingDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_BILLING_DATE)),
                createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED_AT))
            ).also { cursor.close() }
        } else {
            cursor.close()
            null
        }
    }

    fun getAllBills(): List<Bill> {
        val bills = mutableListOf<Bill>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_BILLS,
            null,
            null,
            null,
            null,
            null,
            "$COL_CREATED_AT DESC"
        )

        while (cursor.moveToNext()) {
            bills.add(
                Bill(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                    units = cursor.getInt(cursor.getColumnIndexOrThrow(COL_UNITS)),
                    cost = cursor.getInt(cursor.getColumnIndexOrThrow(COL_COST)),
                    billingDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_BILLING_DATE)),
                    createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_CREATED_AT))
                )
            )
        }
        cursor.close()
        return bills
    }

    fun deleteAllBills() {
        writableDatabase.delete(TABLE_BILLS, null, null)
    }
}