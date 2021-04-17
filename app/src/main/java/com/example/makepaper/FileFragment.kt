package com.example.makepaper

import android.content.Context
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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_file.view.*

class FileFragment : Fragment() {

    private val TAG = "FileFragment"
    private var progressBar: ProgressBar? = null
    var paperList = ArrayList<Papers>()
    lateinit var adapter: PaperAdapter
    private lateinit var databaseReference: DatabaseReference
    private val limit = 11
    private var isLoading = true
    private var lastKey:String? = null
    private var dataCnt = 0

    companion object {
        fun newInstance(): FileFragment = FileFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_file, container, false)

        view.tv_no_papers.visibility = View.GONE

        progressBar = view.progress_bar
        progressBar!!.visibility = View.VISIBLE

        if(!isNetworkAvailable()) {
            Toast.makeText(
                requireContext(), "Internet is not available",
                Toast.LENGTH_SHORT
            ).show()

            progressBar!!.visibility = View.GONE
            view.rv_papers.visibility = View.GONE
            view.tv_no_papers.visibility = View.VISIBLE
        }

        val layoutManager = GridLayoutManager(view.context, 2)
        layoutManager.orientation = GridLayoutManager.VERTICAL
        view.rv_papers.layoutManager = layoutManager

        databaseReference = FirebaseDatabase.getInstance().reference.child(
            FirebaseAuth.getInstance().currentUser?.uid!!
        ).child("papers")

        databaseReference.orderByKey().limitToFirst(limit).addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val data: Map<String, Object> = snapshot.value as Map<String, Object>
                if (dataCnt < limit-1){
                    paperList.add(getPaperObj(data))
                    // questionList.reverse()
                    adapter = PaperAdapter(view.context, paperList)
                    view.rv_papers.adapter = adapter
                    dataCnt += 1
                }else{
                    lastKey = data["key"] as String
                    Log.i(TAG, "Last Key Stored: $lastKey. Data Count = $dataCnt")
                    dataCnt = 0
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                paperList.forEach {
                    if(it.key == snapshot.key){
                        it.name = snapshot.child("name").value as String
                        it.marks = snapshot.child("marks").value as String
                        Log.i(TAG, "Paper as ${it.key} Updated")
                    }
                }

                adapter = PaperAdapter(view.context, paperList)
                view.rv_papers.adapter = adapter
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
                    view.rv_papers.visibility = View.GONE
                    view.tv_no_papers.visibility = View.VISIBLE
                } else {
                    progressBar!!.visibility = View.GONE
                    view.rv_papers.visibility = View.VISIBLE
                    view.tv_no_papers.visibility = View.GONE
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        view.rv_papers.addOnScrollListener(object: RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                val visibleItemCount:Int = layoutManager.childCount
                val pastVisibleItem:Int = layoutManager.findFirstVisibleItemPosition()
                val total = adapter.itemCount

                //  Check if is Loading
                if(isLoading){
                    //  Check if we reached bottom or not
                    if(( visibleItemCount + pastVisibleItem ) >= total){
                        isLoading = false
                        if(paperList.size == limit-1){
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

        val mAddP: Button? = view?.findViewById(R.id.fab_add_paper)
        mAddP?.setOnClickListener {
            startActivity(Intent(view.context, EditPaperActivity::class.java))
        }
        return view
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    fun getData(){
        databaseReference.orderByKey().startAt(lastKey).limitToFirst(limit).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.i(TAG, "Adding Data|| Total children: ${snapshot.childrenCount}")
                var lastChild: DataSnapshot? = null
                for(childObj in snapshot.children){
                    lastChild = childObj
                    if(dataCnt < limit-1){
                        val name = childObj.child("name").value.toString()
                        val marks = childObj.child("marks").value.toString()
                        val key = childObj.key

                        Log.i(TAG, "Child Added: $key")
                        paperList.add(Papers(key!!, name, marks))
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

    private fun getPaperObj(data: Map<String, Object>): Papers {
        val key = data["key"] as String?
        val name = data["name"] as String
        val marks = data["marks"] as String

        return Papers(key, name, marks)
    }
}