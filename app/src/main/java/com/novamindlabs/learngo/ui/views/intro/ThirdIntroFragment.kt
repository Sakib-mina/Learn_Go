package com.novamindlabs.learngo.ui.views.intro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.novamindlabs.learngo.R
import com.novamindlabs.learngo.databinding.FragmentThirdIntroBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ThirdIntroFragment : Fragment() {
    private lateinit var binding: FragmentThirdIntroBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentThirdIntroBinding.inflate(inflater, container, false)

        binding.apply {
            btnStart.setOnClickListener { findNavController().navigate(R.id.action_thirdIntroFragment_to_signUpFragment) }
            btnBack.setOnClickListener { findNavController().navigate(R.id.action_thirdIntroFragment_to_secondIntroFragment) }
            skip.setOnClickListener { findNavController().navigate(R.id.action_thirdIntroFragment_to_signUpFragment) }
        }

        return binding.root
    }
}