package com.novamindlabs.learngo.ui.views.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.novamindlabs.learngo.R
import com.novamindlabs.learngo.databinding.FragmentMainOnboardingBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainOnboardingFragment : Fragment() {
    private var _binding: FragmentMainOnboardingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            btnNext.setOnClickListener {
                findNavController().navigate(R.id.action_mainOnboardingFragment_to_firstIntroFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}