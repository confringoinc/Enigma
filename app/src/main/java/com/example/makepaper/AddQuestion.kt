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
            val answer = validate()

            answer?.let {
                /*generals.fireBaseReff.child(generals.preference.getuserID()!!).child("Questions").setValue(answer)
                    .addOnCompleteListener {
                        Toast.makeText(this, "Question Addedd!!!", Toast.LENGTH_LONG).show()
                    }*/

                val key = generals.fireBaseReff.child(generals.preference.getID()!!).child("Questions").push().key

                generals.fireBaseReff.child(generals.preference.getID()!!).child("Questions").child(key!!).setValue(answer)
                        .addOnCompleteListener {
                            Toast.makeText(this, "Question Stored Successfull!!!", Toast.LENGTH_LONG).show()
                        }
            }
        }
    }

    private fun validate() : Questions? {
        val user_question = ArrayList<String>()
        val categories = ArrayList<String>()
        val difficulty = ArrayList<String>()

        if(et_question.text.toString().isEmpty()){
            et_question.error = "Please Enter a question here !!!"
            return null
        }

        user_question.add(et_question.text.toString())

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

        if(!rb_hard.isChecked && !rb_medium.isChecked && !rb_easy.isChecked) {
            rb_hard.error = "Select alteast one checkbox"
            rb_medium.error = "Select alteast one checkbox"
            rb_easy.error = "Select alteast one checkbox"
            return null
        }

        if(rb_hard.isChecked) {
            Log.i("AddQuestion" , "Checkbox1 Selected: ANYTHING")
            difficulty.add(rb_hard.text.toString())
        }

        if(rb_medium.isChecked) {
            Log.i("AddQuestion" , "Checkbox2 Selected: UNDERSTANDING")
            difficulty.add(rb_medium.text.toString())
        }

        if(rb_easy.isChecked) {
            Log.i("AddQuestion" , "Checkbox3 Selected: REMEMBERING")
            difficulty.add(rb_easy.text.toString())
        }

        Log.i("Add QUestion ", "Total items = " + categories.size)

        val obj = Questions(user_question, categories.toList(), difficulty)
        Log.i("AddQuestion", "User_question: " + obj.questions)
        Log.i("AddQuestion", "Category: " + obj.category)
        Log.i("AddQuestion", "Difficulty: " + obj.difficulty)
        return obj
    }
}