package com.talha.solarscan.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.talha.solarscan.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as? NavHostFragment

        navHostFragment?.let {
            val navController = it.navController
            val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
            bottomNav.setupWithNavController(navController)
        } ?: run {
            // Log error if navHostFragment is null
            android.util.Log.e("MainActivity", "NavHostFragment is null!")
        }
    }
}