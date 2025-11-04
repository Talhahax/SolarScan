package com.talha.solarscan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.talha.solarscan.R
import com.talha.solarscan.viewmodel.BillViewModel
import android.widget.TextView

class DashboardFragment : Fragment() {

    private val billViewModel: BillViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cardBill = view.findViewById<MaterialCardView>(R.id.card_bill)
        val textNoBills = view.findViewById<TextView>(R.id.text_no_bills)
        val textUnits = view.findViewById<TextView>(R.id.text_units)
        val textCost = view.findViewById<TextView>(R.id.text_cost)
        val textDate = view.findViewById<TextView>(R.id.text_date)
        val buttonViewDetails = view.findViewById<MaterialButton>(R.id.button_view_details)
        val fabScan = view.findViewById<FloatingActionButton>(R.id.fab_scan)

        // Observe latest bill
        billViewModel.latestBillLiveData.observe(viewLifecycleOwner) { bill ->
            if (bill != null) {
                cardBill.visibility = View.VISIBLE
                textNoBills.visibility = View.GONE

                textUnits.text = "${bill.units} units"
                textCost.text = "Rs. ${bill.cost}"
                textDate.text = bill.billingDate
            } else {
                cardBill.visibility = View.GONE
                textNoBills.visibility = View.VISIBLE
            }
        }

        // Click listeners
        fabScan.setOnClickListener {
            findNavController().navigate(R.id.scannerFragment)
        }

        buttonViewDetails.setOnClickListener {
            val bill = billViewModel.latestBillLiveData.value
            if (bill != null) {
                findNavController().navigate(R.id.detailsFragment)
            } else {
                Toast.makeText(context, "No bill found", Toast.LENGTH_SHORT).show()
            }
        }

        // Load latest bill
        billViewModel.loadLatestBill()
    }
}