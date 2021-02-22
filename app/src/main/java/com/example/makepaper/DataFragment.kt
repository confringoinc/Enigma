package com.example.makepaper

import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_data.view.*


class DataFragment : Fragment() {
    private val TAG = "DataFragment"
    private var progressBar: ProgressBar? = null
    var questionList = ArrayList<Questions>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_data, container, false)
        Log.i(TAG, "Entered in OnCreateView")

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
        Log.i(TAG, "<-------Set rv_question.linearlayout-------->")

        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference.child(generals.preference.getID()!!).child("Question")
        Log.i(TAG, "Going into ChildEvent Listener")

        if(questionList.isEmpty()){
            view.rv_questions.visibility = View.GONE
            view.tv_no_questions.visibility = View.VISIBLE
            progressBar!!.visibility = View.GONE
        }

        databaseReference.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                if (progressBar!!.visibility != View.GONE) progressBar!!.visibility = View.GONE

                val data:Map<String, Object> = snapshot.value as Map<String, Object>
                Log.i(TAG, "Question: " + data["question"] + " Marks" + data["marks"])
                questionList.add(getQuesObj(data))
                questionList.reverse()

                if (questionList.isEmpty()) {
                    view.rv_questions.visibility = View.GONE
                    view.tv_no_questions.visibility = View.VISIBLE
                    progressBar!!.visibility = View.GONE
                } else {
                    view.rv_questions.visibility = View.VISIBLE
                    view.tv_no_questions.visibility = View.GONE
                    progressBar!!.visibility = View.GONE
                }

                questionList.reverse()
                val adapter = QuestionAdapter(view.context, questionList)
                Log.i(TAG, "Trying to set rv_question adapter")
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

            }

        })
        Log.i(TAG, "It is called after loop ended")
        //  It is addValueListener
        /*databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val que: ListQuestions? = dataSnapshot.getValue(ListQuestions::class.java)
                    questionList.add(que!!)
                }

                if (questionList.isEmpty()) {
                    view.rv_questions.visibility = View.GONE
                    view.tv_no_questions.visibility = View.VISIBLE
                    progressBar!!.visibility = View.GONE
                } else {
                    view.rv_questions.visibility = View.VISIBLE
                    view.tv_no_questions.visibility = View.GONE
                }

                questionList.reverse()
                val adapter = QuestionAdapter(view.context, questionList)
                Log.i(TAG, "Trying to set rv_question adapter")
                view.rv_questions.adapter = adapter

                progressBar!!.visibility = View.GONE
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.i(TAG, "Data not found...")

                progressBar!!.visibility = View.GONE
            }
        })*/



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

    private fun getQuesObj(data: Map<String, Object>): Questions {
        val question = data["question"] as String
        val marks = data["marks"] as String
        val category = data["category"] as List<String>

        return Questions(question, marks, category)
    }
}