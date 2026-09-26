package com.novamindlabs.learngo.ui.views.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.novamindlabs.learngo.R
import com.novamindlabs.learngo.core.Resource
import com.novamindlabs.learngo.databinding.FragmentQuizBinding
import com.novamindlabs.learngo.ui.adapter.QuizAdapter
import com.novamindlabs.learngo.ui.adapter.QuizCategory
import com.novamindlabs.learngo.ui.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class QuizFragment : Fragment() {
    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    private var selectedCategory: String? = null
    private var currentFilterMode = FilterMode.ALL

    private lateinit var quizAdapter: QuizAdapter

    enum class FilterMode { ALL, FREE, PREMIUM, POPULAR }

    private val allCategories = listOf(
        QuizCategory("Islamic", "ইসলামিক কুইজ", "20 Questions", R.drawable.ic_islamic, "#22C55E", "#16A34A", isFree = true, isPopular = true),
        QuizCategory("Hadis_Quiz", "হাদিসের গল্প", "20 Questions", R.drawable.ic_quiz, "#E11D48", "#BE123C", isFree = false, isPopular = true),
        QuizCategory("Prophets_Quiz", "নবীদের জীবনী", "20 Questions", R.drawable.ic_history, "#3B82F6", "#1D4ED8", isFree = true, isPopular = true),
        QuizCategory("Quran_Quiz", "কুরআন কুইজ", "20 Questions", R.drawable.ic_bulb, "#059669", "#047857", isFree = false, isPopular = true),
        QuizCategory("Namaj_Quiz", "নামাজ শিক্ষা", "20 Questions", R.drawable.ic_quiz, "#EC4899", "#DB2777", isFree = true, isPopular = false),
        QuizCategory("GK", "সাধারণ জ্ঞান", "20 Questions", R.drawable.ic_bulb, "#6366F1", "#4338CA", isFree = false, isPopular = true),
        QuizCategory("Science", "বিজ্ঞান ও প্রযুক্তি", "20 Questions", R.drawable.ic_science, "#06B6D4", "#0284C7", isFree = false, isPopular = true),
        QuizCategory("History", "ইতিহাসের পাতা", "20 Questions", R.drawable.ic_history, "#F59E0B", "#D97706", isFree = true, isPopular = false),
        QuizCategory("Sports", "খেলাধুলা", "20 Questions", R.drawable.ic_sports, "#10B981", "#059669", isFree = true, isPopular = false),
        QuizCategory("Geography", "ভূগোল কুইজ", "20 Questions", R.drawable.ic_quiz, "#2563EB", "#1D4ED8", isFree = false, isPopular = false),
        QuizCategory("BD_History", "বাংলাদেশের ইতিহাস", "20 Questions", R.drawable.ic_history, "#059669", "#047857", isFree = true, isPopular = true),
        QuizCategory("World_Country", "বিশ্ব ও দেশ", "20 Questions", R.drawable.ic_bulb, "#8B5CF6", "#7C3AED", isFree = false, isPopular = false),
        QuizCategory("Sahaba_Life", "সাহাবীদের জীবন", "20 Questions", R.drawable.ic_islamic, "#0D9488", "#0F766E", isFree = true, isPopular = false),
        QuizCategory("Islamic_History", "ইসলামের ইতিহাস", "20 Questions", R.drawable.ic_history, "#D97706", "#B45309", isFree = false, isPopular = false),
        QuizCategory("Riddles", "ধাঁধা ও বুদ্ধির খেলা", "20 Questions", R.drawable.ic_ideas, "#4F46E5", "#3730A3", isFree = true, isPopular = true),
        QuizCategory("English_Lang", "ইংরেজি ভাষা", "20 Questions", R.drawable.ic_book, "#E11D48", "#9F1239", isFree = false, isPopular = false),
        QuizCategory("Computer_IT", "কম্পিউটার ও আইটি", "20 Questions", R.drawable.ic_science, "#0284C7", "#0369A1", isFree = true, isPopular = false),
        QuizCategory("Animal_World", "প্রাণীজগৎ", "20 Questions", R.drawable.ic_bag, "#16A34A", "#15803D", isFree = true, isPopular = false),
        QuizCategory("Inventions", "আবিষ্কার ও আবিষ্কারক", "20 Questions", R.drawable.ic_light, "#CA8A04", "#A16207", isFree = false, isPopular = true)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observePurchaseStatus()
        applyFilter()
    }

    private fun setupRecyclerView() {
        quizAdapter = QuizAdapter { category ->
            handleQuizEntry(category)
        }
        binding.rvQuizzes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = quizAdapter
        }
    }

    private fun setupListeners() {
        binding.apply {
            btnBack.setOnClickListener {
                findNavController().navigateUp()
            }

            tabAll.setOnClickListener {
                currentFilterMode = FilterMode.ALL
                updateTabUI(tabAll)
                applyFilter()
            }

            tabFree.setOnClickListener {
                currentFilterMode = FilterMode.FREE
                updateTabUI(tabFree)
                applyFilter()
            }

            tabPremium.setOnClickListener {
                currentFilterMode = FilterMode.PREMIUM
                updateTabUI(tabPremium)
                applyFilter()
            }

            tabPopular.setOnClickListener {
                currentFilterMode = FilterMode.POPULAR
                updateTabUI(tabPopular)
                applyFilter()
            }
        }
    }

    private fun updateTabUI(selectedTab: TextView) {
        binding.apply {
            val tabs = listOf(tabAll, tabFree, tabPremium, tabPopular)
            tabs.forEach { tab ->
                if (tab == selectedTab) {
                    tab.setBackgroundResource(R.drawable.bg_pill_selected)
                    tab.setTextColor(Color.parseColor("#0F172A"))
                } else {
                    tab.setBackgroundResource(R.drawable.bg_pill_unselected)
                    tab.setTextColor(Color.WHITE)
                }
            }
        }
    }

    private fun applyFilter() {
        val filteredList = when (currentFilterMode) {
            FilterMode.ALL -> allCategories
            FilterMode.FREE -> allCategories.filter { it.isFree }
            FilterMode.PREMIUM -> allCategories.filter { !it.isFree }
            FilterMode.POPULAR -> allCategories.filter { it.isPopular }
        }

        quizAdapter.submitList(filteredList)

        if (filteredList.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvQuizzes.visibility = View.GONE
        } else {
            binding.tvEmptyState.visibility = View.GONE
            binding.rvQuizzes.visibility = View.VISIBLE
        }
    }

    private fun observePurchaseStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.purchaseStatus.collectLatest { resource ->
                    when (resource) {
                        is Resource.Loading -> { }
                        is Resource.Success -> {
                            selectedCategory?.let {
                                navigateToQuiz(it)
                                selectedCategory = null
                            }
                        }
                        is Resource.Error -> {
                            Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun handleQuizEntry(item: QuizCategory) {
        if (item.isFree || viewModel.isPurchased(item.id)) {
            navigateToQuiz(item.id)
        } else {
            showPurchaseDialog(item.id)
        }
    }

    private fun showPurchaseDialog(categoryId: String) {
        MaterialAlertDialogBuilder(requireContext(), R.style.Material3DialogTheme)
            .setIcon(R.drawable.ic_coin)
            .setTitle("কুইজ আনলক করুন")
            .setMessage("এই ক্যাটাগরির কুইজগুলো চিরস্থায়ীভাবে আনলক করতে ২০ কয়েন প্রয়োজন। আপনি কি কিনতে চান?")
            .setCancelable(false)
            .setPositiveButton("আনলক করি") { _, _ ->
                selectedCategory = categoryId
                viewModel.purchaseQuizCategory(categoryId)
            }
            .setNegativeButton("এখন না") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun navigateToQuiz(category: String) {
        val bundle = Bundle().apply { putString("CATEGORY_NAME", category) }
        findNavController().navigate(R.id.action_quizFragment_to_quizTestFragment, bundle)
        viewModel.incrementDailyTask()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}