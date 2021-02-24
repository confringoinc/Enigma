package com.example.makepaper


import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_add_question.*

class AddQuestion : AppCompatActivity() {
    private var progressBar: ProgressBar? = null
    var questionList = ArrayList<Questions>()
    val TAG = "AddQuestion"
    private val questionReff = FirebaseDatabase.getInstance().reference.child(FirebaseAuth.getInstance().currentUser?.uid!!).child("questions")

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

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        rv_questions.layoutManager = layoutManager

        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference.child(FirebaseAuth.getInstance().currentUser?.uid!!).child("questions")

        databaseReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val data: Map<String, Object> = snapshot.value as Map<String, Object>
                questionList.add(getQuesObj(data))
                questionList.reverse()
                val adapter = QuestionAdapter(this@AddQuestion, questionList)
                rv_questions.adapter = adapter
                progressBar!!.visibility = View.GONE
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {
                progressBar!!.visibility = View.GONE
            }

        })
    }

    private fun validate() : Questions? {
        val userQuestion = et_question.text.toString()
        val quesCategory = ArrayList<String>()
        val marks = et_marks.text.toString()

        if(userQuestion.isEmpty()){
            et_question.error = "Please enter question"
            et_question.requestFocus()
            progressBar!!.visibility = View.GONE
            return null
        }

        if(marks.isEmpty()){
            et_marks.error = "Please enter marks"
            et_marks.requestFocus()
            progressBar!!.visibility = View.GONE
            return null
        }

        if(!cb_anything.isChecked && !cb_remembering.isChecked && !cb_understanding.isChecked) {
            cb_anything.error = "Select at least one checkbox"
            cb_remembering.error = "Select at least one checkbox"
            cb_understanding.error = "Select at least one checkbox"
            cb_anything.requestFocus()
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

        return Questions(userQuestion, marks, quesCategory.toList())
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    //  Function to reset all components once answer is submit
    private fun resetComponents(){
        et_question.text = null
        et_marks.text = null
        if (cb_anything.isChecked) cb_anything.toggle()
        if (cb_remembering.isChecked) cb_remembering.toggle()
        if (cb_understanding.isChecked) cb_understanding.toggle()
        et_question.requestFocus()
    }

    private fun getQuesObj(data: Map<String, Object>): Questions {
        val question = data["question"] as String
        val marks = data["marks"] as String
        val category = data["category"] as List<String>

        return Questions(question, marks, category)
    }
}
