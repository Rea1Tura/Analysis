package com.example.retrofit2

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_analysis.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AnalysisActivity : AppCompatActivity() {
    private val arrStr: Array<String> = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.LOCATION_HARDWARE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)
        val sdf = SimpleDateFormat("yyyy-mm-dd")
        val currentTime = sdf.format(Date())
                val recyclerView: RecyclerView = my_recycler_view
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = Adapter(parseXML())
        val itemDecor = DividerItemDecoration(recyclerView.context, 1)
        recyclerView.addItemDecoration(itemDecor)
    }

    private fun parseXML(): ArrayList<Test> {
        val parserFactory = XmlPullParserFactory.newInstance()
        val parser = parserFactory.newPullParser()
        val inputStream = FileInputStream("${Environment.getExternalStorageDirectory()}/XML/auth.xml")
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, null)
        return processParsing(parser)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun processParsing(parser: XmlPullParser): ArrayList<Test> {
        val tests = ArrayList<Test>()
        var eventType = parser.eventType
        var currentTest: Test? = null
        var currentParams: Params? = null
        while (eventType != XmlPullParser.END_DOCUMENT) {
            var eltName: String? = null
            try {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        eltName = parser.name
                        if ("test" == eltName) {
                            currentTest = Test()
                            tests.add(currentTest)
                        } else if (currentTest != null) {
                            if ("id" == eltName && currentTest!!.id == null) {
                                currentTest!!.id = parser.nextText()
                            } else if ("name" == eltName && currentTest!!.name == null) {
                                currentTest!!.name = parser.nextText()
                            } else if ("param" == eltName) {
                                currentParams = Params()
                                currentTest.params!!.add(currentParams)
                            } else if (currentTest!!.params != null) {
                                when (eltName) {
                                    "id" -> currentParams!!.id = parser.nextText()
                                    "name" -> currentParams!!.name = parser.nextText()
                                    "type" -> currentParams!!.type = parser.nextText()
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@AnalysisActivity, e.message, Toast.LENGTH_SHORT).show()
            }

            eventType = parser.next()
        }
        return tests
    }
}
