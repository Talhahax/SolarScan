package com.talha.solarscan.fragments.scanner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import androidx.viewpager2.widget.ViewPager2
import com.talha.solarscan.R
import com.talha.solarscan.viewmodel.SolarViewModel

class ScannerFragment : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private val solarViewModel: SolarViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scanner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabLayout = view.findViewById(R.id.tab_layout)
        viewPager = view.findViewById(R.id.view_pager)

        setupViewPager()
        observeNavigation()
    }

    private fun setupViewPager() {
        val adapter = ScannerPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Scan"
                1 -> "Upload"
                2 -> "Manual"
                else -> ""
            }
        }.attach()
    }

    private fun observeNavigation() {
        // Observe the navigation EVENT (single-use)
        // Now receives bill ID instead of response
        solarViewModel.navigationEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { billId ->
                // Navigate to the details fragment with bill ID
                val bundle = bundleOf("bill_id" to billId)
                findNavController().navigate(R.id.action_scanner_to_details, bundle)
            }
        }
    }
}