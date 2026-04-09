package com.novamindlabs.learngo.data.model

data class QuizQuestion(
    val id: String = "",
    val question: String = "",
    val options: List<String> = emptyList(),
    val answer: String = "",
    val category: String = ""
)
