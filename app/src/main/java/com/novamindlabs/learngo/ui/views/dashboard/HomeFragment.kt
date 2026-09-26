package com.novamindlabs.learngo.ui.views.dashboard

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.novamindlabs.learngo.R
import com.novamindlabs.learngo.core.Resource
import com.novamindlabs.learngo.databinding.FragmentHomeBinding
import com.novamindlabs.learngo.databinding.ItemCategoryBinding
import com.novamindlabs.learngo.ui.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()
    private var selectedCategory: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUITheme()

        observeUserData()
        observePurchaseStatus()
        observeDailyTaskProgress()
        observePurchasedListForLocks()

        setupCategoryClicks()
        setupQuizCardClicks()
        setupNavigationActions()
    }

    @SuppressLint("DefaultLocale")
    private fun observeUserData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.userName.collect { name ->
                        binding.tvWelcomeName.text = if (name.isEmpty()) "হ্যালো, লার্নার!" else "হ্যালো, $name"
                    }
                }

                launch {
                    viewModel.userCoins.collect { coins ->
                        binding.tvHomeCoins.text = String.format("%,d", coins)
                    }
                }

                launch {
                    viewModel.streakCount.collect { streak ->
                        binding.tvTopStreak.text = streak.toString()
                    }
                }
            }
        }
    }

    private fun observePurchaseStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.purchaseStatus.collectLatest { resource ->
                    when (resource) {
                        is Resource.Loading -> { }
                        is Resource.Success -> {
                            val result = resource.data
                            if (result == "Already Purchased" || result == "Purchase Successful") {
                                selectedCategory?.let { category ->
                                    navigateToQuiz(category)
                                    selectedCategory = null
                                }
                            }
                        }
                        is Resource.Error -> {
                            showErrorToast(resource.message ?: "ক্রয় ব্যর্থ হয়েছে")
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeDailyTaskProgress() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.dailyTaskCount.collect { count ->
                    binding.apply {
                        val progress = if (count > 5) 5 else count
                        dailyProgressBar.progress = progress
                        tvProgress.text = "$progress/5"
                    }
                }
            }
        }
    }

    private fun observePurchasedListForLocks() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.purchasedQuizzes.collect { purchasedList ->
                    binding.apply {
                        updateLockUI(layoutCategory1, true) 
                        updateLockUI(layoutCategory2, purchasedList.contains("Science"))
                        updateLockUI(layoutCategory3, purchasedList.contains("History"))
                        updateLockUI(layoutCategory4, purchasedList.contains("GK"))
                        updateLockUI(layoutCategory5, purchasedList.contains("Sports"))
                        updateLockUI(layoutCategory6, true) 
                        updateLockUI(layoutCategory7, true) 
                        updateLockUI(layoutCategory8, true)
                        updateLockUI(layoutCategory9, true)
                    }
                }
            }
        }
    }

    private fun updateLockUI(itemBinding: ItemCategoryBinding, isPurchased: Boolean) {}

    private fun applyCategoryCardStyle(
        itemBinding: ItemCategoryBinding,
        startColor: String,
        endColor: String,
        strokeColor: String
    ) {
        val density = resources.displayMetrics.density
        val gradient = android.graphics.drawable.GradientDrawable(
            android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(Color.parseColor(startColor), Color.parseColor(endColor))
        ).apply {
            cornerRadius = 16f * density
            setStroke((1.5f * density).toInt(), Color.parseColor(strokeColor))
        }
        itemBinding.root.background = gradient
    }

    private fun setupCategoryClicks() {
        binding.apply {
            val textColor = Color.WHITE

            layoutCategory1.apply {
                tvCatName.text = "ইসলাম"
                tvCatName.setTextColor(textColor)
                ivCatIcon.setImageResource(R.drawable.ic_islamic)
                ivCatIcon.imageTintList = ColorStateList.valueOf(textColor)
                ivCatIcon.backgroundTintList = null
                applyCategoryCardStyle(this, "#0D3A2D", "#062119", "#10B981")
                root.setOnClickListener { handleQuizAccess("Islamic") }
            }

            layoutCategory2.apply {
                tvCatName.text = "বিজ্ঞান"
                tvCatName.setTextColor(textColor)
                ivCatIcon.setImageResource(R.drawable.ic_science)
                ivCatIcon.imageTintList = ColorStateList.valueOf(textColor)
                ivCatIcon.backgroundTintList = null
                applyCategoryCardStyle(this, "#0B3048", "#061C2C", "#0284C7")
                root.setOnClickListener { handleQuizAccess("Science") }
            }

            layoutCategory3.apply {
                tvCatName.text = "ইতিহাস"
                tvCatName.setTextColor(textColor)
                ivCatIcon.setImageResource(R.drawable.ic_history)
                ivCatIcon.imageTintList = ColorStateList.valueOf(textColor)
                ivCatIcon.backgroundTintList = null
                applyCategoryCardStyle(this, "#3B2208", "#221304", "#D97706")
                root.setOnClickListener { handleQuizAccess("History") }
            }

            layoutCategory4.apply {
                tvCatName.text = "সাধারণ জ্ঞান"
                tvCatName.setTextColor(textColor)
                ivCatIcon.setImageResource(R.drawable.ic_bulb)
                ivCatIcon.imageTintList = ColorStateList.valueOf(textColor)
                ivCatIcon.backgroundTintList = null
                applyCategoryCardStyle(this, "#281D4E", "#171033", "#6366F1")
                root.setOnClickListener { handleQuizAccess("GK") }
            }

            layoutCategory5.apply {
                tvCatName.text = "খেলাধুলা"
                tvCatName.setTextColor(textColor)
                ivCatIcon.setImageResource(R.drawable.ic_sports)
                ivCatIcon.imageTintList = ColorStateList.valueOf(textColor)
                ivCatIcon.backgroundTintList = null
                applyCategoryCardStyle(this, "#0B3338", "#061F22", "#0D9488")
                root.setOnClickListener { handleQuizAccess("Sports") }
            }

            layoutCategory6.apply {
                tvCatName.text = "নামাজ শিক্ষা"
                tvCatName.setTextColor(textColor)
                ivCatIcon.setImageResource(R.drawable.ic_ideas)
                ivCatIcon.imageTintList = ColorStateList.valueOf(textColor)
                ivCatIcon.backgroundTintList = null
                applyCategoryCardStyle(this, "#3D0E1C", "#240811", "#E11D48")
                root.setOnClickListener { handleQuizAccess("Namaj_Quiz") }
            }

            layoutCategory7.apply {
                tvCatName.text = "হাদিস"
                tvCatName.setTextColor(textColor)
                ivCatIcon.setImageResource(R.drawable.ic_book)
                ivCatIcon.imageTintList = ColorStateList.valueOf(textColor)
                ivCatIcon.backgroundTintList = null
                applyCategoryCardStyle(this, "#3B1A22", "#220F14", "#F43F5E")
                root.setOnClickListener { handleQuizAccess("Hadis_Quiz") }
            }

            layoutCategory8.apply {
                tvCatName.text = "কুরআন"
                tvCatName.setTextColor(textColor)
                ivCatIcon.setImageResource(R.drawable.ic_graduation)
                ivCatIcon.imageTintList = ColorStateList.valueOf(textColor)
                ivCatIcon.backgroundTintList = null
                applyCategoryCardStyle(this, "#0A3326", "#051E17", "#059669")
                root.setOnClickListener { handleQuizAccess("Quran_Quiz") }
            }

            layoutCategory9.apply {
                tvCatName.text = "নবীদের জীবনী"
                tvCatName.setTextColor(textColor)
                ivCatIcon.setImageResource(R.drawable.ic_quiz)
                ivCatIcon.imageTintList = ColorStateList.valueOf(textColor)
                ivCatIcon.backgroundTintList = null
                applyCategoryCardStyle(this, "#1E293B", "#0F172A", "#38BDF8")
                root.setOnClickListener { handleQuizAccess("Prophets_Quiz") }
            }
        }
    }

    private fun setupQuizCardClicks() {
        binding.btnPlayNow.setOnClickListener {
            navigateToQuiz("Islamic")
        }
        
        binding.tvSeeAll.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_quizFragment)
        }
    }

    private fun handleQuizAccess(category: String) {
        if (viewModel.isPurchased(category)) {
            navigateToQuiz(category)
        } else {
            showPurchaseConfirmationDialog(category)
        }
    }

    private fun showPurchaseConfirmationDialog(categoryName: String) {
        MaterialAlertDialogBuilder(requireContext(), R.style.Material3DialogTheme)
            .setTitle("কুইজ আনলক করুন")
            .setMessage("এই ক্যাটাগরির কুইজগুলো একবার আনলক করতে ২০ কয়েন প্রয়োজন। একবার কিনলে চিরদিন ফ্রিতে খেলতে পারবেন।")
            .setIcon(R.drawable.ic_coin)
            .setCancelable(false)
            .setPositiveButton("কিনুন") { _, _ ->
                selectedCategory = categoryName
                viewModel.purchaseQuizCategory(categoryName)
            }
            .setNegativeButton("এখন না") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun navigateToQuiz(categoryName: String) {
        val bundle = Bundle().apply {
            putString("CATEGORY_NAME", categoryName)
        }
        findNavController().navigate(R.id.action_homeFragment_to_quizTestFragment, bundle)

        viewModel.incrementDailyTask()
        Toast.makeText(requireContext(), "$categoryName শুরু হচ্ছে...", Toast.LENGTH_SHORT).show()
    }

    private fun setupNavigationActions() {
        binding.apply {
            layoutStats.setOnClickListener { findNavController().navigate(R.id.walletFragment) }
        }
    }

    private fun setupUITheme() {
        activity?.window?.statusBarColor = "#0F172A".toColorInt()
    }

    private fun showErrorToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}