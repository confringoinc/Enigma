package com.example.makepaper

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_add_question.*
import kotlin.collections.listOf as listOf

class AddQuestion : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_question)

        bt_submit.setOnClickListener {
            val answer = validate()

            answer?.let {
                /*generals.fireBaseReff.child(generals.preference.getuserID()!!).child("Questions").setValue(answer)
                    .addOnCompleteListener {
                        Toast.makeText(this, "Question Addedd!!!", Toast.LENGTH_LONG).show()
                    }*/

                val key = generals.fireBaseReff.child(generals.preference.getUserID()!!).child("Questions").push().key

                generals.fireBaseReff.child(generals.preference.getUserID()!!).child("Questions").child(key!!).setValue(answer)
                    .addOnCompleteListener {
                        Toast.makeText(this, "Question Stored Successfull!!!", Toast.LENGTH_LONG).show()
                    }
            }
        }
    }

    private fun validate() : Questions? {
        var user_question = questionTV.text.toString();
        var categories = ArrayList<String>()

        if(user_question.toString().isEmpty()){
            questionTV.error = "Please Enter a question here !!!"
            return null;
        }

        if(!questionTV.text.toString().endsWith("?")){
            user_question += "?"
        }

        Log.i("AddQuestion" , "Checking if checkbox has been selected ")

        if(!checkBox1.isChecked && !checkBox2.isChecked && !checkBox3.isChecked) {
            checkBox1.error = "Select alteast one checkbox"
            checkBox2.error = "Select alteast one checkbox"
            checkBox3.error = "Select alteast one checkbox"
            return null;
        }

        if(checkBox1.isChecked) {
            Log.i("AddQuestion" , "Checkbox1 Selected: ANYTHING")
            categories.add(checkBox1.text.toString())
        }

        if(checkBox2.isChecked) {
                Log.i("AddQuestion" , "Checkbox1 Selected: UNDERSTANDING")
                categories.add(checkBox2.text.toString())
        }

        if(checkBox3.isChecked) {
            Log.i("AddQuestion" , "Checkbox1 Selected: REMEMBERING")
            categories.add(checkBox3.text.toString())
        }

        Log.i("Add QUestion ", "Total items = " + categories.size)

        val obj = Questions(user_question, categories.toList())
        Log.i("AddQUestion", "User_question: " + obj.questions)
        Log.i("AddQuestion", "Category: " + obj.category)
        return obj
    }
}