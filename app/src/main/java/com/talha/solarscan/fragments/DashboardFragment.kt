package com.talha.solarscan.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.talha.solarscan.R
import com.talha.solarscan.bill.BillAdapter
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

        // FIXED: Simulate bottom nav click for consistent navigation
        fabScan.setOnClickListener {
            // Get the bottom navigation view from activity
            val bottomNav = activity?.findViewById<BottomNavigationView>(R.id.bottomNav)

            // Programmatically select the scanner tab
            // This ensures navigation behaves exactly like clicking the bottom nav
            bottomNav?.selectedItemId = R.id.scannerFragment

            Log.d("DashboardFragment", "FAB clicked - navigating to scanner via bottom nav")
        }

        // Load all bills
        billViewModel.loadAllBills()
    }

    private fun onViewDetailsClick(bill: Bill) {
        // Navigate to details fragment with bill ID
        val bundle = bundleOf("bill_id" to bill.id)
        findNavController().navigate(R.id.action_dashboard_to_details, bundle)
        Log.d("DashboardFragment", "Navigating to details for bill ID: ${bill.id}")
    }

    private fun setupRecyclerView() {
        billAdapter = BillAdapter(
            bills = emptyList(),
            onBillClick = { bill ->
                onViewDetailsClick(bill)
            },
            hasRecommendation = { billId ->
                // Check if recommendation exists in database
                solarViewModel.hasRecommendation(billId)
            }
        )

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
                Log.d("DashboardFragment", "Bill $index: id=${bill.id}, units=${bill.units}, cost=${bill.cost}, date=${bill.billingDate}")
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

    override fun onResume() {
        super.onResume()
        Log.d("DashboardFragment", "onResume called - Refreshing bills")
        // Refresh bills when returning to dashboard
        billViewModel.loadAllBills()
    }
}