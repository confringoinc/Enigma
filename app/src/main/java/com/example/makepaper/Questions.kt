package com.example.makepaper

data class Questions(var question: String?, var marks: String?, var category: List<String>?){
    fun getQuestions(): String? {
        return question
    }

    fun getMark(): String? {
        return marks
    }

    fun getCategories(): List<String>? {
        return category
    }
}