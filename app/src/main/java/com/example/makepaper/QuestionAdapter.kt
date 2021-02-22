package com.example.makepaper

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.question_list.view.*

class QuestionAdapter(val context: Context?, val ques: List<Questions>): RecyclerView.Adapter<QuestionAdapter.MyViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        //  Get layout from the context and attach it to parent
        val view = LayoutInflater.from(context).inflate(R.layout.question_list, parent, false)

        //  Return MyViewHolder object
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        //  Return the total items in list
        return ques.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        //  Storing positionth question in object
        val ques = ques[position]

        //  Set it to the view
        holder.setData(ques)
    }

    //  Inner class for handling Data
    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun setData(que: Questions){
            //  Set question from que(Questions) object
            itemView.tv_question.text = que.question
            val marksText = "Marks: " + que.marks
            itemView.tv_marks.text = marksText
            itemView.tv_category.text = que.category.toString()
        }
    }
}