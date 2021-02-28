package com.example.makepaper

import android.content.Context
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
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_file.view.*


class FileFragment : Fragment() {

    private val TAG = "FileFragment"
    private var progressBar: ProgressBar? = null
    var paperList = ArrayList<Papers>()

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

        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference.child(
            FirebaseAuth.getInstance().currentUser?.uid!!
        ).child("papers")

        databaseReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val data: Map<String, Object> = snapshot.value as Map<String, Object>
                paperList.add(getPaperObj(data))
                paperList.reverse()
                val adapter = PaperAdapter(view.context, paperList)
                view.rv_papers.adapter = adapter
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

        val mAddP: Button? = view?.findViewById(R.id.btn_generate_question_paper)
        mAddP?.setOnClickListener {
            startActivity(Intent(view.context, AddPaper::class.java))
        }
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
}