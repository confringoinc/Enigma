package com.example.makepaper

import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_data.view.*


class DataFragment : Fragment() {
    private val TAG = "DataFragment"
    private var progressBar: ProgressBar? = null
    var questionList = ArrayList<Questions>()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_data, container, false)

        view.tv_no_questions.visibility = View.GONE

        progressBar = view.progress_bar
        progressBar!!.visibility = View.VISIBLE

        if(!isNetworkAvailable()) {
            Toast.makeText(
                    requireContext(), "Internet is not available",
                    Toast.LENGTH_SHORT
            ).show()

            progressBar!!.visibility = View.GONE
            view.rv_questions.visibility = View.GONE
            view.tv_no_questions.visibility = View.VISIBLE
        }

        val layoutManager = LinearLayoutManager(view.context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        view.rv_questions.layoutManager = layoutManager

        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference.child(
                FirebaseAuth.getInstance().currentUser?.uid!!
        ).child("questions")

        databaseReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val data: Map<String, Object> = snapshot.value as Map<String, Object>
                questionList.add(getQuestionObj(data))
                questionList.reverse()
                val adapter = QuestionAdapter(view.context, questionList)
                view.rv_questions.adapter = adapter
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

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    progressBar!!.visibility = View.GONE
                    view.rv_questions.visibility = View.GONE
                    view.tv_no_questions.visibility = View.VISIBLE
                } else {
                    progressBar!!.visibility = View.GONE
                    view.rv_questions.visibility = View.VISIBLE
                    view.tv_no_questions.visibility = View.GONE
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        val mAddQ: Button? = view?.findViewById(R.id.btn_add_question)
        mAddQ?.setOnClickListener {
            startActivity(Intent(view.context, AddQuestion::class.java))
        }

        return view
    }

    companion object {
        fun newInstance(): DataFragment = DataFragment()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = activity?.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun getQuestionObj(data: Map<String, Object>): Questions {
        val question = data["question"] as String
        val marks = data["marks"] as String
        val category = data["category"] as List<String>

        return Questions(question, marks, category)
    }
}