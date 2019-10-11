package com.example.retrofit2

class Test(var id: String? = null, var num_doc: String? = null, var auto: String? = null, var name_sku: String? = null,
           var params: ArrayList<Params> = ArrayList(), var state: String? = null, var stock: String? = null) {
    override fun toString(): String {
        return "$num_doc $auto $name_sku"
    }
}