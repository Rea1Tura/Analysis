package ru.kdvm.labcontrol

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface UserClient {

    @POST("auto/import.php")
    fun auth(@Body str: String) : Call<ResponseBody>

    @POST("auto/import.php")
    fun analysisAdd(@Body str: String) : Call<ResponseBody>

    @POST("auto/import.php")
    fun analysisGet(@Body str: String) : Call<ResponseBody>

}




