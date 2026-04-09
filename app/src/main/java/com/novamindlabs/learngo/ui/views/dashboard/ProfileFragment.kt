package com.novamindlabs.learngo.ui.views.dashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.novamindlabs.learngo.R
import com.novamindlabs.learngo.databinding.FragmentProfileBinding
import com.novamindlabs.learngo.ui.host.MainActivity
import com.novamindlabs.learngo.ui.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeUserData()
        setupClickListeners()
    }

    @SuppressLint("DefaultLocale")
    private fun observeUserData() {
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.userName.collectLatest { name ->
                binding.tvUserName.text = name
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.userEmail.collectLatest { email ->
                binding.tvUserEmail.text = email.ifEmpty { "No Email" }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.userCoins.collectLatest { coins ->
                binding.tvProfileCoins.text = String.format("%,d", coins)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.dailyTaskCount.collectLatest { count ->
                binding.tvTotalSpent.text = count.toString()
            }
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            btnLogout.setOnClickListener {
                showLogoutDialog()
            }
            btnHelp.setOnClickListener { findNavController().navigate(R.id.action_profileFragment_to_supportFragment) }

            btnPrivacy.setOnClickListener {
                openUrl("https://sites.google.com/view/learn-go-privicy-policy/home")
            }

            btnTerms.setOnClickListener {
                openUrl("https://sites.google.com/view/learn-go-terms-conditation/home")
            }
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(requireContext(), R.style.CustomAlertDialogTheme)
            .setTitle("লগআউট")
            .setMessage("আপনি কি নিশ্চিত যে আপনি লগআউট করতে চান?")
            .setPositiveButton("লগআউট") { _, _ ->
                authViewModel.logout()
                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)

                activity?.finish()
            }
            .setNegativeButton("বাতিল", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}