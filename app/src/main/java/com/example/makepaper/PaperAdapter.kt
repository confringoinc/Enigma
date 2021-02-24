package com.example.makepaper

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.paper_list.view.*

class PaperAdapter(val context: Context?, val papers: List<Papers>): RecyclerView.Adapter<PaperAdapter.MyViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.paper_list, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return papers.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val papers = papers[position]
        holder.setData(papers)
    }

    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun setData(paper: Papers){
            itemView.tv_paper.text = paper.name
            val marksText = "Marks: " + paper.marks
            itemView.tv_marks.text = marksText
        }
    }
}