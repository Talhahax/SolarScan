package com.talha.solarscan.fragments.scanner

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.talha.solarscan.fragments.UploadPageFragment

class ScannerPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ScanPageFragment()
            1 -> UploadPageFragment()
            2 -> ManualPageFragment()
            else -> ScanPageFragment()
        }
    }
}