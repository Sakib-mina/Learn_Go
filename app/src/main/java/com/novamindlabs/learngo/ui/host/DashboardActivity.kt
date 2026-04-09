package com.novamindlabs.learngo.ui.host

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.novamindlabs.learngo.R
import com.novamindlabs.learngo.databinding.ActivityDashboardBinding
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.graphics.toColorInt

@Suppress("DEPRECATION")
@AndroidEntryPoint
class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_dashboard) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)

        fixStatusBar()

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
    @SuppressLint("ObsoleteSdkInt")
    private fun fixStatusBar() {
        window.apply {
            statusBarColor = "#0F172A".toColorInt()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val decorView = window.decorView
                var flags = decorView.systemUiVisibility
                flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                decorView.systemUiVisibility = flags
            }
        }
    }
}