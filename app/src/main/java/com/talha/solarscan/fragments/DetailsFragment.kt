package com.talha.solarscan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.talha.solarscan.R
import com.talha.solarscan.data.remote.AnalyzeBillResponse
import com.talha.solarscan.viewmodel.SolarViewModel

class DetailsFragment : Fragment() {

    private val solarViewModel: SolarViewModel by activityViewModels()

    private lateinit var progressBar: ProgressBar
    private lateinit var layoutDetails: LinearLayout
    private lateinit var textCategory: TextView
    private lateinit var textPanelSize: TextView
    private lateinit var textInstallationCost: TextView
    private lateinit var textMonthlySaving: TextView
    private lateinit var textPaybackPeriod: TextView
    private lateinit var textTips: TextView

    // Additional fields for new API
    private lateinit var textMonthlyProduction: TextView
    private lateinit var textCO2Reduction: TextView
    private lateinit var textPercentOffset: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progress_bar)
        layoutDetails = view.findViewById(R.id.layout_details)
        textCategory = view.findViewById(R.id.text_category)
        textPanelSize = view.findViewById(R.id.text_panel_size)
        textInstallationCost = view.findViewById(R.id.text_installation_cost)
        textMonthlySaving = view.findViewById(R.id.text_monthly_saving)
        textPaybackPeriod = view.findViewById(R.id.text_payback_period)
        textTips = view.findViewById(R.id.text_tips)

        // Initialize new TextViews
        textMonthlyProduction = view.findViewById(R.id.text_monthly_production)
        textCO2Reduction = view.findViewById(R.id.text_co2_reduction)
        textPercentOffset = view.findViewById(R.id.text_percent_offset)

        setupObservers()
    }

    private fun setupObservers() {
        solarViewModel.loadingLiveData.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            layoutDetails.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        solarViewModel.recommendationLiveData.observe(viewLifecycleOwner) { response ->
            if (response != null && response.success) {
                displayRecommendation(response)
            }
        }

        solarViewModel.errorLiveData.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun displayRecommendation(response: AnalyzeBillResponse) {
        val rec = response.recommendation
        val parsed = response.parsedFields

        // Display category based on consumption
        val category = when {
            (parsed.unitsKWh ?: 0) < 300 -> "Low Consumer ✅"
            (parsed.unitsKWh ?: 0) < 600 -> "Average Consumer 📊"
            else -> "High Consumer ⚡"
        }
        textCategory.text = category

        // Solar system details
        textPanelSize.text = "${rec.suggestedSystemKw} kW"
        textInstallationCost.text = "Rs. ${formatNumber(rec.approxInstallCost)}"
        textMonthlySaving.text = "Rs. ${formatNumber(rec.estMonthlySavings)}"
        textPaybackPeriod.text = "${"%.1f".format(rec.paybackYears)} years"
        textMonthlyProduction.text = "${rec.estMonthlyProductionKwh} kWh"
        textCO2Reduction.text = "${"%.1f".format(rec.co2ReductionTonsPerYear)} t"
        textPercentOffset.text = "${rec.percentOffset}%"


        // Tips and notes
        val allTips = mutableListOf<String>()
        allTips.addAll(response.assumptions)
        allTips.addAll(rec.notes)

        val tipsText = allTips.joinToString(separator = "\n• ", prefix = "• ")
        textTips.text = tipsText

        // Show budget warning if needed
        if (response.budgetRequest.needsBudget) {
            Toast.makeText(
                context,
                response.budgetRequest.reason,
                Toast.LENGTH_LONG
            ).show()
        }

    }

    private fun formatNumber(number: Int): String {
        return String.format("%,d", number)
    }

    override fun onResume() {
        super.onResume()
        android.util.Log.d("DetailsFragment", "onResume called - Fragment is visible")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear the recommendation data when leaving details screen
        solarViewModel.clearRecommendation()
    }
}