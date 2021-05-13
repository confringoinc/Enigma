package com.example.makepaper

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.CompoundButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.android.synthetic.main.activity_paper_properties.*
import java.io.File
import java.io.FileOutputStream

class PaperProperties : AppCompatActivity() {
    private var totalQuestions = 0
    var questionList = ArrayList<Questions>()
    private var progressBar: ProgressBar? = null
    val TAG = "PeperProperties"
    lateinit var adapter: AddQuestionAdapter
    lateinit var databaseReference:DatabaseReference

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paper_properties)

        val paperName: String = intent.getStringExtra("name").toString()
        val paperMarks: String = intent.getStringExtra("marks").toString()
        val paperKey: String = intent.getStringExtra("key").toString()
        tv_add_paper.text = paperName
        tv_add_paper_marks.text = "Marks: " + paperMarks

        progressBar = findViewById(R.id.progress_bar)
        progressBar!!.visibility = View.GONE

        btn_back.setOnClickListener {
            onBackPressed()
        }

        et_institute.isEnabled = false
        et_institute.isFocusable = false
        et_paper_instruction.isEnabled = false
        et_paper_instruction.isFocusable = false

        sw_institute.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
            if(!b) {
                et_institute.isEnabled = false
                et_institute.isFocusable = false
            }
            else {
                et_institute.isEnabled = true
                et_institute.isFocusable = true
                et_institute.isFocusableInTouchMode = true
                et_institute.isCursorVisible = true
            }
        }

        sw_instruction.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
            if(!b) {
                et_paper_instruction.isEnabled = false
                et_paper_instruction.isFocusable = false
            }
            else {
                et_paper_instruction.isEnabled = true
                et_paper_instruction.isFocusable = true
                et_paper_instruction.isFocusableInTouchMode = true
                et_paper_instruction.isCursorVisible = true
            }
        }

        val layoutManager = LinearLayoutManager(applicationContext)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        rv_added.layoutManager = layoutManager

        databaseReference = FirebaseDatabase.getInstance().reference.child(FirebaseAuth.getInstance().currentUser?.uid!!).child("papers")
                            .child(paperKey).child("questions")

        //  Adding a value event Listener to check if user has Question or not.
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Log.i(TAG, "No DataSnapshot Exists")
                    progressBar!!.visibility = View.GONE
                    rv_added.visibility = View.GONE
                    tv_no_questions.visibility = View.VISIBLE
                } else {
                    totalQuestions = dataSnapshot.childrenCount.toInt()
                    Log.i(TAG, "DataSnapshot Exists with total Children: $totalQuestions")
                    progressBar!!.visibility = View.GONE
                    rv_added.visibility = View.VISIBLE
                    tv_no_questions.visibility = View.GONE
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })

        databaseReference.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val data: Map<String, Object> = snapshot.value as Map<String, Object>
                questionList.add(getQuestionObj(data))
                questionList.reverse()
                adapter = AddQuestionAdapter(applicationContext, questionList)
                rv_added.adapter = adapter
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                questionList.forEach {
                    if(it.key == snapshot.key){
                        it.question = snapshot.child("question").value as String
                        it.marks = snapshot.child("marks").value as String
                        it.category = snapshot.child("category").value as List<String>
                        Log.i(TAG, "Question as ${it.key} Updated")
                    }
                }

                adapter.notifyDataSetChanged()
                rv_added.adapter = adapter
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
                progressBar!!.visibility = View.GONE
            }

        })

        btn_generate.setOnClickListener {

            if(!isNetworkAvailable()) {
                Toast.makeText(
                        baseContext, "Internet is not available",
                        Toast.LENGTH_SHORT
                ).show()
            }
            else {
                if(sw_institute.isEnabled && validateInstitute()) {
                    generatePaper()
                }
                else if(sw_instruction.isEnabled && validateInstruction()) {
                    generatePaper()
                }
                else {
                    generatePaper()
                }
            }
        }
    }

    private fun generatePaper() {
        var srNo = 1
        val fileName:String = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/myPdf.pdf"
        Log.i(TAG, "FilePath = " + fileName)
        
        val document = Document()
        PdfWriter.getInstance(document, FileOutputStream(fileName))
        document.open()
        
        val table = PdfPTable(3)
        table.widthPercentage = 100f

        //  First add Institute name
        addHeader(table, et_institute.text.toString(), 100.0f)
        addBlankRow(table)      //  Add blank row

        //  Secondly add Instruction in it
        addHeader(table, et_paper_instruction.text.toString(), 45f)
        addBlankRow(table)      //  Add blank row

        for(a_question in questionList) {
            addRow(table, srNo.toString(), a_question)
            srNo += 1
        }

        document.add(table)
        document.close()
        
        Toast.makeText(baseContext, "Pdf Generated", Toast.LENGTH_LONG).show()
    }

    private fun addHeader(table: PdfPTable, header: String, size: Float){
        table.addCell(addCell("", 0f, false))   //  First blank cell
        table.addCell(addCell(header, size, true))   //  Second cell with header
        table.addCell(addCell("", 0f, false))
    }

    //  This function add 3 cells with no content as a blank row
    private fun addBlankRow(table:PdfPTable){
        val blankRow = PdfPCell(Paragraph(" "))
        blankRow.colspan = 3
        blankRow.border = Rectangle.NO_BORDER
        table.addCell(blankRow)
    }

    private fun addRow(table: PdfPTable, srNo:String, quesObj: Questions){
        Log.i(TAG, "Adding ${srNo} | Question = ${quesObj}")
        table.addCell(addCell(srNo, 30f, false))
        table.addCell(addCell(quesObj.question!!, 30f, false))
        table.addCell(addCell(quesObj.category.toString(), 30f, false))
    }

    private fun addCell(content: String, size:Float, bold:Boolean): PdfPCell{
        val srNo = Paragraph(content)
        var paraFont: Font? = null
        if(bold){
            paraFont = Font(Font.FontFamily.TIMES_ROMAN, size)
        } else{
            //  Adding bold
            paraFont = Font(Font.FontFamily.TIMES_ROMAN, size, Font.BOLD)
        }
        srNo.font = paraFont
        val cell1 = PdfPCell(srNo)
        cell1.border = Rectangle.NO_BORDER
        cell1.verticalAlignment = PdfPCell.ALIGN_CENTER
        return cell1
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun getQuestionObj(data: Map<String, Object>): Questions {
        val key = data["key"] as String?
        val question = data["question"] as String
        val marks = data["marks"] as String
        val category = data["category"] as List<String>

        return Questions(key, question, marks, category)
    }

    private fun validateInstitute(): Boolean {
        val instituteName = et_institute.text.toString()

        if(instituteName.isEmpty()){
            et_institute.error = "Please enter institute name"
            et_institute.requestFocus()
            progressBar!!.visibility = View.GONE
            return false
        }

        return true
    }

    private fun validateInstruction(): Boolean {
        et_paper_instruction.text
        val instruction = et_paper_instruction.text.toString()

        if(instruction.isEmpty()){
            et_paper_instruction.error = "Please enter instructions"
            et_paper_instruction.requestFocus()
            progressBar!!.visibility = View.GONE
            return false
        }

        return true
    }
}