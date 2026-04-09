package com.novamindlabs.learngo.ui.views.support

import android.content.Intent
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.novamindlabs.learngo.databinding.FragmentSupportBinding
import com.novamindlabs.learngo.databinding.ItemFaqBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SupportFragment : Fragment() {
    private var _binding: FragmentSupportBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSupportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadFaqs()

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.cardEmailSupport.setOnClickListener {
            sendEmail()
        }
    }

    private fun loadFaqs() {
        addFaqItem("কিভাবে ফ্রি কয়েন পাব?", "নতুন ইউজার হিসেবে সাইন আপ করলেই আপনি পাচ্ছেন ৫০টি কয়েন একদম ফ্রি!")
        addFaqItem("কুইজ খেলতে কত কয়েন লাগে?","যেকোনো পেইড ক্যাটাগরি আনলক করতে ২০টি কয়েন প্রয়োজন। একবার আনলক করলে ওই ক্যাটাগরি চিরস্থায়ীভাবে আপনার হয়ে যাবে, বারবার কয়েন কাটার প্রয়োজন নেই।"        )
        addFaqItem("আমি কিভাবে আরো কয়েন পাব?", "আপনি 'ভল্ট' সেকশন থেকে কয়েন প্যাক কিনতে পারেন অথবা ডেইলি টাস্ক পূরণ করে কয়েন আয় করতে পারেন।")
        addFaqItem("আমার কয়েন কি অন্য ডিভাইসে পাব?", "হ্যাঁ! আপনার অ্যাকাউন্ট ইমেইল দিয়ে লগইন করা থাকলে যেকোনো ডিভাইসে আপনার কয়েন সিঙ্ক হবে।")
        addFaqItem("কয়েন কেনা হয়েছে কিন্তু যোগ হয়নি?", "কখনো কখনো কয়েক মিনিট সময় লাগতে পারে। ১০ মিনিটের বেশি হলে আপনার ট্রানজেকশন আইডি সহ আমাদের ইমেইল করুন।")
    }

    private fun addFaqItem(question: String, answer: String) {

        val faqBinding = ItemFaqBinding.inflate(layoutInflater, binding.faqContainer, false)

        faqBinding.tvQuestion.text = question
        faqBinding.tvAnswer.text = answer

        faqBinding.layoutQuestion.setOnClickListener {
            TransitionManager.beginDelayedTransition(binding.faqContainer, AutoTransition())

            if (faqBinding.tvAnswer.isVisible) {
                faqBinding.tvAnswer.visibility = View.GONE
                faqBinding.ivExpandArrow.animate().rotation(0f).setDuration(300).start()
            } else {
                faqBinding.tvAnswer.visibility = View.VISIBLE
                faqBinding.ivExpandArrow.animate().rotation(180f).setDuration(300).start()
            }
        }

        binding.faqContainer.addView(faqBinding.root)
    }

    private fun sendEmail() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:apps.helpdesk263@gmail.com".toUri()
            putExtra(Intent.EXTRA_SUBJECT, "Support Request - Learn Go")
        }
        try {
            startActivity(emailIntent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "আপনার ডিভাইসে কোনো ইমেইল অ্যাপ নেই!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}