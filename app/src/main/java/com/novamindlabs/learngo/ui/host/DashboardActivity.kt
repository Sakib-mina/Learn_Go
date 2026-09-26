package com.novamindlabs.learngo.ui.host

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.novamindlabs.learngo.R
import com.novamindlabs.learngo.databinding.ActivityDashboardBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_dashboard) as? NavHostFragment
        
        if (navHostFragment != null) {
            navController = navHostFragment.navController
            binding.bottomNav.setupWithNavController(navController)
            
            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.homeFragment,
                    R.id.quizFragment,
                    R.id.walletFragment,
                    R.id.profileFragment -> {
                        binding.bottomNav.visibility = View.VISIBLE
                    }
                    else -> {
                        binding.bottomNav.visibility = View.GONE
                    }
                }
            }
        }

        fixStatusBar()
    }

    private fun fixStatusBar() {
        window.statusBarColor = "#0F172A".toColorInt()
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.isAppearanceLightStatusBars = false
    }
}
