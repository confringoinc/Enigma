package com.example.makepaper

data class Questions(var questions: String?, var category: List<String>?){

}

object questions_list{
    val my_questions = listOf(
            Questions("My Question 1", listOf("Understanding", "Anything")),
            Questions("My Question 2", listOf("Understanding")),
            Questions("My Question 3", listOf("Remembering", "Anything")),
            Questions("My Question 4", listOf("Remembering", "Understanding")),
            Questions("My Question 5", listOf("Anything", "Understaning")),
            Questions("My Question 6", listOf("Remembering", "Anything")),
            Questions("My Question 7", listOf("Understaning", "Anything")),
            Questions("My Question 8", listOf("Remembering"))
    )
}