package com.novamindlabs.learngo.ui.views.dashboard

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.novamindlabs.learngo.R
import com.novamindlabs.learngo.databinding.FragmentResultBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val score = arguments?.getInt("SCORE") ?: 0
        val total = arguments?.getInt("TOTAL") ?: 0

        binding.apply {
            tvFinalScore.text = "আপনার স্কোর: $score/$total"

            val percentage = if (total > 0) (score * 100) / total else 0

            tvResultStatus.text = when {
                percentage >= 80 -> "অসাধারণ!"
                percentage >= 50 -> "ভালো হয়েছে!"
                else -> "আরও চেষ্টা করুন!"
            }

            btnDone.setOnClickListener {
                findNavController().navigate(R.id.action_resultFragment_to_homeFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}