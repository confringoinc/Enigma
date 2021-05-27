package com.confringoinc.enigma

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import java.io.File

class HomeFragment : Fragment() {

    private val TAG = "HomeFragment"
    private var progressBarPaper: ProgressBar? = null
    var paperList = ArrayList<Papers>()
    var pdfList = ArrayList<PaperPdfs>()
    lateinit var paperAdapter: PaperAdapter
    lateinit var paperPdfAdapter: PaperPdfAdapter

    companion object {
        lateinit var auth: FirebaseAuth
        fun newInstance(): HomeFragment = HomeFragment()
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        user?.let {
            val name = user.displayName
            val firstName = name?.split(" ")
            view?.findViewById<TextView?>(R.id.tv_name)?.text = firstName?.get(0)
        }

        view.tv_no_papers.visibility = View.GONE
        progressBarPaper = view.progress_bar_paper
        progressBarPaper!!.visibility = View.VISIBLE

        if (!isNetworkAvailable()) {
            Toast.makeText(
                requireContext(), "Internet is not available",
                Toast.LENGTH_SHORT
            ).show()

            progressBarPaper!!.visibility = View.GONE
            view.tv_no_papers.visibility = View.VISIBLE
        }

        view.rv_papers_pdf.layoutManager =
            object : LinearLayoutManager(view.context, HORIZONTAL, false) {
                override fun checkLayoutParams(lp: RecyclerView.LayoutParams): Boolean {
                    lp.width = (width / 2.15).toInt()
                    return true
                }
            }

        val directoryPdf = File(
            requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "/Question Paper Maker"
        )
        if (directoryPdf.listFiles()?.isEmpty() == false) {
            val newFile: Array<File> = directoryPdf.listFiles()!!
            pdfList.clear()

            for (i in newFile.indices) {
                val path = "file://" + newFile[i].absolutePath
                val filename = path.substring(path.lastIndexOf("/") + 1)
                pdfList.add(PaperPdfs(path, filename))
            }
        }

        pdfList.asReversed()
        paperPdfAdapter = PaperPdfAdapter(this@HomeFragment.context, pdfList)
        view.rv_papers_pdf.adapter = paperPdfAdapter

        if (pdfList.isNullOrEmpty()) {
            view.tv_top_papers_pdf.visibility = View.GONE
            view.rv_papers_pdf.visibility = View.GONE
        }

        val layoutManager = GridLayoutManager(view.context, 2)
        layoutManager.orientation = GridLayoutManager.VERTICAL
        view.rv_papers.layoutManager = layoutManager

        val databaseReferencePaper: DatabaseReference =
            FirebaseDatabase.getInstance().reference.child(FirebaseAuth.getInstance().currentUser?.uid!!)
                .child("papers")
        val queryPaper: Query = databaseReferencePaper.limitToLast(10)

        queryPaper.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val data: Map<String, Object> = snapshot.value as Map<String, Object>
                paperList.add(getPaperObj(data))
                paperAdapter = PaperAdapter(this@HomeFragment.context, paperList.asReversed())
                view.rv_papers.adapter = paperAdapter
                progressBarPaper!!.visibility = View.GONE
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                paperList.forEach {
                    if (it.key == snapshot.key) {
                        it.name = snapshot.child("name").value as String
                        it.marks = snapshot.child("marks").value as String
                        Log.i(TAG, "Paper as ${it.key} Updated")
                    }
                }

                paperAdapter = PaperAdapter(view.context, paperList.asReversed())
                view.rv_papers.adapter = paperAdapter
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
        return view
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun getPaperObj(data: Map<String, Object>): Papers {
        val key = data["key"] as String?
        val name = data["name"] as String
        val marks = data["marks"] as String

        return Papers(key, name, marks)
    }

}