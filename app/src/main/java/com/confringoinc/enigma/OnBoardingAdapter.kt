package com.confringoinc.enigma

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.layout_slides.view.*

class OnBoardingAdapter(val context: Context?, private val onBoardingItems: List<OnBoardingItem>) :
    RecyclerView.Adapter<OnBoardingAdapter.OnBoardingViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OnBoardingAdapter.OnBoardingViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_slides, parent, false)
        return OnBoardingViewHolder(view)
    }

    override fun onBindViewHolder(holder: OnBoardingViewHolder, position: Int) {
        holder.setData(onBoardingItems[position])
    }

    override fun getItemCount(): Int {
        return onBoardingItems.size
    }

    inner class OnBoardingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun setData(board: OnBoardingItem) {
            itemView.tv_on_boarding_title.text = board.title
            itemView.tv_on_boarding_description.text = board.description
            itemView.iv_on_Boarding.setImageResource(board.image)
        }
    }
}