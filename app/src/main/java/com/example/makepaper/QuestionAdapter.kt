package com.example.makepaper

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.question_list.view.*

class QuestionAdapter(val context: Context?, val questions: List<Questions>): RecyclerView.Adapter<QuestionAdapter.MyViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.question_list, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return questions.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val ques = questions[position]
        holder.setData(ques)
    }

    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun setData(question: Questions){
            itemView.tv_question.text = question.question
            val marksText = "Marks: " + question.marks
            itemView.tv_marks.text = marksText
            itemView.tv_category.text = question.category.toString()
        }
    }
}