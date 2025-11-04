package com.talha.solarscan.repository

import com.talha.solarscan.data.remote.ApiClient
import com.talha.solarscan.data.remote.AnalyzeBillRequest
import com.talha.solarscan.data.remote.AnalyzeBillResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class SolarRepository {

    private val apiService = ApiClient.apiService

    suspend fun fetchRecommendation(
        text: String,
        budget: Int? = null
    ): Result<AnalyzeBillResponse> = withContext(Dispatchers.IO) {
        try {
            val request = AnalyzeBillRequest(text, budget)
            val response: Response<AnalyzeBillResponse> =
                apiService.analyzeBill(request).execute()

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("API Error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}