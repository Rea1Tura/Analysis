package com.example.retrofit2

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_params.*
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File
import java.io.FileWriter


class ParamsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_params)
        val preferences: SharedPreferences? = getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val paramsData = intent.getStringArrayListExtra("params_data")
        val editor = preferences?.edit()
        editor?.putString("doc_id", paramsData[0])
        editor?.putString("data", ObjectSerializer.serialize(paramsData))
        editor?.apply()
        paramsData.removeAt(0)
        val id_text: ArrayList<String> = ArrayList()
        val edit: ArrayList<String> = ArrayList()
        for(param in paramsData) {
            id_text.add(param.split(",").subList(0, 2).joinToString(","))
            edit.add(param.split(",").subList(2, 3).joinToString(","))
        }
        val recyclerView: RecyclerView = paramsRecycler
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ParamAdapter(id_text, edit)
        val itemDecor = DividerItemDecoration(recyclerView.context, 1)
        recyclerView.addItemDecoration(itemDecor)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.params_menu, menu)
        val stock = File("${applicationContext.filesDir}/XML", "stock_state.xml").readText()
        if(stock.split(" ").size == 2 && stock.split(" ")[0] == "2") {
            val item = menu?.findItem(R.id.action_save)
            item?.setVisible(false)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item?.itemId) {
            R.id.action_save -> {
                executeMessage()
                finish()
                return true
            }
        }
        return true
    }

    private fun executeMessage() : String {
        val preferences: SharedPreferences? = getSharedPreferences("preferences", Context.MODE_PRIVATE)
        val file = File("${applicationContext.filesDir}/XML/params.xml")
        val stock = File("${applicationContext.filesDir}/XML/stock.xml")
        val status = File("${applicationContext.filesDir}/XML/status.xml")
        val msg =
            "<?xml version=\"1.0\"?>\n" +
                    "<import>\n" +
                    "    <auth>\n" +
                    "        <login>" + preferences?.getString("login", "") + "</login>\n" +
                    "        <password>" + preferences?.getString("password", "") + "</password>\n" +
                    "        <apk>" + BuildConfig.VERSION_CODE.toString() + "</apk>\n" +
                    "    </auth>\n" +
                    "    <save>\n" +
                    "        <docs>\n" +
                    "            <doc>\n" +
                    "                <id>" + preferences?.getString("doc_id", "") + "</id>\n" +
                    "                <stock>" + stock.readText() + "</stock>\n" +
                    "                <state>" + status.readText() + "</state>\n" +
                    "                <params>\n" + file.readText() +
                    "                </params>\n" +
                    "            </doc>\n" +
                    "        </docs>\n" +
                    "    </save>\n" +
                    "</import>"
        val mFolder = File(applicationContext.filesDir,"XML")
        if(!mFolder.exists())
            mFolder.mkdir()
        val mName = File(mFolder, "save.xml")
        val write = FileWriter(mName)
        write.append(msg)
        write.close()

        val builder = Retrofit.Builder()
            .baseUrl("http://tsk-web-agro.kdvm.ru:8887/")
            .addConverterFactory(ScalarsConverterFactory.create())

        val retrofit = builder.build()
        val userClient = retrofit.create(UserClient::class.java)
        val call = userClient.auth(msg).enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                val m_data = response.body()!!.string()
                val mFolder = File(applicationContext.filesDir,"XML")
                if(!mFolder.exists())
                    mFolder.mkdir()
                val mName = File(mFolder, "saved.xml")
                val write = FileWriter(mName)
                write.append(m_data)
                write.close()
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@ParamsActivity, t.message, Toast.LENGTH_LONG).show()
            }
        })
        return call.toString()
    }
}
