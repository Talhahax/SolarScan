package com.talha.solarscan.data.remote

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/analyze-bill")
    fun analyzeBill(@Body request: AnalyzeBillRequest): Call<AnalyzeBillResponse>
}