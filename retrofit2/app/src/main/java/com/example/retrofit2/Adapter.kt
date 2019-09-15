package com.example.retrofit2

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView

class Adapter(private val values: ArrayList<Test>) :
    RecyclerView.Adapter<Adapter.ViewHolder>() {

    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        var textView: TextView? = null
        init {
            textView = itemView?.findViewById(R.id.text_list_item)
        }
    }

    override fun getItemCount() = values.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_view, parent, false)
        return ViewHolder(itemView)
    }



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView?.text = values[position].toString()
        holder.itemView.setOnClickListener {
            val m_data = ArrayList<String>()
            for(value in values[position].params) {
                m_data.add(value.toString())
            }
            val m_intent = Intent(holder.itemView.context, ParamsActivity::class.java)
            m_intent.putExtra("params_data", m_data)
            holder.itemView.context.startActivity(m_intent)
        }
    }
}