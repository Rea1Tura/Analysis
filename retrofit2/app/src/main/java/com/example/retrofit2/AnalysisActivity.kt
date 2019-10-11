package com.example.retrofit2

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.widget.DatePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_analysis.*
import okhttp3.ResponseBody
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.*
import java.lang.Exception
import java.lang.StringBuilder
import java.math.BigInteger
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


class AnalysisActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)
        val mName = File("${applicationContext.filesDir}/XML/date.xml")
        mName.deleteRecursively()
        showDatePickerDialog()
    }

    override fun onRestart() {
        val mName = File("${applicationContext.filesDir}/XML/analysis.xml")
        executeMessage(mName.readText())
        super.onRestart()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId) {
            R.id.app_bar_calendar -> {
                showDatePickerDialog()
                return true
            }
            //R.id.app_bar_search -> {
            //    Toast.makeText(this@AnalysisActivity, "You're searching", Toast.LENGTH_SHORT).show()
            //    return true
            //}
        }
        return true
    }

    private fun showDatePickerDialog() {
        val pref = getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val recyclerView: RecyclerView = my_recycler_view
        val newFragment = DatePickerFragment(pref.getString("login", ""), pref.getString("password", ""),
            recyclerView, applicationContext)
        newFragment.show(supportFragmentManager, "datePicker")
    }

    private fun executeMessage(msg: String) : String {
        val recyclerView: RecyclerView = my_recycler_view
        val builder = Retrofit.Builder()
            .baseUrl("http://tsk-web-agro.kdvm.ru:8887/")
            .addConverterFactory(ScalarsConverterFactory.create())

        val retrofit = builder.build()
        val userClient = retrofit.create(UserClient::class.java)
        val call = userClient.analysisGet(msg).enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val m_data = response.body()!!.string()
                val mFol = File(applicationContext.filesDir, "XML")
                val mNam = File(mFol, "analys.xml")
                val wr = FileWriter(mNam)
                wr.append(m_data)
                wr.close()
                val mFolder = File(applicationContext.filesDir, "XML")
                if(!mFolder.exists())
                    mFolder.mkdir()
                val mName = File(mFolder, "responseDoc.xml")
                val write = FileWriter(mName)
                write.append(m_data)
                write.close()
                recyclerView.layoutManager = LinearLayoutManager(this@AnalysisActivity)
                recyclerView.adapter = DocAdapter(parseXML())
                val itemDecor = DividerItemDecoration(recyclerView.context, 1)
                recyclerView.addItemDecoration(itemDecor)
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@AnalysisActivity, "Something is wrong", Toast.LENGTH_LONG).show()
            }
        })
        return call.toString()
    }

    private fun parseXML(): ArrayList<Test> {
        val parserFactory = XmlPullParserFactory.newInstance()
        val parser = parserFactory.newPullParser()
        val inputStream =
            FileInputStream("${applicationContext.filesDir}/XML/responseDoc.xml")
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
                        if ("doc" == eltName) {
                            currentTest = Test()
                            tests.add(currentTest)
                        } else if (currentTest != null) {
                            when {
                                "id" == eltName -> currentTest!!.id = parser.nextText()
                                "num_doc" == eltName -> currentTest!!.num_doc = parser.nextText()
                                "auto" == eltName -> currentTest!!.auto = parser.nextText()
                                "name_sku" == eltName -> currentTest!!.name_sku = parser.nextText()
                                "state" == eltName -> currentTest!!.state = parser.nextText()
                                "stock" == eltName -> currentTest!!.stock = parser.nextText()
                                "param" == eltName -> {
                                    currentParams = Params()
                                    currentTest.params!!.add(currentParams)
                                }
                                currentTest!!.params != null -> when (eltName) {
                                    "id_param" -> currentParams!!.id_param = parser.nextText()
                                    "paramname" -> currentParams!!.name = parser.nextText()
                                    "value" -> currentParams!!.value = parser.nextText()
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }

            eventType = parser.next()
        }
        return tests
    }

}

