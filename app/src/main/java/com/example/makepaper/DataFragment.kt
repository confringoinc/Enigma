package com.example.makepaper

import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_data.view.*


class DataFragment : Fragment() {
    private val TAG = "DataFragment"
    private var progressBar: ProgressBar? = null
    var questionList = ArrayList<Questions>()

    private val limit = 11
    private var isLoading = true
    private var lastKey:String? = null
    private var dataCnt = 0
    lateinit var adapter: QuestionAdapter
    lateinit var databaseReference: DatabaseReference

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
                    Log.i(TAG, "DataSnapshot Exists")
                    progressBar!!.visibility = View.GONE
                    view.rv_questions.visibility = View.VISIBLE
                    view.tv_no_questions.visibility = View.GONE
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        databaseReference.orderByKey().limitToFirst(limit).addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val data: Map<String, Object> = snapshot.value as Map<String, Object>
                if (dataCnt < limit-1){
                    questionList.add(getQuestionObj(data))
                    // questionList.reverse()
                    adapter = QuestionAdapter(view.context, questionList)
                    view.rv_questions.adapter = adapter
                    dataCnt += 1
                }else{
                    lastKey = data["key"] as String
                    Log.i(TAG, "Last Key Stored: $lastKey. Data Count = $dataCnt")
                    dataCnt = 0
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                questionList.forEach {
                    if(it.key == snapshot.key){
                        it.question = snapshot.child("question").value as String
                        it.marks = snapshot.child("marks").value as String
                        it.category = snapshot.child("category").value as List<String>
                        Log.i(TAG, "Question as ${it.key} Updated")
                    }
                }

                adapter = QuestionAdapter(view.context, questionList)
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
        view.rv_questions.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                val visibleItemCount:Int = layoutManager.childCount
                val pastVisibleItem:Int = layoutManager.findFirstVisibleItemPosition()
                val total = adapter.itemCount

                //  Check if is Loading
                if(isLoading){
                    //  Check if we reached bottom or not
                    if(( visibleItemCount + pastVisibleItem ) >= total){
                        isLoading = false
                        if(questionList.size == limit-1){
                            view!!.onSwipeUpPB.visibility = View.VISIBLE
                            Handler().postDelayed({
                                getData()
                                isLoading = true
                            }, 3000)
                        }
                    }
                }
            }
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

    fun getData(){
        databaseReference.orderByKey().startAt(lastKey).limitToFirst(limit).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.i(TAG, "Adding Data|| Total children: ${snapshot.childrenCount}")
                var lastChild: DataSnapshot? = null
                for(childObj in snapshot.children){
                    lastChild = childObj
                    if(dataCnt < limit-1){
                        val question = childObj.child("question").value.toString()
                        val marks = childObj.child("marks").value.toString()
                        val category = childObj.child("category").value as List<String>
                        val key = childObj.key

                        Log.i(TAG, "Child Added: $key")
                        questionList.add(Questions(key!!, question, marks, category))
                        dataCnt += 1
                    }else{
                        lastKey = childObj.key
                        Log.i(TAG, "New LastKey: $lastKey")
                        dataCnt = 0
                    }
                }

                //  In Case if their are < 10 element then store last key of last child
                if(snapshot.childrenCount.toInt() != limit) {
                    lastKey = lastChild!!.key
                    Log.i(TAG, "New LastKey: $lastKey")
                    dataCnt = 0
                }
                // adapter = QuestionAdapter(view!!.context, questionList)
                adapter.notifyDataSetChanged()
                //  view!!.rv_questions.adapter = adapter
                isLoading = false
                view!!.onSwipeUpPB.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Log.i(TAG, "Value Event Listener Failed. Error: $error")
            }

        })
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = activity?.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun getQuestionObj(data: Map<String, Object>): Questions {
        val key = data["key"] as String?
        val question = data["question"] as String
        val marks = data["marks"] as String
        val category = data["category"] as List<String>

        return Questions(key, question, marks, category)
    }
}