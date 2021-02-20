package com.example.makepaper

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_data.*
import kotlinx.android.synthetic.main.fragment_data.view.*

class DataFragment : Fragment() {
    private val TAG = "DataFragment"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_data, container, false)
        Log.i(TAG, "Entered in OnCreateView")

        val layoutManager = LinearLayoutManager(view.context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL

        view.rv_questions.layoutManager = layoutManager
        Log.i(TAG, "<-------Set rv_question.linearlayout-------->")

        val adapter = QuestionAdapter(view.context, questions_list.my_questions)
        Log.i(TAG, "Trying to set rv_question adapter")
        view.rv_questions.adapter = adapter

        if(rv_questions == null){
            Log.i(TAG, "rv_question is null")
        }

        val mAddQ: Button? = view?.findViewById(R.id.btn_add_question)
        mAddQ?.setOnClickListener {
            startActivity(Intent(view.context, AddQuestion::class.java))
        }
        return view
    }

    companion object {
        fun newInstance(): DataFragment = DataFragment()
    }
}