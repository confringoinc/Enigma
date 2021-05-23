package com.example.makepaper

import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_data.*
import kotlinx.android.synthetic.main.fragment_data.view.*

class DataFragment : Fragment() {
    private val TAG = "DataFragment"
    private var progressBar: ProgressBar? = null
    var questionList = ArrayList<Questions>()

    private var totalQuestions = 0  //  Total question ofa particular user
    private val limit = 11
    private var isLoading = true
    private var lastKey:String? = null
    private var dataCnt = 0
    private var filterList = ArrayList<String>()

    lateinit var adapter: QuestionAdapter

    lateinit var databaseReference: DatabaseReference
    var popup: PopupMenu? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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
                    totalQuestions = dataSnapshot.childrenCount.toInt()
                    Log.i(TAG, "DataSnapshot Exists with total Children: $totalQuestions")
                    progressBar!!.visibility = View.GONE
                    view.rv_questions.visibility = View.VISIBLE
                    view.tv_no_questions.visibility = View.GONE
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        //  Call the databaseListener function once to get data
        databaseListener(filterList)

        //  Implement onScrollListener to handle Load More Functionality
        view.rv_questions.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                    isLoading = true
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {

                    val visibleItemCount: Int = layoutManager.childCount
                    val pastVisibleItem: Int = layoutManager.findFirstVisibleItemPosition()
                    val total = adapter.itemCount

                    // Scroll Down
                    if (fab_add_question.isShown) {
                        fab_add_question.hide()
                    }

                    //  Check if is Loading
                    if (isLoading) {
                        //  Check if we reached bottom or not
                        if ((visibleItemCount + pastVisibleItem) >= total && questionList.size < totalQuestions) {
                            Log.i(TAG, "Total $total || visible = $visibleItemCount || pastItem = $pastVisibleItem || questionSize = ${questionList.size}")
                            Log.i(TAG, "ListSize: ${questionList.size} || TotalQues = $totalQuestions")
                            isLoading = false

                            view!!.onSwipeUpPB.visibility = View.VISIBLE
                            // getData(filterList)

                            Handler().postDelayed({
                                getData(filterList)
                            }, 2000)
                            setAdapter(view)

                            //  Data count set here to 0
                            dataCnt = 0

                        } else{
                            //Toast.makeText(context, "Data Up to Date", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else if (dy < 0) {
                    // Scroll Up
                    if (!fab_add_question.isShown) {
                        fab_add_question.show()
                    }
                }
            }
        })

        val mAddQ: Button? = view?.findViewById(R.id.fab_add_question)
        mAddQ?.setOnClickListener {
            startActivity(Intent(view.context, AddQuestion::class.java))
        }

        return view
    }

    companion object {
        fun newInstance(): DataFragment = DataFragment()
    }

    fun getData(filters : ArrayList<String>){
        databaseReference.startAt(lastKey).orderByKey().addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                val data = getQuestionObj(snapshot.value as Map<String, Object>)
                if (dataCnt < limit - 1) {
                    Log.i(TAG, "Question -> ${data.question}")

                    if(filters.size != 0){
                        if(!data.category!!.any{ filter -> filter !in filters }){
                            questionList.add(data)
                            Log.i(TAG, "OnScrollEvent --> Child Added: ${data.question} | listSize = ${questionList.size}")
                            dataCnt += 1
                        }
                    } else{
                        questionList.add(data)
                        Log.i(TAG, "noFilter OnScrollEvent --> Child Added: ${data.question} | listSize = ${questionList.size}")
                        dataCnt += 1
                    }
                }

                //  if either dataCnt > 10 | or their is no more question left
                if(dataCnt > limit-1 || questionList.size == totalQuestions ){
                    lastKey = data.key

                    Log.i(TAG, "New LastKey: $lastKey")
                    setAdapter(view!!)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                Log.i(TAG, "onScroll ChildChanged Called for $snapshot")
                questionList.forEach {
                    if (it.key == snapshot.key) {
                        it.question = snapshot.child("question").value as String
                        it.marks = snapshot.child("marks").value as String
                        it.category = snapshot.child("category").value as List<String>
                        Log.i(TAG, "Question as ${it.key} Updated")
                    }
                }

                adapter.notifyDataSetChanged()
                view!!.rv_questions.adapter = adapter
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(view!!.context, "Failed to load Data", Toast.LENGTH_LONG).show()
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

        return Questions(key, question, marks, category, null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.filter, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mToolBar.inflateMenu(R.menu.filter)

        mToolBar.setOnMenuItemClickListener{
            it.isChecked = !it.isChecked

            if(it.isCheckable && it.isChecked && it.title !in filterList){
                filterList.add(it.title as String)
                Log.i(TAG, "Adding tag: ${it.title}")

                // questionList.clear()
                dataCnt = 0
                //  databaseListener(filterList)
                filterData(filterList)

            }

            if(it.isCheckable && !it.isChecked && it.title in filterList){
                filterList.remove(it.title as String)
                Log.i(TAG, "Removing tag: ${it.title}")

                questionList.clear()
                dataCnt = 0
                databaseListener(filterList)
            }

            return@setOnMenuItemClickListener when(it.itemId) {
                R.id.item1 -> {
                    Log.i(TAG, "Item1 selected = ${it.isChecked}")
                    Toast.makeText(context, "Selected ${it.title}", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.item2 -> {
                    Log.i(TAG, "Item2 selected = ${it.isChecked}")
                    Toast.makeText(context, "Selected ${it.title}", Toast.LENGTH_SHORT).show()
                    true
                }

                R.id.item3 -> {
                    Log.i(TAG, "Item3 selected = ${it.isChecked}")
                    Toast.makeText(context, "Selected ${it.title}", Toast.LENGTH_SHORT).show()
                    true
                }

                else -> false
            }
        }
    }

    //  This databaseListener will be used until the filter is not called
    //  After it another databaseListener will handle all method
    private fun databaseListener(filters : ArrayList<String>){
        Log.i(TAG, "Filter List = $filters")

        databaseReference.orderByKey().addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                //  val data: Map<String, Object> = snapshot.value as Map<String, Object>
                val data = getQuestionObj(snapshot.value as Map<String, Object>)

                dataCnt += 1
                if(questionList.size < limit-1) {
                    if(filters.size != 0){
                        if(!data.category!!.any { filter -> filter !in filters }){
                            questionList.add(data)
                            Log.i(TAG, "Adding Question -> ${data.question} || ${data.category} || size = ${questionList.size}")
                            //questionList.reverse()
                        }
                    }else{
                        questionList.add(data)
                        //questionList.reverse()
                    }
                }else{
                    if(lastKey == null){
                        adapter = QuestionAdapter(this@DataFragment.context, questionList)
                        view!!.rv_questions?.adapter = adapter

                        lastKey =  data.key
                        Log.i(TAG, "Last Question to add from -> ${data.question}")
                    }
                }

                if(dataCnt == totalQuestions){
                    setAdapter(view!!)
                }
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
                view?.rv_questions?.adapter = adapter
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

    private fun setAdapter(view : View){
        // adapter = QuestionAdapter(view!!.context, questionList)
        adapter.notifyDataSetChanged()

        //  view!!.rv_questions.adapter = adapter
        view.onSwipeUpPB.visibility = View.GONE
        isLoading = false

        Log.i(TAG, "Setting up adapter")
    }

    private fun filterData(filters : ArrayList<String>){
        Log.i(TAG, "In filter data -> ${filters}")
        val strFilter: String = filters.toString()
                        .replace("[", "")
                        .replace("]", "")

        Log.i(TAG, "String Filters = $strFilter")
//        databaseReference.child("category").orderByChild("0").equalTo("Anything")
//            .addChildEventListener(object : ChildEventListener{
//
//            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
//                //  val data = getQuestionObj(snapshot.value as Map<String, Object>)
//                val tempData = snapshot.value as Map<String, Object>
//                Log.i(TAG, "Question -> ${tempData["category"]}")
//            }
//
//            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
//                Log.i(TAG, "on child changed")
//            }
//
//            override fun onChildRemoved(snapshot: DataSnapshot) {
//                Log.i(TAG, "on child removed")
//            }
//
//            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
//                Log.i(TAG, "on moved called")
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                Log.i(TAG, "on Cancelled")
//            }
//
//        })
        databaseReference.orderByChild("category").equalTo("Anything", "0")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(ss in snapshot.children){
                        Log.i(TAG, "Question -> $ss")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.i(TAG, "Cancelled")
                }

            })
    }
}