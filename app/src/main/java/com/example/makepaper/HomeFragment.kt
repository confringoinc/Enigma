package com.example.makepaper

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_home.view.*


class HomeFragment : Fragment() {

    private val TAG = "HomeFragment"
    private var progressBarPaper: ProgressBar? = null
    private var progressBarQuestion: ProgressBar? = null
    var paperList = ArrayList<Papers>()
    var questionList = ArrayList<Questions>()

    companion object {
        lateinit var auth: FirebaseAuth
        fun newInstance(): HomeFragment = HomeFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        user?.let {
            val name = user.displayName
            val firstName = name?.split(" ")
            view?.findViewById<TextView?>(R.id.tv_name)?.text = firstName?.get(0)
        }

        view.tv_no_papers.visibility = View.GONE
        view.tv_no_questions.visibility = View.GONE
        progressBarPaper = view.progress_bar_paper
        progressBarPaper!!.visibility = View.VISIBLE
        progressBarQuestion = view.progress_bar_question
        progressBarQuestion!!.visibility = View.VISIBLE

        if(!isNetworkAvailable()) {
            Toast.makeText(
                    requireContext(), "Internet is not available",
                    Toast.LENGTH_SHORT
            ).show()

            progressBarPaper!!.visibility = View.GONE
            progressBarQuestion!!.visibility = View.GONE
            view.tv_no_papers.visibility = View.VISIBLE
            view.tv_no_questions.visibility = View.VISIBLE
        }

        view.rv_papers.layoutManager = object: LinearLayoutManager(view.context, HORIZONTAL, false) {
            override fun checkLayoutParams(lp: RecyclerView.LayoutParams): Boolean {
                lp.width = (width / 2.15).toInt()
                return true
            }
        }

        view.rv_questions.layoutManager = object: LinearLayoutManager(view.context, VERTICAL, false) {}

        val databaseReferencePaper: DatabaseReference = FirebaseDatabase.getInstance().reference.child(FirebaseAuth.getInstance().currentUser?.uid!!).child("papers")
        val queryPaper: Query = databaseReferencePaper.orderByKey().limitToLast(10)

        queryPaper.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val data: Map<String, Object> = snapshot.value as Map<String, Object>
                paperList.add(getPaperObj(data))
                paperList.reverse()
                val adapter = PaperAdapter(view.context, paperList)
                view.rv_papers.adapter = adapter
                progressBarPaper!!.visibility = View.GONE
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {
                progressBarPaper!!.visibility = View.GONE
            }

        })

        databaseReferencePaper.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    progressBarPaper!!.visibility = View.GONE
                    view.tv_no_papers.visibility = View.VISIBLE
                } else {
                    progressBarPaper!!.visibility = View.GONE
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        val databaseReferenceQuestion: DatabaseReference = FirebaseDatabase.getInstance().reference.child(FirebaseAuth.getInstance().currentUser?.uid!!).child("questions")

        val queryQuestion: Query = databaseReferenceQuestion.orderByKey().limitToLast(10)
        queryQuestion.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val data: Map<String, Object> = snapshot.value as Map<String, Object>
                questionList.add(getQuestionObj(data))
                questionList.reverse()
                val adapter = QuestionAdapter(view.context, questionList)
                view.rv_questions.adapter = adapter
                progressBarQuestion!!.visibility = View.GONE
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {
                progressBarQuestion!!.visibility = View.GONE
            }

        })

        databaseReferenceQuestion.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    progressBarQuestion!!.visibility = View.GONE
                    view.tv_no_questions.visibility = View.VISIBLE
                } else {
                    progressBarQuestion!!.visibility = View.GONE
                    view.tv_no_questions.visibility = View.GONE
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
        return view
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun getPaperObj(data: Map<String, Object>): Papers {
        val name = data["name"] as String
        val marks = data["marks"] as String

        return Papers(name, marks)
    }

    private fun getQuestionObj(data: Map<String, Object>): Questions {
        val key = data["key"] as String
        val question = data["question"] as String
        val marks = data["marks"] as String
        val category = data["category"] as List<String>

        return Questions(key, question, marks, category)
    }
}