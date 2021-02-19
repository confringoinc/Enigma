package com.example.makepaper

class ListQuestions {
    var id: String? = null
    var question: String? = null

    constructor() {}
    constructor(id: String?, question: String?) {
        this.id = id
        this.question = question
    }

    @JvmName("getId1")
    fun getId(): String? {
        return id
    }

    @JvmName("getQuestion1")
    fun getQuestion(): String? {
        return question
    }
}