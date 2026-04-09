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
                        tvProgress.text = "$progress/5 সম্পন্ন"
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
                        updateLockUI(layoutCategory1, purchasedList.contains("Islamic"))
                        updateLockUI(layoutCategory2, purchasedList.contains("Science"))
                        updateLockUI(layoutCategory3, purchasedList.contains("History"))
                        updateLockUI(layoutCategory4, purchasedList.contains("GK"))
                        updateLockUI(layoutCategory5, purchasedList.contains("Sports"))
                    }
                }
            }
        }
    }

    private fun updateLockUI(itemBinding: ItemCategoryBinding, isPurchased: Boolean) {}

    private fun setupCategoryClicks() {
        binding.apply {
            val textColor = Color.WHITE

            layoutCategory1.apply {
                tvCatName.text = "ইসলাম"
                tvCatName.setTextColor(textColor)
                ivCatIcon.setImageResource(R.drawable.ic_islamic)
                ivCatIcon.backgroundTintList = ColorStateList.valueOf("#4010B981".toColorInt())
                ivCatIcon.imageTintList = ColorStateList.valueOf("#10B981".toColorInt())
                root.setOnClickListener { handleQuizAccess("Islamic") }
            }

            layoutCategory2.apply {
                tvCatName.text = "বিজ্ঞান"
                tvCatName.setTextColor(textColor)
                ivCatIcon.setImageResource(R.drawable.ic_science)
                ivCatIcon.backgroundTintList = ColorStateList.valueOf("#4038BDF8".toColorInt())
                ivCatIcon.imageTintList = ColorStateList.valueOf("#38BDF8".toColorInt())
                root.setOnClickListener { handleQuizAccess("Science") }
            }

            layoutCategory3.apply {
                tvCatName.text = "ইতিহাস"
                tvCatName.setTextColor(textColor)
                ivCatIcon.setImageResource(R.drawable.ic_history)
                ivCatIcon.backgroundTintList = ColorStateList.valueOf("#40F59E0B".toColorInt())
                ivCatIcon.imageTintList = ColorStateList.valueOf("#F59E0B".toColorInt())
                root.setOnClickListener { handleQuizAccess("History") }
            }

            layoutCategory4.apply {
                tvCatName.text = "সাধারণ জ্ঞান"
                tvCatName.setTextColor(textColor)
                ivCatIcon.setImageResource(R.drawable.ic_bulb)
                ivCatIcon.backgroundTintList = ColorStateList.valueOf("#408B5CF6".toColorInt())
                ivCatIcon.imageTintList = ColorStateList.valueOf("#8B5CF6".toColorInt())
                root.setOnClickListener { handleQuizAccess("GK") }
            }

            layoutCategory5.apply {
                tvCatName.text = "খেলাধুলা"
                tvCatName.setTextColor(textColor)
                ivCatIcon.setImageResource(R.drawable.ic_sports)
                ivCatIcon.backgroundTintList = ColorStateList.valueOf("#40F43F5E".toColorInt())
                ivCatIcon.imageTintList = ColorStateList.valueOf("#F43F5E".toColorInt())
                root.setOnClickListener { handleQuizAccess("Sports") }
            }
        }
    }

    private fun setupQuizCardClicks() {
        binding.apply {
            quizCard1.apply {
                tvQuizTitle.text = "হাদিসের গল্প"
                tvQuizInfo.text = "১০টি প্রশ্ন • ফ্রি"
                ivQuizThumbnail.setImageResource(R.drawable.img_hadis_stories)
                btnStartQuiz.setOnClickListener { navigateToQuiz("Hadis_Quiz") }
            }

            quizCard2.apply {
                tvQuizTitle.text = "নবীদের জীবনী"
                tvQuizInfo.text = "১০টি প্রশ্ন • ফ্রি"
                ivQuizThumbnail.setImageResource(R.drawable.img_prophet)
                btnStartQuiz.setOnClickListener { navigateToQuiz("Prophets_Quiz") }
            }

            quizCard3.apply {
                tvQuizTitle.text = "নামাজ শিক্ষা"
                tvQuizInfo.text = "১০টি প্রশ্ন • ফ্রি"
                ivQuizThumbnail.setImageResource(R.drawable.img_namaz)
                btnStartQuiz.setOnClickListener { navigateToQuiz("Namaj_Quiz") }
            }

            quizCard4.apply {
                tvQuizTitle.text = "কুরআন কুইজ"
                tvQuizInfo.text = "১০টি প্রশ্ন • ফ্রি"
                ivQuizThumbnail.setImageResource(R.drawable.img_quran_storie)
                btnStartQuiz.setOnClickListener { navigateToQuiz("Quran_Quiz") }
            }
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
            cardWallet.setOnClickListener { findNavController().navigate(R.id.walletFragment) }
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