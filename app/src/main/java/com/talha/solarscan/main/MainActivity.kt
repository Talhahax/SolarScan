package com.talha.solarscan.main

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.talha.solarscan.R

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        var isReady = false
        splashScreen.setKeepOnScreenCondition { !isReady }

        // Simulate loading
        Handler(Looper.getMainLooper()).postDelayed({
            isReady = true
        }, 2000)
        setContentView(R.layout.activity_main)

        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as? NavHostFragment

        navHostFragment?.let {
            navController = it.navController
            bottomNav = findViewById(R.id.bottomNav)

            // Setup bottom nav with nav controller
            bottomNav.setupWithNavController(navController)

            // CRITICAL FIX: Handle item reselection
            // When user clicks the already-selected tab, pop back to that destination
            bottomNav.setOnItemReselectedListener { item ->
                Log.d("MainActivity", "Bottom nav item reselected: ${item.title}")
                when (item.itemId) {
                    R.id.dashboardFragment -> {
                        // Pop everything back to dashboard
                        navController.popBackStack(R.id.dashboardFragment, false)
                    }
                    R.id.scannerFragment -> {
                        // Pop everything back to scanner
                        navController.popBackStack(R.id.scannerFragment, false)
                    }
                }
            }

            // Optional: Hide bottom nav on detail screens for cleaner UI
            navController.addOnDestinationChangedListener { _, destination, _ ->
                Log.d("MainActivity", "Navigated to: ${destination.label}")
                when (destination.id) {
                    R.id.detailsFragment -> {
                        bottomNav.visibility = android.view.View.GONE
                    }
                    R.id.dashboardFragment, R.id.scannerFragment -> {
                        bottomNav.visibility = android.view.View.VISIBLE
                    }
                }
            }

        } ?: run {
            Log.e("MainActivity", "NavHostFragment is null!")
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}