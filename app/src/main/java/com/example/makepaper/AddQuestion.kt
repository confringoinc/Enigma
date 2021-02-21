package com.example.makepaper

import android.content.ContentValues.TAG
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_add_question.*

class AddQuestion : AppCompatActivity() {
    private var progressBar: ProgressBar? = null
    var questionList = ArrayList<Questions>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_question)

        progressBar = findViewById(R.id.progress_bar)
        progressBar!!.visibility = View.GONE

        btn_back.setOnClickListener {
            onBackPressed()
        }

        btn_submit.setOnClickListener {

            if(!isNetworkAvailable()) {
                Toast.makeText(
                        baseContext, "Internet is not available",
                        Toast.LENGTH_SHORT
                ).show()
            }
            else {
                progressBar!!.visibility = View.VISIBLE
                val answer = validate() //  Validate components

                answer?.let {
                    //  Get a timeStamp based Unique Key for storing question
                    val key = generals.fireBaseReff.child(generals.preference.getID()!!).child("Question").push().key

                    //  Store the question in current user's uid node under Questions Node
                    generals.fireBaseReff.child(generals.preference.getID()!!).child("Question").child(key!!).setValue(answer)
                            .addOnCompleteListener {
                                progressBar!!.visibility = View.GONE
                                Toast.makeText(this, "Question added", Toast.LENGTH_LONG).show()
                                et_question.text = null
                                et_marks.text = null
                                if (cb_anything.isChecked) cb_anything.toggle()
                                if (cb_remembering.isChecked) cb_remembering.toggle()
                                if (cb_understanding.isChecked) cb_understanding.toggle()
                                et_question.requestFocus()
                            }
                }
            }
        }

        val layoutManager = LinearLayoutManager(applicationContext)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        rv_questions.layoutManager = layoutManager

        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference.child(generals.preference.getID()!!).child("Question")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.i(TAG, "Adding Childeren")
                for (dataSnapshot in snapshot.children) {

                    val data: Map<String, Object> = dataSnapshot.getValue() as Map<String, Object>
                    questionList.add(Questions(data["question"] as String, data["marks"] as String, data["category"] as List<String>))
                }

                questionList.reverse()
                val adapter = QuestionAdapter(applicationContext, questionList)
                rv_questions.adapter = adapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.i(TAG, "Data not found...")
            }
        })
    }

    private fun validate() : Questions? {
        val userQuestion = et_question.text.toString()
        val categories = ArrayList<String>()
        val marks = et_marks.text.toString()

        if(userQuestion.isEmpty()){
            et_question.error = "Please Enter a question"
            et_question.requestFocus()
            progressBar!!.visibility = View.GONE
            return null
        }

        if(marks.isEmpty()){
            et_marks.error = "Please Enter a marks"
            et_marks.requestFocus()
            progressBar!!.visibility = View.GONE
            return null
        }

        if(!cb_anything.isChecked && !cb_remembering.isChecked && !cb_understanding.isChecked) {
            cb_anything.error = "Select at least one checkbox"
            cb_remembering.error = "Select at least one checkbox"
            cb_understanding.error = "Select at least one checkbox"
            progressBar!!.visibility = View.GONE
            return null
        }

        if(cb_anything.isChecked) {
            categories.add(cb_anything.text.toString())
        }

        if(cb_remembering.isChecked) {
            categories.add(cb_remembering.text.toString())
        }

        if(cb_understanding.isChecked) {
            categories.add(cb_understanding.text.toString())
        }

        return Questions(userQuestion, marks, categories.toList())
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }
}