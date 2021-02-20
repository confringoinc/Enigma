package com.example.makepaper

class ListQuestions {
    var question: String? = null
    var marks: String? = null
    var category: List<String>? = null

    constructor() {}
    constructor(question: String?, marks: String?, category: List<String>?) {
        this.question = question
        this.marks = marks
        this.category = category
    }

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