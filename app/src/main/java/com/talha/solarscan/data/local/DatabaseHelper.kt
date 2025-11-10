package com.talha.solarscan.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.talha.solarscan.bill.Bill
import org.json.JSONArray

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "energy_bills.db"
        private const val DATABASE_VERSION = 2 // Incremented version

        // Bills table
        private const val TABLE_BILLS = "bills"
        private const val COL_ID = "id"
        private const val COL_UNITS = "units"
        private const val COL_COST = "cost"
        private const val COL_BILLING_DATE = "billing_date"
        private const val COL_CREATED_AT = "created_at"

        // Recommendations table
        private const val TABLE_RECOMMENDATIONS = "recommendations"
        private const val COL_REC_ID = "id"
        private const val COL_REC_BILL_ID = "bill_id"
        private const val COL_REC_SYSTEM_KW = "suggested_system_kw"
        private const val COL_REC_MONTHLY_PROD = "est_monthly_production_kwh"
        private const val COL_REC_MONTHLY_SAVINGS = "est_monthly_savings"
        private const val COL_REC_INSTALL_COST = "approx_install_cost"
        private const val COL_REC_PAYBACK_YEARS = "payback_years"
        private const val COL_REC_CO2_REDUCTION = "co2_reduction_tons_per_year"
        private const val COL_REC_PERCENT_OFFSET = "percent_offset"
        private const val COL_REC_NOTES = "notes" // JSON array as string
        private const val COL_REC_ASSUMPTIONS = "assumptions" // JSON array as string
        private const val COL_REC_TIMESTAMP = "created_at"

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
        // Create bills table
        val createBillsTable = """
            CREATE TABLE $TABLE_BILLS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_UNITS INTEGER NOT NULL,
                $COL_COST INTEGER NOT NULL,
                $COL_BILLING_DATE TEXT,
                $COL_CREATED_AT INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(createBillsTable)

        // Create recommendations table
        val createRecommendationsTable = """
            CREATE TABLE $TABLE_RECOMMENDATIONS (
                $COL_REC_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_REC_BILL_ID INTEGER NOT NULL UNIQUE,
                $COL_REC_SYSTEM_KW REAL NOT NULL,
                $COL_REC_MONTHLY_PROD INTEGER NOT NULL,
                $COL_REC_MONTHLY_SAVINGS INTEGER NOT NULL,
                $COL_REC_INSTALL_COST INTEGER NOT NULL,
                $COL_REC_PAYBACK_YEARS REAL NOT NULL,
                $COL_REC_CO2_REDUCTION REAL NOT NULL,
                $COL_REC_PERCENT_OFFSET INTEGER NOT NULL,
                $COL_REC_NOTES TEXT,
                $COL_REC_ASSUMPTIONS TEXT,
                $COL_REC_TIMESTAMP INTEGER NOT NULL,
                FOREIGN KEY($COL_REC_BILL_ID) REFERENCES $TABLE_BILLS($COL_ID) ON DELETE CASCADE
            )
        """.trimIndent()
        db.execSQL(createRecommendationsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Create recommendations table for existing users
            val createRecommendationsTable = """
                CREATE TABLE IF NOT EXISTS $TABLE_RECOMMENDATIONS (
                    $COL_REC_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COL_REC_BILL_ID INTEGER NOT NULL UNIQUE,
                    $COL_REC_SYSTEM_KW REAL NOT NULL,
                    $COL_REC_MONTHLY_PROD INTEGER NOT NULL,
                    $COL_REC_MONTHLY_SAVINGS INTEGER NOT NULL,
                    $COL_REC_INSTALL_COST INTEGER NOT NULL,
                    $COL_REC_PAYBACK_YEARS REAL NOT NULL,
                    $COL_REC_CO2_REDUCTION REAL NOT NULL,
                    $COL_REC_PERCENT_OFFSET INTEGER NOT NULL,
                    $COL_REC_NOTES TEXT,
                    $COL_REC_ASSUMPTIONS TEXT,
                    $COL_REC_TIMESTAMP INTEGER NOT NULL,
                    FOREIGN KEY($COL_REC_BILL_ID) REFERENCES $TABLE_BILLS($COL_ID) ON DELETE CASCADE
                )
            """.trimIndent()
            db.execSQL(createRecommendationsTable)
        }
    }

    // ========== BILLS METHODS ==========

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

    // ========== RECOMMENDATIONS METHODS ==========

    fun insertRecommendation(billId: Long, recommendation: SolarRecommendation): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_REC_BILL_ID, billId)
            put(COL_REC_SYSTEM_KW, recommendation.suggestedSystemKw)
            put(COL_REC_MONTHLY_PROD, recommendation.estMonthlyProductionKwh)
            put(COL_REC_MONTHLY_SAVINGS, recommendation.estMonthlySavings)
            put(COL_REC_INSTALL_COST, recommendation.approxInstallCost)
            put(COL_REC_PAYBACK_YEARS, recommendation.paybackYears)
            put(COL_REC_CO2_REDUCTION, recommendation.co2ReductionTonsPerYear)
            put(COL_REC_PERCENT_OFFSET, recommendation.percentOffset)
            put(COL_REC_NOTES, JSONArray(recommendation.notes).toString())
            put(COL_REC_ASSUMPTIONS, JSONArray(recommendation.assumptions).toString())
            put(COL_REC_TIMESTAMP, System.currentTimeMillis())
        }
        return db.insertWithOnConflict(TABLE_RECOMMENDATIONS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getRecommendationForBill(billId: Long): SolarRecommendation? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_RECOMMENDATIONS,
            null,
            "$COL_REC_BILL_ID = ?",
            arrayOf(billId.toString()),
            null,
            null,
            null
        )

        return if (cursor.moveToFirst()) {
            val notesJson = cursor.getString(cursor.getColumnIndexOrThrow(COL_REC_NOTES))
            val assumptionsJson = cursor.getString(cursor.getColumnIndexOrThrow(COL_REC_ASSUMPTIONS))

            val notes = mutableListOf<String>()
            val notesArray = JSONArray(notesJson ?: "[]")
            for (i in 0 until notesArray.length()) {
                notes.add(notesArray.getString(i))
            }

            val assumptions = mutableListOf<String>()
            val assumptionsArray = JSONArray(assumptionsJson ?: "[]")
            for (i in 0 until assumptionsArray.length()) {
                assumptions.add(assumptionsArray.getString(i))
            }

            SolarRecommendation(
                suggestedSystemKw = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_REC_SYSTEM_KW)),
                estMonthlyProductionKwh = cursor.getInt(cursor.getColumnIndexOrThrow(COL_REC_MONTHLY_PROD)),
                estMonthlySavings = cursor.getInt(cursor.getColumnIndexOrThrow(COL_REC_MONTHLY_SAVINGS)),
                approxInstallCost = cursor.getInt(cursor.getColumnIndexOrThrow(COL_REC_INSTALL_COST)),
                paybackYears = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_REC_PAYBACK_YEARS)),
                co2ReductionTonsPerYear = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_REC_CO2_REDUCTION)),
                percentOffset = cursor.getInt(cursor.getColumnIndexOrThrow(COL_REC_PERCENT_OFFSET)),
                notes = notes,
                assumptions = assumptions,
                unitsKWh = 0 // You'll need to fetch this from the bill if needed
            ).also { cursor.close() }
        } else {
            cursor.close()
            null
        }
    }

    fun hasRecommendation(billId: Long): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_RECOMMENDATIONS,
            arrayOf(COL_REC_ID),
            "$COL_REC_BILL_ID = ?",
            arrayOf(billId.toString()),
            null,
            null,
            null
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun deleteRecommendation(billId: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_RECOMMENDATIONS, "$COL_REC_BILL_ID = ?", arrayOf(billId.toString()))
    }
}

// Data class for recommendations
data class SolarRecommendation(
    val suggestedSystemKw: Double,
    val estMonthlyProductionKwh: Int,
    val estMonthlySavings: Int,
    val approxInstallCost: Int,
    val paybackYears: Double,
    val co2ReductionTonsPerYear: Double,
    val percentOffset: Int,
    val notes: List<String>,
    val assumptions: List<String>,
    val unitsKWh: Int
)