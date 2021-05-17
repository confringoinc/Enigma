package com.example.makepaper

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class PaperProperties : AppCompatActivity() {
    private var totalQuestions = 0
    var questionList = ArrayList<Questions>()
    private var progressBar: ProgressBar? = null
    val TAG = "PeperProperties"
    lateinit var adapter: AddQuestionAdapter
    lateinit var databaseReference:DatabaseReference
    private val STORAGE_PERMISSION_CODE = 101
    private val REQUEST_PERMISSIONS = 1

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paper_properties)

        val paperName: String = intent.getStringExtra("name").toString()
        val paperMarks: String = intent.getStringExtra("marks").toString()
        val paperKey: String = intent.getStringExtra("key").toString()
        tv_add_paper.text = paperName
        tv_add_paper_marks.text = "Marks: $paperMarks"

        progressBar = findViewById(R.id.progress_bar)
        progressBar!!.visibility = View.GONE

        btn_back.setOnClickListener {
            onBackPressed()
        }

        val layoutManager = LinearLayoutManager(applicationContext)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        rv_added.layoutManager = layoutManager

        databaseReference = FirebaseDatabase.getInstance().reference.child(FirebaseAuth.getInstance().currentUser?.uid!!).child("papers").child(paperKey).child("questions")

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
                adapter = AddQuestionAdapter(this@PaperProperties, questionList)
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
                generatePaper(paperName, paperMarks)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun generatePaper(paperName: String, paperMarks: String) {
        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE)
        var srNo = 1
        val folder = File(applicationContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "/Question Paper Maker")
        if (!folder.exists()) {
            folder.mkdirs()
        }

        val df = SimpleDateFormat("yyyyMMddkmmss", Locale.getDefault())
        val formattedDate: String = df.format(Calendar.getInstance().time)
        val fileName = File(folder, "$paperName $formattedDate.pdf")

        val document = Document()
        PdfWriter.getInstance(document, FileOutputStream(fileName))
        document.open()

        document.pageSize = PageSize.A4
        document.addCreationDate()

        val table = PdfPTable(3)
        table.widthPercentage = 100f

        //  First add Institute name
        addHeader(table, et_institute.text.toString(), 16.0f, true, Element.ALIGN_CENTER)
        addBlankRow(table)      //  Add blank row

        table.addCell(addCell("", 0f, false))   //  First blank cell
        table.addCell(addCell(paperName, 12f, true, Element.ALIGN_CENTER))   //  Second cell with header
        table.addCell(addCell("[$paperMarks]", 12f, false))
        addBlankRow(table)      //  Add blank row

        //  Secondly add Instruction in it
        if(et_paper_instruction.text.toString().isNotEmpty()) {
            addHeader(table, "Instructions:", 12f, true)
            for (ins in et_paper_instruction.text.toString().split("\n")) {
                addInstructions(table, ins, 12f, false)
            }
            addBlankRow(table)
        }

        for(a_question in questionList) {
            addRow(table, srNo.toString(), a_question)
            srNo += 1
        }

        val columnWidths = floatArrayOf(3f, 100f, 7f)
        table.setWidths(columnWidths)
        document.add(table)
        document.close()

        Toast.makeText(this, "PDF Generated", Toast.LENGTH_LONG).show()
        val file = File(
            this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "/Question Paper Maker/$paperName $formattedDate.pdf"
        )

        if(file.exists()) {
            openPdf(file, this)
        }
    }

    private fun addHeader(table: PdfPTable, header: String, size: Float, bold: Boolean = false, align: Int = Element.ALIGN_LEFT){
        table.addCell(addCell("", 0f, false))   //  First blank cell
        table.addCell(addCell(header, size, bold, align))   //  Second cell with header
        table.addCell(addCell("", 0f, false))
    }

    private fun addInstructions(table: PdfPTable, header: String, size: Float, bold: Boolean = false, align: Int = Element.ALIGN_LEFT) {
        table.addCell(addCell("\u2022", 12f, true))   //  First blank cell
        table.addCell(addCell(header, size, bold, align))   //  Second cell with header
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
        table.addCell(addCell("$srNo. ", 12f, false))
        table.addCell(addCell(quesObj.question!! + "\n" + quesObj.category.toString(), 12f, false))
        table.addCell(addCell("[" + quesObj.marks!! + "]", 12f, false))
        addBlankRow(table)
    }

    private fun addCell(content: String, size:Float, bold:Boolean, align: Int = Element.ALIGN_LEFT): PdfPCell{
        var paraFont: Font? = null
        paraFont = if(bold){
            Font(Font.FontFamily.TIMES_ROMAN, size, Font.BOLD)
        } else{
            Font(Font.FontFamily.TIMES_ROMAN, size)
        }
        val srNo = Paragraph(content, paraFont)
        val cell1 = PdfPCell(srNo)
        cell1.border = Rectangle.NO_BORDER
        cell1.verticalAlignment = Element.ALIGN_CENTER
        cell1.horizontalAlignment = align
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

    private fun checkPermission(permission: String, requestCode: Int) {
        // Checking if permission is not granted
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    // Try to open PDF and return false if it is not possible.
    private fun openPdf(file: File, context: Context): Boolean {
        val uri = getUriFromFile(file, context)
        if (uri == null) {
            return false
        } else {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            // Validate that the device can open your File.
            val activityInfo = intent.resolveActivityInfo(context.packageManager, intent.flags)
            return if (activityInfo?.exported == true) {
                context.startActivity(Intent.createChooser(intent, "Open PDF with"))
                true
            } else {
                false
            }
        }
    }

    // Get URI from file.
    private fun getUriFromFile(file: File, context: Context): Uri? =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Uri.fromFile(file)
        } else {
            try {
                FileProvider.getUriForFile(context, context.packageName, file)
            } catch (e: Exception) {
                if (e.message?.contains("ProviderInfo.loadXmlMetaData") == true) {
                    throw Error("FileProvider doesn't exist or has no permissions")
                } else {
                    throw e
                }
            }
        }
}