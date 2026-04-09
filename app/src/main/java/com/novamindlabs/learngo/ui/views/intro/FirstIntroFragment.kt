package com.novamindlabs.learngo.ui.views.intro

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.novamindlabs.learngo.R
import com.novamindlabs.learngo.databinding.FragmentFirstIntroBinding
import com.novamindlabs.learngo.ui.host.DashboardActivity
import com.novamindlabs.learngo.ui.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class FirstIntroFragment : Fragment() {
    private lateinit var binding: FragmentFirstIntroBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFirstIntroBinding.inflate(inflater, container, false)

        viewLifecycleOwner.lifecycleScope.launch {
            if (viewModel.checkAutoLogin()) {
                val intent = Intent(requireContext(), DashboardActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }
        }

        binding.apply {
           btnNext.setOnClickListener { findNavController().navigate(R.id.action_firstIntroFragment_to_secondIntroFragment) }
           skip.setOnClickListener { findNavController().navigate(R.id.action_firstIntroFragment_to_signUpFragment) }
        }

        return binding.root
    }
}