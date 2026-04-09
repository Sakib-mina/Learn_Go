@file:Suppress("DEPRECATION")

package com.novamindlabs.learngo.ui.views.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.novamindlabs.learngo.R
import com.novamindlabs.learngo.core.Resource
import com.novamindlabs.learngo.data.model.UserRegister
import com.novamindlabs.learngo.databinding.FragmentSignUpBinding
import com.novamindlabs.learngo.ui.host.DashboardActivity
import com.novamindlabs.learngo.ui.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient

    // Google Sign-In Launcher
    private val googleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            viewModel.signInWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            handleLoading(false)
            Toast.makeText(requireContext(), "Google Auth Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupGoogleClient()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupGoogleClient() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun setupClickListeners() {
        binding.apply {
            // Email/Password Signup
            btnSubmit.setOnClickListener {
                val name = etName.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString().trim()

                if (name.isNotEmpty() && email.isNotEmpty() && password.length >= 6) {
                    viewModel.register(UserRegister(id = "", name, email, password))
                } else {
                    Toast.makeText(requireContext(),
                        getString(R.string.enterpass), Toast.LENGTH_SHORT).show()
                }
            }

            // Google Signup Button
            googleSignUpBtn.setOnClickListener {
                handleLoading(true)
                googleSignInClient.signOut().addOnCompleteListener {
                    googleLauncher.launch(googleSignInClient.signInIntent)
                }
            }

            // Login Redirect
            tvLogin.setOnClickListener {
                findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.signupState.collect { resource ->
                    handleLoading(resource is Resource.Loading)

                    when (resource) {
                        is Resource.Success -> {
                            Toast.makeText(requireContext(), "স্বাগতম! Learn Go", Toast.LENGTH_SHORT).show()
                            navigateToDashboard()
                        }
                        is Resource.Error -> {
                            Toast.makeText(requireContext(), resource.message ?: "Error occurred", Toast.LENGTH_LONG).show()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(requireContext(), DashboardActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun handleLoading(isLoading: Boolean) {
        binding.apply {
            // ProgressBar and Button Control
            progressBar.isVisible = isLoading
            btnSubmit.isEnabled = !isLoading
            googleSignUpBtn.isEnabled = !isLoading

            // UI Feedback (Optional: Progress handling)
            btnSubmit.alpha = if (isLoading) 0.5f else 1.0f
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}