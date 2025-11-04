package com.talha.solarscan.utils

object BillParser {

    data class ParsedBill(
        val units: Int?,
        val cost: Int?,
        val billingDate: String?
    )

    fun parse(text: String): ParsedBill {
        // Extract units
        val unitsRegex = """(?:units?\s*consumed?|consumption)[\s:]*(\d+)""".toRegex(RegexOption.IGNORE_CASE)
        val units = unitsRegex.find(text)?.groupValues?.get(1)?.toIntOrNull()

        // Extract cost (handles both "Rs. 9000" and "Rs. 9,000")
        val costRegex = """(?:amount|total|payable|bill)[\s:]*Rs\.?\s*(\d+(?:,\d+)*)""".toRegex(RegexOption.IGNORE_CASE)
        val costMatch = costRegex.find(text)?.groupValues?.get(1)
        val cost = costMatch?.replace(",", "")?.toIntOrNull()

        // Extract date (various formats)
        val dateRegex = """(\d{1,2}[-/]\d{1,2}[-/]\d{2,4})|([A-Za-z]+\s+\d{4})""".toRegex()
        val billingDate = dateRegex.find(text)?.value

        return ParsedBill(units, cost, billingDate)
    }
}