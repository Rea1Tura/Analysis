package com.example.retrofit2

import java.io.Serializable

class Params(var id_param: String? = null, var name: String? = null, var value: String? = ""): Serializable {
    override fun toString(): String {
        return "$id_param,$name,$value"
    }
}