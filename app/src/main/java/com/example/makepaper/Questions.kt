package com.example.makepaper

data class Questions(var questions: List<String>){

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
    ))
}