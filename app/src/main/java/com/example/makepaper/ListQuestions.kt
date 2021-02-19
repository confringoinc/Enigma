package com.example.makepaper

class ListQuestions {
    var id: String? = null
    var question: String? = null

    constructor() {}
    constructor(id: String?, question: String?) {
        this.id = id
        this.question = question
    }

    fun getID(): String? {
        return id
    }

    fun getQuestions(): String? {
        return question
    }
}