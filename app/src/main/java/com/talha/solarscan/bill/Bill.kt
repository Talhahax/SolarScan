package com.talha.solarscan.bill

data class Bill(
    val id: Long = 0,
    val units: Int,
    val cost: Int,
    val billingDate: String,
    val createdAt: Long = System.currentTimeMillis()
)