package com.example.retrofit2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_params.*

class ParamsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_params)
        val paramsData = intent.getStringArrayListExtra("params_data")
        textView.text = paramsData.joinToString("\n")
    }
}
