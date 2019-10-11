package com.example.retrofit2

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
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
import java.math.BigInteger
import java.security.MessageDigest

var result: String? = null

class LoginActivity : AppCompatActivity() {

    val stockName = ArrayList<String>()
    val stockId = ArrayList<String>()

    private val arrStr: Array<String> = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.LOCATION_HARDWARE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ActivityCompat.requestPermissions(this, arrStr, 0)
        val pref = getSharedPreferences("preferences", Context.MODE_PRIVATE)
        txtLogin.setText(pref.getString("login", ""))
        button.setOnClickListener {
            executeMessage()
        }
    }

    private fun executeMessage() : String {
        val mLogin = txtLogin.text.toString().toLowerCase()
        val mPassword = txtPassword.text.toString().md5().toUpperCase()
        val msg =
                "<?xml version=\"1.0\"?>\n" +
                "<import>\n" +
                "    <auth>\n" +
                "        <login>" + mLogin + "</login>\n" +
                "        <password>" + mPassword + "</password>\n" +
                "        <apk>" + BuildConfig.VERSION_CODE.toString() + "</apk>\n" +
                "    </auth>\n" +
                "</import>"


        val builder = Retrofit.Builder()
            .baseUrl("http://tsk-web-agro.kdvm.ru:8887/")
            .addConverterFactory(ScalarsConverterFactory.create())

        val retrofit = builder.build()
        val userClient = retrofit.create(UserClient::class.java)
        val call = userClient.auth(msg).enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Toast.makeText(this@LoginActivity, "Вы успешно вошли!", Toast.LENGTH_LONG).show()
                val m_data = response.body()!!.string()
                val m_intent = Intent(this@LoginActivity, AnalysisActivity::class.java)
                val filename = "preferences"
                val pref = getSharedPreferences(filename, Context.MODE_PRIVATE)
                val editor = pref.edit()
                editor.putString("login", mLogin)
                editor.putString("password", mPassword)
                editor.apply()
                val mFolder = File(applicationContext.filesDir,"XML")
                if(!mFolder.exists())
                    mFolder.mkdir()
                val mName = File(mFolder, "auth.xml")
                val write = FileWriter(mName)
                write.append(m_data)
                write.close()
                resultParse()
                if(result == "OK")
                    startActivity(m_intent)
                else
                    Toast.makeText(this@LoginActivity, result, Toast.LENGTH_LONG).show()
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@LoginActivity, t.message, Toast.LENGTH_LONG).show()
            }
        })
        return call.toString()
    }

    fun String.md5() : String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
    }

    fun resultParse() {
        val parserFactory = XmlPullParserFactory.newInstance()
        val parser = parserFactory.newPullParser()
        val inputStream = FileInputStream("${applicationContext.filesDir}/XML/auth.xml")
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, null)
        processParsing(parser)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun processParsing(parser: XmlPullParser) {
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            var eltName: String?
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    eltName = parser.name
                    if ("name" == eltName) {
                        stockName.add(parser.nextText())
                    } else if ("id" == eltName) {
                        stockId.add(parser.nextText())
                    } else if ("result" == eltName) {
                        result = parser.nextText()
                    }
                }
            }

            eventType = parser.next()
        }
        val mFolder = File(applicationContext.filesDir,"XML")
        val mId = File(mFolder, "id_stock.xml")
        val mName = File(mFolder, "stocks.xml")
        val writeId = FileWriter(mId)
        val write = FileWriter(mName)
        write.append(stockName.joinToString(","))
        write.close()
        writeId.append(stockId.joinToString(","))
        writeId.close()
    }
}
