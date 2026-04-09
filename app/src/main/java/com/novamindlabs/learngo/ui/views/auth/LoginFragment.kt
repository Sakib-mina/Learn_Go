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
import com.novamindlabs.learngo.databinding.FragmentLoginBinding
import com.novamindlabs.learngo.ui.host.DashboardActivity
import com.novamindlabs.learngo.ui.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
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
            if (e.statusCode != 12501) { // 12501 means user cancelled
                Toast.makeText(requireContext(), "Google Auth Error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupGoogleClient()
        setupListeners()
        observeViewModel()
    }

    private fun setupGoogleClient() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun setupListeners() {
        binding.apply {
            // Email & Password Login
            btnLogin.setOnClickListener {
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString().trim()

                if (email.isNotEmpty() && password.isNotEmpty()) {
                    viewModel.login(email, password)
                } else {
                    Toast.makeText(requireContext(), "সবগুলো ঘর পূরণ করুন", Toast.LENGTH_SHORT).show()
                }
            }

            // Google Login
            googleSignUpBtn.setOnClickListener {
                handleLoading(true)
                googleSignInClient.signOut().addOnCompleteListener {
                    googleLauncher.launch(googleSignInClient.signInIntent)
                }
            }

            // Navigate to SignUp
            tvSignUpLink.setOnClickListener {
                findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect Email Login State
                launch {
                    viewModel.loginState.collect { resource ->
                        handleLoading(resource is Resource.Loading)
                        handleResource(resource)
                    }
                }

                launch {
                    viewModel.signupState.collect { resource ->
                        handleLoading(resource is Resource.Loading)
                        handleResource(resource)
                    }
                }
            }
        }
    }

    private fun handleResource(resource: Resource<*>) {
        when (resource) {
            is Resource.Success -> {
                Toast.makeText(requireContext(), "আবারও স্বাগতম!", Toast.LENGTH_SHORT).show()
                navigateToDashboard()
            }
            is Resource.Error -> {
                Toast.makeText(requireContext(), resource.message ?: "Login Failed", Toast.LENGTH_LONG).show()
            }
            else -> Unit
        }
    }

    private fun handleLoading(isLoading: Boolean) {
        binding.apply {
            // ProgressBar/Overlay visibility
            progressBar.isVisible = isLoading

            btnLogin.isEnabled = !isLoading
            btnLogin.isEnabled = !isLoading

            // Subtle feedback
            btnLogin.alpha = if (isLoading) 0.5f else 1.0f
        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(requireContext(), DashboardActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}