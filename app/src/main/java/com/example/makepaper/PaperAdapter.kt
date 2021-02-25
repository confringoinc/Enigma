package com.example.makepaper

import android.content.Context
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.paper_list.view.*

class PaperAdapter(val context: Context?, private val papers: List<Papers>): RecyclerView.Adapter<PaperAdapter.MyViewHolder>(){

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

    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        @RequiresApi(Build.VERSION_CODES.M)
        fun setData(paper: Papers){
            itemView.tv_paper.text = paper.name
            val marksText = "Marks: " + paper.marks
            itemView.tv_marks.text = marksText

            itemView.ib_options.setOnClickListener {
                val popup = PopupMenu(itemView.context, itemView.ib_options)
                popup.menuInflater.inflate(R.menu.option, popup.menu)

                popup.setOnMenuItemClickListener { item ->
                    Toast.makeText(
                        itemView.context,
                        "You Clicked : " + item.title,
                        Toast.LENGTH_SHORT
                    ).show()
                    true
                }
                popup.gravity = Gravity.END
                popup.show()
            }
        }
    }
}