package com.confringoinc.enigma

import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_questions.*
import kotlinx.android.synthetic.main.fragment_questions.view.*

class QuestionsFragment : Fragment() {
    private val TAG = "QuestionsFragment"
    private var progressBar: ProgressBar? = null
    var questionList = ArrayList<Questions>()

    private var totalQuestions = 0

    //private val limit = 11
    private var isLoading = true
    private var lastKey: String? = null
    private var dataCnt = 0
    lateinit var adapter: QuestionAdapter
    lateinit var databaseReference: DatabaseReference
    var popup: PopupMenu? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_questions, container, false)

        view.tv_no_questions.visibility = View.GONE

        progressBar = view.progress_bar
        progressBar!!.visibility = View.VISIBLE

        if (!isNetworkAvailable()) {
            Toast.makeText(
                requireContext(), "Internet is not available",
                Toast.LENGTH_SHORT
            ).show()

            progressBar!!.visibility = View.GONE
            view.rv_questions.visibility = View.GONE
            view.tv_no_questions.visibility = View.VISIBLE
        }

        //  Setting LayoutManager for Recycler View
        val layoutManager = LinearLayoutManager(view.context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        view.rv_questions.layoutManager = layoutManager

        //  Getting Firebase Reference Object Instance
        databaseReference = FirebaseDatabase.getInstance().reference
            .child(FirebaseAuth.getInstance().currentUser?.uid!!).child("questions")

        //  Adding a value event Listener to check if user has Question or not.
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Log.i(TAG, "No DataSnapshot Exists")
                    progressBar!!.visibility = View.GONE
                    view.rv_questions.visibility = View.GONE
                    view.tv_no_questions.visibility = View.VISIBLE
                } else {
                    totalQuestions = dataSnapshot.childrenCount.toInt()
                    Log.i(TAG, "DataSnapshot Exists with total Children: $totalQuestions")
                    progressBar!!.visibility = View.GONE
                    view.rv_questions.visibility = View.VISIBLE
                    view.tv_no_questions.visibility = View.GONE
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        databaseReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val data: Map<String, Object> = snapshot.value as Map<String, Object>
                questionList.add(getQuestionObj(data))
                //questionList.reverse()
                adapter = QuestionAdapter(this@QuestionsFragment.context, questionList)
                view.rv_questions.adapter = adapter
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                questionList.forEach {
                    if (it.key == snapshot.key) {
                        it.question = snapshot.child("question").value as String
                        it.marks = snapshot.child("marks").value as String
                        it.category = snapshot.child("category").value as List<String>
                        Log.i(TAG, "Question as ${it.key} Updated")
                    }
                }

                adapter.notifyDataSetChanged()
                view.rv_questions.adapter = adapter
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
                progressBar!!.visibility = View.GONE
            }

        })

        //  Implement onScrollListener to handle Load More Functionality
        view.rv_questions.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    // Scroll Down
                    if (fab_add_question.isShown) {
                        fab_add_question.hide()
                    }
                } else if (dy < 0) {
                    // Scroll Up
                    if (!fab_add_question.isShown) {
                        fab_add_question.show()
                    }
                }
//
//                val visibleItemCount: Int = layoutManager.childCount
//                val pastVisibleItem: Int = layoutManager.findFirstVisibleItemPosition()
//                val total = adapter.itemCount
//
//                //  Check if is Loading
//                if (isLoading) {
//                    //  Check if we reached bottom or not
//                    if ((visibleItemCount + pastVisibleItem) >= total) {
//
//                        if (questionList.size < totalQuestions) {
//                            Log.i(TAG, "ListSize: ${questionList.size} || TotalQues = $totalQuestions")
//                            isLoading = false
//
//                            view!!.onSwipeUpPB.visibility = View.VISIBLE
//                            getData()
//                            /*Handler().postDelayed({
//                                getData()
//                            }, 3000)*/
//
//                        } else {
//                            Log.i(TAG, "Data Up-To-Date")
//                        }
//                    }
//                }
            }
        })

        val mAddQ: Button? = view?.findViewById(R.id.fab_add_question)
        mAddQ?.setOnClickListener {
            startActivity(Intent(view.context, AddQuestionActivity::class.java))
        }

        return view
    }

    companion object {
        fun newInstance(): QuestionsFragment = QuestionsFragment()
    }

//    fun getData(){
//        databaseReference.orderByKey().startAt(lastKey).limitToFirst(limit).addChildEventListener(object : ChildEventListener {
//            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//                var lastChild: DataSnapshot? = null
//
//                lastChild = snapshot
//                if (dataCnt < limit - 1) {
//                    val question = snapshot.child("question").value.toString()
//                    val marks = snapshot.child("marks").value.toString()
//                    val category = snapshot.child("category").value as List<String>
//                    val key = snapshot.key
//
//                    Log.i(TAG, "OnscrollEvent-- Child Added: $key")
//                    questionList.add(Questions(key!!, question, marks, category))
//                    dataCnt += 1
//                } else {
//                    lastKey = snapshot.key
//                    Log.i(TAG, "New LastKey: $lastKey")
//                    dataCnt = 0
//                }
//
//                // adapter = QuestionAdapter(view!!.context, questionList)
//                adapter.notifyDataSetChanged()
//                //  view!!.rv_questions.adapter = adapter
//                view!!.onSwipeUpPB.visibility = View.GONE
//
//                isLoading = true
//            }
//
//            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
//                Log.i(TAG, "onScroll ChildChanged Called for $snapshot")
//                questionList.forEach {
//                    if (it.key == snapshot.key) {
//                        it.question = snapshot.child("question").value as String
//                        it.marks = snapshot.child("marks").value as String
//                        it.category = snapshot.child("category").value as List<String>
//                        Log.i(TAG, "Question as ${it.key} Updated")
//                    }
//                }
//
//                adapter.notifyDataSetChanged()
//                view!!.rv_questions.adapter = adapter
//            }
//
//            override fun onChildRemoved(snapshot: DataSnapshot) {}
//
//            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
//
//            override fun onCancelled(error: DatabaseError) {
//                Toast.makeText(view!!.context, "Failed to load Data", Toast.LENGTH_LONG).show()
//            }
//
//        })
//    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            activity?.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun getQuestionObj(data: Map<String, Object>): Questions {
        val key = data["key"] as String?
        val question = data["question"] as String
        val marks = data["marks"] as String
        val category = data["category"] as List<String>

        return Questions(key, question, marks, category, null)
    }
}