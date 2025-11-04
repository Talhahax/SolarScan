package com.talha.solarscan.fragments.scanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.talha.solarscan.R
import com.talha.solarscan.data.local.Bill
import com.talha.solarscan.viewmodel.BillViewModel
import com.talha.solarscan.viewmodel.SolarViewModel
import java.text.SimpleDateFormat
import java.util.*

class ManualPageFragment : Fragment() {

    private val billViewModel: BillViewModel by activityViewModels()
    private val solarViewModel: SolarViewModel by activityViewModels()

    private lateinit var editUnits: TextInputEditText
    private lateinit var editCost: TextInputEditText
    private lateinit var editBudget: TextInputEditText
    private lateinit var buttonCalculate: MaterialButton
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.page_manual, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editUnits = view.findViewById(R.id.edit_units)
        editCost = view.findViewById(R.id.edit_cost)
        editBudget = view.findViewById(R.id.edit_budget)
        buttonCalculate = view.findViewById(R.id.button_calculate)
        progressBar = view.findViewById(R.id.progress_bar)

        buttonCalculate.setOnClickListener {
            validateAndSubmit()
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        solarViewModel.recommendationLiveData.observe(viewLifecycleOwner) { response ->
            if (response != null && response.success) {
                showLoading(false)
                Toast.makeText(context, "✅ Analysis complete!", Toast.LENGTH_SHORT).show()
            }
        }

        solarViewModel.errorLiveData.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                showLoading(false)
                Toast.makeText(context, "❌ Error: $error", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun validateAndSubmit() {
        val unitsStr = editUnits.text.toString().trim()
        val costStr = editCost.text.toString().trim()
        val budgetStr = editBudget.text.toString().trim()

        if (unitsStr.isEmpty()) {
            editUnits.error = "Required"
            return
        }

        if (costStr.isEmpty()) {
            editCost.error = "Required"
            return
        }

        val units = unitsStr.toIntOrNull()
        val cost = costStr.toIntOrNull()
        val budget = if (budgetStr.isNotEmpty()) budgetStr.toIntOrNull() else null

        if (units == null || units <= 0) {
            editUnits.error = "Invalid number"
            return
        }

        if (cost == null || cost <= 0) {
            editCost.error = "Invalid number"
            return
        }

        saveBill(units, cost)

        val billText = """
            Units Consumed: $units kWh
            Total Amount: Rs. $cost
            Date: ${getCurrentDate()}
        """.trimIndent()

        fetchSolarRecommendation(billText, budget)
    }

    private fun saveBill(units: Int, cost: Int) {
        val bill = Bill(
            units = units,
            cost = cost,
            billingDate = getCurrentDate()
        )
        billViewModel.saveBill(bill)
    }

    private fun fetchSolarRecommendation(billText: String, budget: Int?) {
        showLoading(true)
        solarViewModel.fetchRecommendation(billText, budget)
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd-MMM-yyyy", Locale.US)
        return dateFormat.format(Date())
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        buttonCalculate.isEnabled = !show
        editUnits.isEnabled = !show
        editCost.isEnabled = !show
        editBudget.isEnabled = !show
    }
}