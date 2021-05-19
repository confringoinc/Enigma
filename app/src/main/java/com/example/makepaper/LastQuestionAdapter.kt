package com.example.makepaper

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
import kotlinx.android.synthetic.main.last_question_list.view.*


class LastQuestionAdapter(val context: Context?, private var questions: MutableList<Questions>): RecyclerView.Adapter<LastQuestionAdapter.MyViewHolder>(){

    val databaseReference = FirebaseDatabase.getInstance().reference.child(FirebaseAuth.getInstance().currentUser?.uid!!).child("papers")
    val TAG = "LastQuestionAdapter"
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.last_question_list, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return questions.size
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val ques = questions[position]
        holder.setData(ques)
    }

    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        @RequiresApi(Build.VERSION_CODES.M)

        fun setData(question: Questions) {

            itemView.tv_question.text = question.question
            val marksText = "Marks: " + question.marks
            itemView.tv_marks.text = marksText
            itemView.tv_category.text = question.category.toString()

            itemView.ib_options.setOnClickListener {
                val popup = PopupMenu(itemView.context, itemView.ib_options)
                popup.menuInflater.inflate(R.menu.option, popup.menu)

                popup.setOnMenuItemClickListener { item ->
                    when(item.title) {
                        "Edit" -> {
                            val query: Query = databaseReference.child(question.paperkey!!).child("questions").orderByChild("question").equalTo(
                                question.question
                            )
                            query.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    for (snapshot in dataSnapshot.children) {
                                        val intent = Intent(context, AddQuestion::class.java)
                                        intent.putExtra("quesKey", snapshot.key.toString())
                                        intent.putExtra(
                                            "etQuestion",
                                            snapshot.child("question").value.toString()
                                        )
                                        intent.putExtra(
                                            "etMarks",
                                            snapshot.child("marks").value.toString()
                                        )
                                        intent.putExtra(
                                            "cb1",
                                            snapshot.child("category").child("0").value.toString()
                                        )
                                        intent.putExtra(
                                            "cb2",
                                            snapshot.child("category").child("1").value.toString()
                                        )
                                        intent.putExtra(
                                            "cb3",
                                            snapshot.child("category").child("2").value.toString()
                                        )
                                        intent.putExtra("paperKey", question.paperkey)
                                        itemView.context.startActivity(intent)
                                    }
                                    notifyItemChanged(adapterPosition)
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
                                    deleteText.text = question.question

                                    btnDelete.setOnClickListener {

                                        val query: Query = databaseReference.child(question.paperkey!!).child("questions").orderByChild("question").equalTo(
                                            question.question
                                        )

                                        query.addListenerForSingleValueEvent(object :
                                            ValueEventListener {
                                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                for (snapshot in dataSnapshot.children) {
                                                    snapshot.ref.removeValue()
                                                }
                                                questions.removeAt(adapterPosition)
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