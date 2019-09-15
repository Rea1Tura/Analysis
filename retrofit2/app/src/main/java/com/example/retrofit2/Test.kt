package com.example.retrofit2

class Test(var id: String? = null, var name: String? = null, var params: ArrayList<Params> = ArrayList()) {
    override fun toString(): String {
        return "$name"
    }
}