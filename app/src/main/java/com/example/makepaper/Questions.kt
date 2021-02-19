package com.example.makepaper

data class Questions(var questions: List<String>, var category: List<String>, var difficulty: List<String>){

}

object questions_list{
    val my_quetiions = Questions(listOf(
            "My Question 1",
            "My Question 2",
            "My Question 3",
            "My Question 4",
            "My Question 5",
            "My Question 6",
            "My Question 7",
            "My Question 8",
            "My Question 9"
    ), listOf(
            "ANYTHING",
            "ANYTHING",
            "ANYTHING",
            "ANYTHING",
            "ANYTHING",
            "ANYTHING",
            "ANYTHING",
            "ANYTHING",
            "ANYTHING"
    ), listOf(
            "Hard",
            "Hard",
            "Hard",
            "Hard",
            "Hard",
            "Hard",
            "Hard",
            "Hard",
            "Hard"
    ))
}