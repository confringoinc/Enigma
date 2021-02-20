package com.example.makepaper

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_add_question.*

class AddQuestion : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_question)

        btn_back.setOnClickListener {
            onBackPressed()
        }

        btn_submit.setOnClickListener {
            val answer = validate() //  Validate components

            answer?.let {
                //  Get a timeStamp based Unique Key for storing question
                val key = generals.fireBaseReff.child(generals.preference.getID()!!).child("Questions").push().key

                //  Store the question in current user's uid node under Questions Node
                generals.fireBaseReff.child(generals.preference.getID()!!).child("Questions").child(key!!).setValue(answer)
                        .addOnCompleteListener {
                            Toast.makeText(this, "Question Stored Successfull!!!", Toast.LENGTH_LONG).show()
                        }
            }
        }
    }

    private fun validate() : Questions? {
        val user_question = et_question.text.toString()
        val categories = ArrayList<String>()

        if(user_question.isEmpty()){
            et_question.error = "Please Enter a question here !!!"
            return null
        }

        Log.i("AddQuestion" , "Checking if checkbox has been selected ")

        if(!cb_anything.isChecked && !cb_remembering.isChecked && !cb_understanding.isChecked) {
            cb_anything.error = "Select alteast one checkbox"
            cb_remembering.error = "Select alteast one checkbox"
            cb_understanding.error = "Select alteast one checkbox"
            return null
        }

        if(cb_anything.isChecked) {
            Log.i("AddQuestion" , "Checkbox1 Selected: ANYTHING")
            categories.add(cb_anything.text.toString())
        }

        if(cb_remembering.isChecked) {
            Log.i("AddQuestion" , "Checkbox2 Selected: UNDERSTANDING")
            categories.add(cb_remembering.text.toString())
        }

        if(cb_understanding.isChecked) {
            Log.i("AddQuestion" , "Checkbox3 Selected: REMEMBERING")
            categories.add(cb_understanding.text.toString())
        }

        Log.i("Add Question ", "Total items = " + categories.size)

        val obj = Questions(user_question, categories.toList())
        Log.i("AddQuestion", "User_question: " + obj.questions)
        Log.i("AddQuestion", "Category: " + obj.category)
        return obj
    }
}