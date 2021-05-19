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
import kotlinx.android.synthetic.main.question_list.view.*


class QuestionAdapter(val context: Context?, private var questions: MutableList<Questions>): RecyclerView.Adapter<QuestionAdapter.MyViewHolder>(){

    var databaseReference = FirebaseDatabase.getInstance().reference.child(FirebaseAuth.getInstance().currentUser?.uid!!).child(
        "questions"
    )
    val TAG = "QuestionAdapter"
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.question_list, parent, false)
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
                            val query: Query = databaseReference.orderByChild("question").equalTo(
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
                                    deleteText.text = question.question

                                    btnDelete.setOnClickListener {

                                        val query: Query =
                                            databaseReference.orderByChild("question").equalTo(
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

            var items = arrayOf("Cancel Selection")
            val key = ArrayList<String>()

            val databaseReferencePaper = FirebaseDatabase.getInstance().reference.child(
                FirebaseAuth.getInstance().currentUser?.uid!!
            ).child("papers")

            databaseReferencePaper.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val data: Map<String, Object> = snapshot.value as Map<String, Object>
                    items += data["name"].toString()
                    key.add(data["key"].toString())
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

            itemView.ib_add.setOnClickListener {

                val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(context)
                builder.setTitle("Select Question Paper")
                builder.setItems(
                    items
                ) { dialog, que ->
                    if (que > 0) {
                        val query: Query = databaseReferencePaper.child(key[que - 1]).child("questions").orderByChild("question").equalTo(question.question)
                        query.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.hasChildren()) {
                                    Toast.makeText(
                                        context,
                                        "Question already exists in paper",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    databaseReferencePaper.child(key[que - 1]).child("questions")
                                        .child(databaseReferencePaper.push().key!!).setValue(
                                            Questions(
                                                databaseReferencePaper.push().key,
                                                question.question,
                                                question.marks,
                                                question.category,
                                                null
                                            )
                                        )
                                        .addOnCompleteListener {

                                            Toast.makeText(
                                                context, "Question Added",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(
                                    context,
                                    "Question already exists in paper but not changed",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        })
                    }
                    dialog.cancel()
                }
                builder.create()?.show()
            }
        }
    }
}