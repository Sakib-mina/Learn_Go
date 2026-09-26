package com.novamindlabs.learngo.ui.views.intro

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.novamindlabs.learngo.R
import com.novamindlabs.learngo.databinding.FragmentSplashBinding
import com.novamindlabs.learngo.ui.host.DashboardActivity
import com.novamindlabs.learngo.ui.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashFragment : Fragment() {
    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Animate progress bar from 0 to 100 over 2.5 seconds
        val animator = ValueAnimator.ofInt(0, 100).apply {
            duration = 2500 // 2.5 seconds
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                binding.splashProgressBar.progress = animation.animatedValue as Int
            }
        }
        animator.start()

        // After the duration, check auto-login and navigate accordingly
        viewLifecycleOwner.lifecycleScope.launch {
            delay(2800) // Ensure animation is fully visible and smooth
            
            if (viewModel.checkAutoLogin()) {
                val intent = Intent(requireContext(), DashboardActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            } else {
                if (isAdded) {
                    findNavController().navigate(R.id.action_splashFragment_to_mainOnboardingFragment)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}