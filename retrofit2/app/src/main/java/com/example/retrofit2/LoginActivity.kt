package com.example.retrofit2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_analysis.*
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.math.BigInteger
import java.security.MessageDigest

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
                m_intent.putExtra("login_activity_data", m_data)
                startActivity(m_intent)
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
}
