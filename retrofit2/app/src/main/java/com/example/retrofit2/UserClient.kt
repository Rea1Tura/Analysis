package com.example.retrofit2

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import java.io.FileInputStream

interface UserClient {

    @POST("auto/import.php")
    fun auth(@Body str: String) : Call<ResponseBody>

    @POST("auto/import.php")
    fun analysisAdd(@Body str: String) : Call<ResponseBody>

    @POST("auto/import.php")
    fun analysisGet(@Body str: String) : Call<ResponseBody>

}




