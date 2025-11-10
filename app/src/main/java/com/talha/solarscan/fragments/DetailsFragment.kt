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
import androidx.lifecycle.lifecycleScope
import com.talha.solarscan.R
import com.talha.solarscan.data.local.DatabaseHelper
import com.talha.solarscan.data.local.SolarRecommendation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailsFragment : Fragment() {

    private lateinit var progressBar: ProgressBar
    private lateinit var layoutDetails: LinearLayout
    private lateinit var textCategory: TextView
    private lateinit var textPanelSize: TextView
    private lateinit var textInstallationCost: TextView
    private lateinit var textMonthlySaving: TextView
    private lateinit var textPaybackPeriod: TextView
    private lateinit var textTips: TextView
    private lateinit var textMonthlyProduction: TextView
    private lateinit var textCO2Reduction: TextView
    private lateinit var textPercentOffset: TextView

    private lateinit var dbHelper: DatabaseHelper
    private var billId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DatabaseHelper.getInstance(requireContext())

        // Get bill ID from arguments (passed from dashboard)
        billId = arguments?.getLong("bill_id", -1) ?: -1
    }

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
        textMonthlyProduction = view.findViewById(R.id.text_monthly_production)
        textCO2Reduction = view.findViewById(R.id.text_co2_reduction)
        textPercentOffset = view.findViewById(R.id.text_percent_offset)

        loadRecommendation()
    }

    private fun loadRecommendation() {
        if (billId == -1L) {
            // No bill ID provided, get the latest bill
            lifecycleScope.launch {
                val latestBill = withContext(Dispatchers.IO) {
                    dbHelper.getLatestBill()
                }

                if (latestBill != null) {
                    billId = latestBill.id
                    fetchAndDisplayRecommendation(latestBill.units)
                } else {
                    Toast.makeText(context, "No bill found", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                }
            }
        } else {
            // Bill ID provided, fetch its recommendation
            fetchAndDisplayRecommendation(0) // units will be fetched if needed
        }
    }

    private fun fetchAndDisplayRecommendation(unitsKWh: Int) {
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            layoutDetails.visibility = View.GONE

            val recommendation = withContext(Dispatchers.IO) {
                dbHelper.getRecommendationForBill(billId)
            }

            progressBar.visibility = View.GONE
            layoutDetails.visibility = View.VISIBLE

            if (recommendation != null) {
                displayRecommendation(recommendation, unitsKWh)
            } else {
                Toast.makeText(context, "No recommendation found for this bill", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayRecommendation(rec: SolarRecommendation, unitsKWh: Int) {
        // Display category based on consumption
        val units = if (unitsKWh > 0) unitsKWh else rec.unitsKWh
        val category = when {
            units < 300 -> "Low Consumer ✅"
            units < 600 -> "Average Consumer 📊"
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
        allTips.addAll(rec.assumptions)
        allTips.addAll(rec.notes)

        val tipsText = if (allTips.isNotEmpty()) {
            allTips.joinToString(separator = "\n• ", prefix = "• ")
        } else {
            "No additional notes"
        }
        textTips.text = tipsText
    }

    private fun formatNumber(number: Int): String {
        return String.format("%,d", number)
    }

    companion object {
        fun newInstance(billId: Long): DetailsFragment {
            return DetailsFragment().apply {
                arguments = Bundle().apply {
                    putLong("bill_id", billId)
                }
            }
        }
    }
}