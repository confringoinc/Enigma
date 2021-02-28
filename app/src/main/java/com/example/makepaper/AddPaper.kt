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
import kotlinx.android.synthetic.main.activity_add_paper.*

class AddPaper : AppCompatActivity() {
    private var progressBar: ProgressBar? = null
    val TAG = "AddPaper"
    private val databaseReference = FirebaseDatabase.getInstance().reference.child(FirebaseAuth.getInstance().currentUser?.uid!!).child("papers")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_paper)

        if(intent.hasExtra("etName")) {
            et_name.setText(intent.getStringExtra("etName"))
        }

        if(intent.hasExtra("etMarks")) {
            et_marks.setText(intent.getStringExtra("etMarks"))
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

                answer?.let {
                    //  Get a timeStamp based Unique Key for storing question
                    val key = databaseReference.push().key

                    //  Store the question in current user's uid node under Questions Node
                    databaseReference.child(key!!).setValue(answer)
                        .addOnCompleteListener {
                            progressBar!!.visibility = View.GONE
                            Toast.makeText(this, "Paper added", Toast.LENGTH_LONG).show()
                            resetComponents()
                        }
                }
            }
        }
    }

    private fun validate() : Papers? {
        val userPaper = et_name.text.toString()
        val marks = et_marks.text.toString()

        if(userPaper.isEmpty()){
            et_name.error = "Please enter paper name"
            et_name.requestFocus()
            progressBar!!.visibility = View.GONE
            return null
        }

        if(marks.isEmpty()){
            et_marks.error = "Please enter marks"
            et_marks.requestFocus()
            progressBar!!.visibility = View.GONE
            return null
        }

        if(intent.hasExtra("etName")) {
            val query: Query = databaseReference.orderByChild("name").equalTo(intent.getStringExtra("etName"))

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        snapshot.ref.removeValue()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                            applicationContext, "Failed to edit",
                            Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }

        return Papers(userPaper, marks)
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    //  Function to reset all components once answer is submit
    private fun resetComponents(){
        et_name.text = null
        et_marks.text = null
        et_name.requestFocus()
    }
}