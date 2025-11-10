package com.talha.solarscan.bill

import android.util.Log

object BillParser {

    private const val TAG = "BillParser"

    data class ParsedBill(
        val units: Int?,
        val cost: Int?,
        val billingDate: String?
    )

    fun parse(text: String): ParsedBill {
        Log.d(TAG, "========== PARSING BILL ==========")
        Log.d(TAG, "Raw text length: ${text.length}")

        // Extract units - now handles BOTH "679 Units" AND "units consumed: 679"
        val unitsRegex = """(\d+)\s*(?:units?|kwh)|(?:units?\s*consumed?|consumption)[\s:]*(\d+)""".toRegex(RegexOption.IGNORE_CASE)
        val unitsMatch = unitsRegex.find(text)
        val units = unitsMatch?.let {
            // Try first capture group (for "679 Units" format)
            it.groupValues.getOrNull(1)?.toIntOrNull()
            // If null, try second capture group (for "units consumed: 679" format)
                ?: it.groupValues.getOrNull(2)?.toIntOrNull()
        }
        Log.d(TAG, "Units match: ${unitsMatch?.value}")
        Log.d(TAG, "Units parsed: $units")

        // Extract cost - enhanced to handle more formats
        // Handles: "Rs. 42,719", "Amount Payable Rs. 42,719", "Total: Rs 42719"
        val costRegex = """(?:amount|total|payable|bill|rs\.?)[\s:]*rs\.?\s*(\d+(?:,\d+)*)|rs\.?\s*(\d+(?:,\d+)*)""".toRegex(RegexOption.IGNORE_CASE)
        val costMatch = costRegex.find(text)
        val costString = costMatch?.let {
            // Try first capture group
            it.groupValues.getOrNull(1)?.takeIf { s -> s.isNotEmpty() }
            // If null or empty, try second capture group
                ?: it.groupValues.getOrNull(2)
        }
        val cost = costString?.replace(",", "")?.toIntOrNull()
        Log.d(TAG, "Cost match: ${costMatch?.value}")
        Log.d(TAG, "Cost string: $costString")
        Log.d(TAG, "Cost parsed: $cost")

        // Extract date (various formats)
        // Priority order: DD-MMM-YYYY (most specific) -> DD/MM/YYYY -> Month YYYY
        val dateRegex = """(\d{1,2}[-/](Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[-/]\d{2,4})|(\d{1,2}[-/]\d{1,2}[-/]\d{2,4})|((?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec|January|February|March|April|May|June|July|August|September|October|November|December)[-\s]\d{4})""".toRegex(RegexOption.IGNORE_CASE)
        val dateMatch = dateRegex.find(text)
        val billingDate = dateMatch?.value
        Log.d(TAG, "Date match: ${dateMatch?.value}")
        Log.d(TAG, "Date parsed: $billingDate")

        val result = ParsedBill(units, cost, billingDate)
        Log.d(TAG, "========== RESULT ==========")
        Log.d(TAG, "Units: ${result.units}")
        Log.d(TAG, "Cost: ${result.cost}")
        Log.d(TAG, "Date: ${result.billingDate}")
        Log.d(TAG, "============================")

        return result
    }
}