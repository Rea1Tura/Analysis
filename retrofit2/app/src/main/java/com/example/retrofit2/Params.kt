package com.example.retrofit2

class Params(var id: String? = null, var name: String? = null, var type: String? = null) {
    override fun toString(): String {
        return "$id $name $type"
    }
}