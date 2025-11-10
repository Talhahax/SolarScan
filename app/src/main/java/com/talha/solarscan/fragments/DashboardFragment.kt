package com.talha.solarscan.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.talha.solarscan.R
import com.talha.solarscan.adapters.BillAdapter
import com.talha.solarscan.bill.Bill
import com.talha.solarscan.bill.BillViewModel
import com.talha.solarscan.viewmodel.SolarViewModel

class DashboardFragment : Fragment() {

    private val billViewModel: BillViewModel by activityViewModels()
    private val solarViewModel: SolarViewModel by activityViewModels()

    private lateinit var recyclerBills: RecyclerView
    private lateinit var textNoBills: TextView
    private lateinit var billAdapter: BillAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerBills = view.findViewById(R.id.recycler_bills)
        textNoBills = view.findViewById(R.id.text_no_bills)
        val fabScan = view.findViewById<FloatingActionButton>(R.id.fab_scan)

        setupRecyclerView()
        setupObservers()

        // Click listener for FAB
        fabScan.setOnClickListener {
            findNavController().navigate(R.id.scannerFragment)
        }

        // Load all bills
        billViewModel.loadAllBills()
    }

    private fun setupRecyclerView() {
        billAdapter = BillAdapter(emptyList()) { bill ->
            onBillClick(bill)
        }

        recyclerBills.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = billAdapter
        }
    }

    private fun setupObservers() {
        Log.d("DashboardFragment", "Setting up observers...")

        billViewModel.allBillsLiveData.observe(viewLifecycleOwner) { bills ->
            Log.d("DashboardFragment", "========== BILLS UPDATED ==========")
            Log.d("DashboardFragment", "Received ${bills.size} bills")
            bills.forEachIndexed { index, bill ->
                Log.d("DashboardFragment", "Bill $index: units=${bill.units}, cost=${bill.cost}, date=${bill.billingDate}")
            }

            if (bills.isNotEmpty()) {
                recyclerBills.visibility = View.VISIBLE
                textNoBills.visibility = View.GONE
                billAdapter.updateBills(bills)
                Log.d("DashboardFragment", "✅ RecyclerView updated with ${bills.size} bills")
            } else {
                recyclerBills.visibility = View.GONE
                textNoBills.visibility = View.VISIBLE
                Log.d("DashboardFragment", "⚠️ No bills to display")
            }
        }
    }

    private fun onBillClick(bill: Bill) {
        // Load the recommendation for this bill from the database/storage
        // For now, we'll need to fetch it again or store it with the bill
        // Navigate to details fragment
        findNavController().navigate(R.id.detailsFragment)
    }

    override fun onResume() {
        super.onResume()
        // Refresh bills when returning to dashboard
        billViewModel.loadAllBills()
    }
}