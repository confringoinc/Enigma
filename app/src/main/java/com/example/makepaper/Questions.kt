package com.example.makepaper

data class Questions(
    var key: String?,
    var question: String?,
    var marks: String?,
    var category: List<String>?,
    var paperkey: String? = null
)