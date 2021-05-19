package com.example.makepaper


import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_add_question.*

class AddQuestion : AppCompatActivity() {
    private var progressBar: ProgressBar? = null
    val TAG = "AddQuestion"
    private val databaseReference = FirebaseDatabase.getInstance().reference.child(FirebaseAuth.getInstance().currentUser?.uid!!).child("questions")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_question)

        if(intent.hasExtra("etQuestion")) {
            et_question_add.setText(intent.getStringExtra("etQuestion"))
        }

        if(intent.hasExtra("etMarks")) {
            et_marks_add.setText(intent.getStringExtra("etMarks"))
        }

        if(intent.hasExtra("cb1") || intent.hasExtra("cb2") || intent.hasExtra("cb3")) {
            when(intent.getStringExtra("cb1")) {
                "Anything" -> cb_anything.toggle()
                "Remembering" -> cb_remembering.toggle()
                "Understanding" -> cb_understanding.toggle()
            }
            when(intent.getStringExtra("cb2")) {
                "Remembering" -> cb_remembering.toggle()
                "Understanding" -> cb_understanding.toggle()
            }
            if(intent.getStringExtra("cb3") == "Understanding") cb_understanding.toggle()
        }

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

                //  if data is submit from intent
                if(intent.hasExtra("quesKey")){
                    if(intent.hasExtra("paperKey")) {
                        val paperKey = intent.getStringExtra("paperKey")

                        val databaseReferencePaper = FirebaseDatabase.getInstance().reference.child(FirebaseAuth.getInstance().currentUser?.uid!!).child("papers").child(paperKey!!).child("questions")
                        val query = databaseReferencePaper.orderByChild("question").equalTo(intent.getStringExtra("etQuestion"))

                        query.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for (snapshot in dataSnapshot.children) {
                                    val key = snapshot.key

                                    databaseReferencePaper.child(key!!).setValue(answer).addOnCompleteListener {
                                        progressBar!!.visibility = View.GONE
                                        Toast.makeText(applicationContext, "Question Edited", Toast.LENGTH_LONG)
                                            .show()
                                        resetComponents()
                                        onBackPressed()
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) { }
                        })
                    }
                    else {
                        answer?.let {
                            answer.key = intent.getStringExtra("quesKey")!!

                            databaseReference.child(answer.key!!).setValue(answer)
                                .addOnCompleteListener {
                                    progressBar!!.visibility = View.GONE
                                    Toast.makeText(this, "Question Edited", Toast.LENGTH_LONG)
                                        .show()
                                    resetComponents()
                                    onBackPressed()
                                }
                        }
                    }
                } else {
                    answer?.let {
                        //  Store the question in current user's uid node under Questions Node
                        val query: Query = databaseReference.orderByChild("question").equalTo(answer.question)
                        query.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if(dataSnapshot.hasChildren()) {
                                    progressBar!!.visibility = View.GONE
                                    resetComponents()
                                    Toast.makeText(
                                        applicationContext,
                                        "Question already exists",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                else {
                                    databaseReference.child(answer.key!!).setValue(answer)
                                        .addOnCompleteListener {
                                            progressBar!!.visibility = View.GONE
                                            Toast.makeText(applicationContext, "Question added", Toast.LENGTH_LONG).show()
                                            resetComponents()
                                        }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(
                                    applicationContext,
                                    "Question already exists but not changed",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        })
                    }
                }

            }
        }
    }

    private fun validate() : Questions? {
        val userQuestion = et_question_add.text.toString()
        val quesCategory = ArrayList<String>()
        val marks = et_marks_add.text.toString()

        if(userQuestion.isEmpty()){
            et_question_add.error = "Please enter question"
            et_question_add.requestFocus()
            progressBar!!.visibility = View.GONE
            return null
        }

        if(marks.isEmpty()){
            et_marks_add.error = "Please enter marks"
            et_marks_add.requestFocus()
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

        //  Get a timeStamp based Unique Key for storing question
        val key = databaseReference.push().key

        return Questions(key!!, userQuestion, marks, quesCategory.toList(), null)
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    //  Function to reset all components once answer is submit
    private fun resetComponents(){
        et_question_add.text = null
        et_marks_add.text = null
        if (cb_anything.isChecked) cb_anything.toggle()
        if (cb_remembering.isChecked) cb_remembering.toggle()
        if (cb_understanding.isChecked) cb_understanding.toggle()
        et_question_add.requestFocus()
    }
}