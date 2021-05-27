package com.confringoinc.enigma

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.paper_list.view.*

class PaperAdapter(val context: Context?, private val papers: MutableList<Papers>) :
    RecyclerView.Adapter<PaperAdapter.MyViewHolder>() {

    val databaseReference =
        FirebaseDatabase.getInstance().reference.child(FirebaseAuth.getInstance().currentUser?.uid!!)
            .child("papers")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.paper_list, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return papers.size
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val papers = papers[position]
        holder.setData(papers)
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @RequiresApi(Build.VERSION_CODES.M)
        fun setData(paper: Papers) {
            itemView.tv_paper.text = paper.name
            val marksText = "Marks: " + paper.marks
            itemView.tv_marks.text = marksText

            itemView.setOnClickListener {
                val intent = Intent(context, PaperProperties::class.java)
                intent.putExtra("key", paper.key)
                intent.putExtra("name", paper.name)
                intent.putExtra("marks", paper.marks)
                itemView.context.startActivity(intent)
            }

            itemView.ib_options.setOnClickListener {
                val popup = PopupMenu(itemView.context, itemView.ib_options)
                popup.menuInflater.inflate(R.menu.option, popup.menu)

                popup.setOnMenuItemClickListener { item ->
                    when (item.title) {
                        "Edit" -> {
                            val query: Query =
                                databaseReference.orderByChild("name").equalTo(paper.name)

                            query.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    for (snapshot in dataSnapshot.children) {
                                        val intent = Intent(context, AddPaperActivity::class.java)
                                        intent.putExtra("paperKey", snapshot.key.toString())
                                        intent.putExtra(
                                            "etName",
                                            snapshot.child("name").value.toString()
                                        )
                                        intent.putExtra(
                                            "etMarks",
                                            snapshot.child("marks").value.toString()
                                        )
                                        itemView.context.startActivity(intent)
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    Toast.makeText(
                                        context, "Failed to edit",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })
                        }
                        "Delete" -> {
                            Dialog(context!!)
                                .apply {
                                    setCancelable(true)
                                    setContentView(R.layout.dialog)

                                    val btnDelete = findViewById<TextView>(R.id.btn_delete)
                                    val btnCancel = findViewById<TextView>(R.id.btn_cancel)
                                    val deleteText = findViewById<TextView>(R.id.delete_text)
                                    deleteText.text = paper.name

                                    btnDelete.setOnClickListener {

                                        val query: Query = databaseReference.orderByChild("name")
                                            .equalTo(paper.name)

                                        query.addListenerForSingleValueEvent(object :
                                            ValueEventListener {
                                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                for (snapshot in dataSnapshot.children) {
                                                    snapshot.ref.removeValue()
                                                }
                                                papers.removeAt(adapterPosition)
                                                notifyItemRemoved(adapterPosition)
                                                dismiss()
                                            }

                                            override fun onCancelled(databaseError: DatabaseError) {
                                                Toast.makeText(
                                                    context, "Failed to delete",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        })
                                    }

                                    btnCancel.setOnClickListener {
                                        dismiss()
                                    }
                                    window?.setBackgroundDrawable(
                                        ColorDrawable(
                                            ContextCompat.getColor(
                                                context,
                                                R.color.transparent
                                            )
                                        )
                                    )
                                    show()
                                }
                        }
                    }
                    true
                }
                popup.gravity = Gravity.END
                popup.show()
            }
        }
    }
}