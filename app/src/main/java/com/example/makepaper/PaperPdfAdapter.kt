package com.example.makepaper

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.paper_list.view.*
import java.io.File


class PaperPdfAdapter(val context: Context?, private val papers: ArrayList<PaperPdfs>): RecyclerView.Adapter<PaperPdfAdapter.MyViewHolder>(){

    val databaseReference = FirebaseDatabase.getInstance().reference.child(FirebaseAuth.getInstance().currentUser?.uid!!).child("papers")
    val PICK_PDF_FILE = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.paper_pdf_list, parent, false)

        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

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
        fun setData(paper: PaperPdfs){
            itemView.tv_paper.text = paper.name

            itemView.setOnClickListener {
                val file = File(
                    itemView.context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                    "/Question Paper Maker/" + paper.name
                )
                val path = Uri.fromFile(file)
                val pdfOpenintent = Intent(Intent.ACTION_VIEW)
                pdfOpenintent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                pdfOpenintent.setDataAndType(path, "application/pdf")
                Intent.createChooser(pdfOpenintent, "Open with")
                try {
                    itemView.context.startActivity(pdfOpenintent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(
                        context, "Install PDF reader to open PDF file",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            itemView.ib_options.setOnClickListener {
                val popup = PopupMenu(itemView.context, itemView.ib_options)
                popup.menuInflater.inflate(R.menu.option, popup.menu)

                popup.setOnMenuItemClickListener { item ->
                    when(item.title) {
                        "Delete" -> {
                            val dir = File(itemView.context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                                "Question Paper Maker")
                            val file = File(dir, paper.name!!)
                            file.delete()
                            papers.removeAt(adapterPosition)
                            notifyItemRemoved(adapterPosition)

                            Toast.makeText(
                                context, "Question Paper Deleted",
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                    }
                    true
                }
                popup.gravity = Gravity.END
                popup.show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun openFile(pickerInitialUri: Uri) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }

        startActivityForResult(context as Activity, intent, PICK_PDF_FILE, null)
    }
}