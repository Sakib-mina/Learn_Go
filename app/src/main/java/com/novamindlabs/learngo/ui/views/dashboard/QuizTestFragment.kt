package com.novamindlabs.learngo.ui.views.dashboard

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.novamindlabs.learngo.R
import com.novamindlabs.learngo.core.Resource
import com.novamindlabs.learngo.databinding.FragmentQuizTestBinding
import com.novamindlabs.learngo.ui.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class QuizTestFragment : Fragment() {
    private var _binding: FragmentQuizTestBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()
    private var quizList: List<Map<String, Any>> = listOf()
    private var currentQuestionIndex = 0
    private var score = 0
    private var timer: CountDownTimer? = null
    private var isAnswered = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val category = arguments?.getString("CATEGORY_NAME") ?: "Islamic"
        binding.tvQuizCategoryTitle.text = category

        viewModel.fetchQuizzes(category)
        observeQuizData()

        binding.btnBack.setOnClickListener { handleExitAttempt() }
        binding.btnNext.setOnClickListener { nextQuestion() }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleExitAttempt()
            }
        })
    }

    private fun observeQuizData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.quizState.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is Resource.Success -> {
                            binding.progressBar.visibility = View.GONE
                            quizList = resource.data ?: listOf()
                            if (quizList.isNotEmpty()) {
                                displayQuestion()
                            } else {
                                Toast.makeText(context, "No questions found!", Toast.LENGTH_SHORT).show()
                                findNavController().popBackStack()
                            }
                        }
                        is Resource.Error -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayQuestion() {
        if (currentQuestionIndex < quizList.size) {
            val currentQuestion = quizList[currentQuestionIndex]
            isAnswered = false
            resetOptionButtons()

            binding.apply {
                tvQuestionCount.text = "${currentQuestionIndex + 1}/${quizList.size}"
                tvQuestion.text = currentQuestion["question"]?.toString() ?: "No Question Found"

                option1.text = currentQuestion["option1"]?.toString() ?: "A"
                option2.text = currentQuestion["option2"]?.toString() ?: "B"
                option3.text = currentQuestion["option3"]?.toString() ?: "C"
                option4.text = currentQuestion["option4"]?.toString() ?: "D"

                btnNext.visibility = View.GONE
                startTimer()
            }

            setupOptionClicks(currentQuestion["answer"]?.toString() ?: "")
        } else {
            finishQuiz()
        }
    }

    private fun setupOptionClicks(correctAnswer: String) {
        val options = listOf(binding.option1, binding.option2, binding.option3, binding.option4)
        options.forEach { button ->
            button.setOnClickListener {
                if (!isAnswered) {
                    isAnswered = true
                    timer?.cancel()
                    checkAnswer(button, correctAnswer)
                }
            }
        }
    }

    private fun checkAnswer(selectedButton: MaterialButton, correctAnswer: String) {
        val selectedText = selectedButton.text.toString()

        if (selectedText.trim() == correctAnswer.trim()) {
            selectedButton.backgroundTintList = ColorStateList.valueOf("#10B981".toColorInt())
            selectedButton.setTextColor(Color.WHITE)
            score++
        } else {
            selectedButton.backgroundTintList = ColorStateList.valueOf("#F43F5E".toColorInt())
            selectedButton.setTextColor(Color.WHITE)
            showCorrectAnswer(correctAnswer)
        }
        binding.btnNext.visibility = View.VISIBLE
    }

    private fun showCorrectAnswer(correctAnswer: String) {
        val options = listOf(binding.option1, binding.option2, binding.option3, binding.option4)
        options.forEach { button ->
            if (button.text.toString().trim() == correctAnswer.trim()) {
                button.backgroundTintList = ColorStateList.valueOf("#10B981".toColorInt())
                button.setTextColor(Color.WHITE)
            }
        }
    }

    private fun startTimer() {
        timer?.cancel()
        binding.timerProgress.progress = 100
        timer = object : CountDownTimer(15000, 100) {
            override fun onTick(millisUntilFinished: Long) {
                val progress = (millisUntilFinished / 150).toInt()
                binding.timerProgress.progress = progress
            }

            override fun onFinish() {
                if (!isAnswered) {
                    isAnswered = true
                    showCorrectAnswer(quizList[currentQuestionIndex]["answer"]?.toString() ?: "")
                    binding.btnNext.visibility = View.VISIBLE
                }
            }
        }.start()
    }

    private fun nextQuestion() {
        currentQuestionIndex++
        displayQuestion()
    }

    private fun resetOptionButtons() {
        val options = listOf(binding.option1, binding.option2, binding.option3, binding.option4)
        options.forEach { button ->
            button.backgroundTintList = ColorStateList.valueOf("#1E293B".toColorInt())
            button.strokeColor = ColorStateList.valueOf("#334155".toColorInt())
            button.setTextColor("#E2E8F0".toColorInt())
        }
    }

    private fun finishQuiz() {
        timer?.cancel()

        val bundle = Bundle().apply {
            putInt("SCORE", score)
            putInt("TOTAL", quizList.size)
        }

        findNavController().navigate(R.id.action_quizTestFragment_to_resultFragment, bundle)
    }

    private fun handleExitAttempt() {
        MaterialAlertDialogBuilder(requireContext(), R.style.Material3DialogTheme)
            .setTitle("Exit Quiz?")
            .setMessage("Are you sure you want to quit? Your progress will be lost.")
            .setPositiveButton("Exit") { _, _ -> findNavController().popBackStack() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.cancel()
        _binding = null
    }
}