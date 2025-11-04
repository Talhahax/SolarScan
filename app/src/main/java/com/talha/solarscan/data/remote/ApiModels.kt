package com.talha.solarscan.data.remote

data class AnalyzeBillRequest(
    val text: String,
    val budget: Int? = null
)

data class ParsedFields(
    val unitsKWh: Int?,
    val totalCost: Int?,
    val costPerUnit: Double?,
    val billingDate: String?,
    val location: String?,
    val tariff: String?,
    val peakDemandKw: Double?
)

data class CostBreakdown(
    val panels: Int,
    val inverterAndBalance: Int,
    val installation: Int
)

data class Recommendation(
    val suggestedSystemKw: Double,
    val estMonthlyProductionKwh: Int,
    val estMonthlySavings: Int,
    val approxInstallCost: Int,
    val costBreakdown: CostBreakdown,
    val paybackYears: Double,
    val co2ReductionTonsPerYear: Double,
    val percentOffset: Int,
    val notes: List<String>
)

data class BudgetRequest(
    val needsBudget: Boolean,
    val reason: String?
)

data class MetaDefaults(
    val costPerKw: Int,
    val prodPerKwPerMonth: Int,
    val emissionKgPerKwh: Double
)

data class Meta(
    val usedDefaults: MetaDefaults
)

data class AnalyzeBillResponse(
    val success: Boolean,
    val parsedFields: ParsedFields,
    val assumptions: List<String>,
    val recommendation: Recommendation,
    val budgetRequest: BudgetRequest,
    val meta: Meta
)