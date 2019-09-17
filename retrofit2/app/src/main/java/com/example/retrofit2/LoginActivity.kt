package com.example.retrofit2

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.text.InputType
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_analysis.*
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
import java.lang.Exception
import java.math.BigInteger
import java.security.MessageDigest

class LoginActivity : AppCompatActivity() {

    private val arrStr: Array<String> = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.LOCATION_HARDWARE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ActivityCompat.requestPermissions(this, arrStr, 0)
        button.setOnClickListener {
            val mLogin = txtLogin.text.toString().toLowerCase()
            val mPassword = txtPassword.text.toString().md5().toUpperCase()
            executeMessage(mLogin, mPassword)
        }
    }

    private fun executeMessage(mLogin: String, mPassword: String) : String {
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
                Toast.makeText(this@LoginActivity, "You successfully logged in!", Toast.LENGTH_LONG).show()
                val m_data = response.body()!!.string()
                val m_intent = Intent(this@LoginActivity, AnalysisActivity::class.java)
                val mFolder = File(Environment.getExternalStorageDirectory(), "XML")
                mFolder.mkdirs()
                val mName = File(mFolder, "auth.xml")
                val write = FileWriter(mName)
                write.append(m_data)
                write.close()
                val errorStr = resultParse()
                if(errorStr == "0")
                    startActivity(m_intent)
                else
                    Toast.makeText(this@LoginActivity, errorStr, Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Something is wrong", Toast.LENGTH_LONG).show()
            }
        })
        return call.toString()
    }

    fun String.md5() : String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
    }

    fun resultParse(): String {
        val parserFactory = XmlPullParserFactory.newInstance()
        val parser = parserFactory.newPullParser()
        val inputStream = FileInputStream("${Environment.getExternalStorageDirectory()}/XML/auth.xml")
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, null)
        return processParsing(parser)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun processParsing(parser: XmlPullParser): String {
        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            var eltName: String?
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    eltName = parser.name
                    if ("error" == eltName) {
                        return parser.nextText()
                    }
                }
            }

            eventType = parser.next()
        }
        return "0"
    }
}