class DatePickerFragment(var login: String? = null, var password: String? = null, var recyclerView: RecyclerView,
                         var applicationContext: Context,
                         var date: String? = null) : DialogFragment(), DatePickerDialog.OnDateSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val file = File("${applicationContext.filesDir}/XML/date.xml")
        val c = Calendar.getInstance()
        var year = c.get(Calendar.YEAR)
        var month = c.get(Calendar.MONTH)
        var day = c.get(Calendar.DAY_OF_MONTH)
        if(file.exists()) {
            val date = file.readText()
            year = date.substring(0, 4).toInt()
            month = date.substring(5, 7).toInt() - 1
            day = date.substring(8).toInt()
        }
        return DatePickerDialog(context!!, this, year, month, day)
    }

    private fun parseXML(): ArrayList<Test> {
        val parserFactory = XmlPullParserFactory.newInstance()
        val parser = parserFactory.newPullParser()
        val inputStream =
            FileInputStream("${applicationContext.filesDir}/XML/responseDoc.xml")
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
                        if ("doc" == eltName) {
                            currentTest = Test()
                            tests.add(currentTest)
                        } else if (currentTest != null) {
                            when {
                                "id" == eltName -> currentTest!!.id = parser.nextText()
                                "num_doc" == eltName -> currentTest!!.num_doc = parser.nextText()
                                "auto" == eltName -> currentTest!!.auto = parser.nextText()
                                "name_sku" == eltName -> currentTest!!.name_sku = parser.nextText()
                                "state" == eltName -> currentTest!!.state = parser.nextText()
                                "stock" == eltName -> currentTest!!.stock = parser.nextText()
                                "param" == eltName -> {
                                    currentParams = Params()
                                    currentTest.params!!.add(currentParams)
                                }
                                currentTest!!.params != null -> when (eltName) {
                                    "id_param" -> currentParams!!.id_param = parser.nextText()
                                    "paramname" -> currentParams!!.name = parser.nextText()
                                    "value" -> currentParams!!.value = parser.nextText()
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
            }

            eventType = parser.next()
        }
        return tests
    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        var month = "${p2 + 1}"
        var day = "$p3"
        if (p2 < 9)
            month = "0${p2 + 1}"
        if (p3 < 10)
            day = "0$p3"
        date = "$p1-$month-$day"
        val msg =
            "<?xml version=\"1.0\"?>\n" +
                    "<import>\n" +
                    "    <auth>\n" +
                    "        <login>" + login + "</login>\n" +
                    "        <password>" + password + "</password>\n" +
                    "        <apk>" + BuildConfig.VERSION_CODE.toString() + "</apk>\n" +
                    "    </auth>\n" +
                    "    <date>" + date + "</date>\n" +
                    "</import>"
        val mFold = File(applicationContext.filesDir, "XML")
        val mNam = File(mFold, "date.xml")
        val writ = FileWriter(mNam)
        writ.append(date)
        writ.close()
        val mFolder = File(applicationContext.filesDir, "XML")
        val mName = File(mFolder, "analysis.xml")
        val write = FileWriter(mName)
        write.append(msg)
        write.close()
        executeMessage(msg)
    }

    private fun executeMessage(msg: String) : String {

        val builder = Retrofit.Builder()
            .baseUrl("http://tsk-web-agro.kdvm.ru:8887/")
            .addConverterFactory(ScalarsConverterFactory.create())

        val retrofit = builder.build()
        val userClient = retrofit.create(UserClient::class.java)
        val call = userClient.analysisGet(msg).enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val m_data = response.body()!!.string()
                val mFolder = File(applicationContext.filesDir, "XML")
                if(!mFolder.exists())
                    mFolder.mkdir()
                val mName = File(mFolder, "responseDoc.xml")
                val write = FileWriter(mName)
                write.append(m_data)
                write.close()
                recyclerView.layoutManager = LinearLayoutManager(activity)
                recyclerView.adapter = DocAdapter(parseXML())
                val itemDecor = DividerItemDecoration(recyclerView.context, 1)
                recyclerView.addItemDecoration(itemDecor)
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(context!!, "Something is wrong", Toast.LENGTH_LONG).show()
            }
        })
        return call.toString()
    }
}
