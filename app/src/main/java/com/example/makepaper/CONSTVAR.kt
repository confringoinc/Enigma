package com.example.makepaper

import com.google.firebase.database.FirebaseDatabase

object generals{
    val fireBaseReff = FirebaseDatabase.getInstance().reference
    lateinit var preference: MyPreference
    val CSI = "Custom Sign In"
    val GSI = "Google Sign In"
    val FSI = "Facebook Sign In"
}