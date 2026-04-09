package com.novamindlabs.learngo.ui.views.dashboard

import android.content.res.ColorStateList
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
import com.novamindlabs.learngo.databinding.FragmentQuizBinding
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

    private val categoryList = listOf(
        QuizItem("Islamic", "ইসলামিক কুইজ", R.drawable.ic_islamic, "#4010B981", "মাধ্যম", "#00E5FF", false),
        QuizItem("Hadis_Quiz", "হাদিসের গল্প", R.drawable.ic_quiz, "#40F59E0B", "মাধ্যম", "#FFC107", true), // FREE
        QuizItem("Prophets_Quiz", "নবীদের জীবনী", R.drawable.ic_history, "#403B82F6", "মাধ্যম", "#FFC107", true), // FREE
        QuizItem("Quran_Quiz", "কুরআন কুইজ", R.drawable.ic_bulb, "#408B5CF6", "सहজ", "#00E5FF", true), // FREE
        QuizItem("Namaj_Quiz", "নামাজ শিক্ষা", R.drawable.ic_quiz, "#40EC4899", "सहज", "#00E5FF", true), // FREE
        QuizItem("GK", "সাধারণ জ্ঞান", R.drawable.ic_bulb, "#4006B6D4", "মাধ্যম", "#FFC107", false),
        QuizItem("Science", "বিজ্ঞান ও প্রযুক্তি", R.drawable.ic_science, "#40EF4444", "কঠিন", "#FF5252", false),
        QuizItem("History", "ইতিহাসের পাতা", R.drawable.ic_history, "#40D97706", "কঠিন", "#FF5252", false),
        QuizItem("Sports", "খেলাধুলা", R.drawable.ic_sports, "#4022C55E", "মাধ্যম", "#00E5FF", false)
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

        observePurchaseStatus()
        renderList(categoryList)
    }

    private fun observePurchaseStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.purchaseStatus.collectLatest { resource ->
                    when (resource) {
                        is Resource.Loading -> {  }
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

    private fun renderList(list: List<QuizItem>) {
        binding.apply {
            quizIslamic.root.visibility = if (list.any { it.id == "Islamic" }) View.VISIBLE else View.GONE
            quizHadis.root.visibility = if (list.any { it.id == "Hadis_Quiz" }) View.VISIBLE else View.GONE
            quizProphets.root.visibility = if (list.any { it.id == "Prophets_Quiz" }) View.VISIBLE else View.GONE
            quizQuran.root.visibility = if (list.any { it.id == "Quran_Quiz" }) View.VISIBLE else View.GONE
            quizNamaj.root.visibility = if (list.any { it.id == "Namaj_Quiz" }) View.VISIBLE else View.GONE
            quizGK.root.visibility = if (list.any { it.id == "GK" }) View.VISIBLE else View.GONE
            quizScience.root.visibility = if (list.any { it.id == "Science" }) View.VISIBLE else View.GONE
            quizHistory.root.visibility = if (list.any { it.id == "History" }) View.VISIBLE else View.GONE
            quizSports.root.visibility = if (list.any { it.id == "Sports" }) View.VISIBLE else View.GONE

            list.forEach { item ->
                val cardBinding = when(item.id) {
                    "Islamic" -> quizIslamic
                    "Hadis_Quiz" -> quizHadis
                    "Prophets_Quiz" -> quizProphets
                    "Quran_Quiz" -> quizQuran
                    "Namaj_Quiz" -> quizNamaj
                    "GK" -> quizGK
                    "Science" -> quizScience
                    "History" -> quizHistory
                    else -> quizSports
                }

                cardBinding.apply {
                    tvQuizTitle.text = item.title

                    if (item.isFree) {
                        tvDifficulty.text = "ফ্রি কুইজ"
                        tvDifficulty.setTextColor("#10B981".toColorInt())
                    } else {
                        tvDifficulty.text = item.difficulty
                        tvDifficulty.setTextColor(item.diffColor.toColorInt())
                    }

                    ivQuizIcon.setImageResource(item.icon)
                    ivQuizIcon.backgroundTintList = ColorStateList.valueOf(item.color.toColorInt())
                    ivQuizIcon.imageTintList = ColorStateList.valueOf(item.color.replace("#40", "#FF").toColorInt())

                    root.setOnClickListener {
                        handleQuizEntry(item)
                    }
                }
            }
        }
    }

    private fun handleQuizEntry(item: QuizItem) {
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
            .setMessage("এই ক্যাটাগরির কুইজগুলো চিরস্থায়ীভাবে আনলক করতে ২০ কয়েন প্রয়োজন। আপনি কি কিনতে চান?")
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

    data class QuizItem(
        val id: String,
        val title: String,
        val icon: Int,
        val color: String,
        val difficulty: String,
        val diffColor: String,
        val isFree: Boolean = false
    )
}