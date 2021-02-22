package com.example.makepaper


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
    val TAG = "AddQuestion"
    val questionReff = generals.fireBaseReff.child(generals.preference.getID()!!).child("Question")

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
                    val key = questionReff.push().key

                    Log.i(TAG, "Answer: " + answer)

                    //  Store the question in current user's uid node under Questions Node
                    questionReff.child(key!!).setValue(answer)
                            .addOnCompleteListener {
                                progressBar!!.visibility = View.GONE
                                Toast.makeText(this, "Question added", Toast.LENGTH_LONG).show()
                                resetComponents()
                            }
                }
            }
        }

        val layoutManager = LinearLayoutManager(applicationContext)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        rv_questions.layoutManager = layoutManager

        //  val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference.child(generals.preference.getID()!!).child("Question")

        questionReff.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.i(TAG, "Adding Childeren")
                for (dataSnapshot in snapshot.children) {

                    val data: Map<String, Object> = dataSnapshot.getValue() as Map<String, Object>
                    val question = data["question"] as String
                    val marks = data["marks"] as String
                    val category = data["category"] as List<String>
                    questionList.add(Questions(question, marks, category))

                    Log.i(TAG, "Ques: " + question + " marks: " + marks + " cat: " + category)
                }

                questionList.reverse()
                val adapter = QuestionAdapter(this@AddQuestion, questionList)
                rv_questions.adapter = adapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.i(TAG, "Data not found...")
            }
        })
    }

    private fun validate() : Questions? {
        val userQuestion = et_question.text.toString()
        val quesCategory = ArrayList<String>()
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
            quesCategory.add(cb_anything.text.toString())
        }

        if(cb_remembering.isChecked) {
            quesCategory.add(cb_remembering.text.toString())
        }

        if(cb_understanding.isChecked) {
            quesCategory.add(cb_understanding.text.toString())
        }

        val ques_obj = Questions(userQuestion, marks, quesCategory.toList())
        Log.i(TAG, "Returned Questions Object: " + ques_obj)

        return ques_obj
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    //  Function to reset all componenets once answer is submit
    private fun resetComponents(){
        et_question.text = null
        et_marks.text = null
        if (cb_anything.isChecked) cb_anything.toggle()
        if (cb_remembering.isChecked) cb_remembering.toggle()
        if (cb_understanding.isChecked) cb_understanding.toggle()
        et_question.requestFocus()
    }
}