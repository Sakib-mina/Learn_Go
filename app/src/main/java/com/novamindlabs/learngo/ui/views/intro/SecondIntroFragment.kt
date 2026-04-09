package com.novamindlabs.learngo.ui.views.intro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.novamindlabs.learngo.R
import com.novamindlabs.learngo.databinding.FragmentSecondIntroBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SecondIntroFragment : Fragment() {
    private lateinit var binding: FragmentSecondIntroBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSecondIntroBinding.inflate(inflater, container, false)

        binding.apply {
            btnNext.setOnClickListener { findNavController().navigate(R.id.action_secondIntroFragment_to_thirdIntroFragment) }
            btnBack.setOnClickListener { findNavController().navigate(R.id.action_secondIntroFragment_to_firstIntroFragment) }
            skip.setOnClickListener { findNavController().navigate(R.id.action_secondIntroFragment_to_signUpFragment) }
        }

        return binding.root
    }
}