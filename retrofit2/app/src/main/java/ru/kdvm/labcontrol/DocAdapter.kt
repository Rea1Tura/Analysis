package ru.kdvm.labcontrol

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileWriter

class DocAdapter(private val values: ArrayList<Test>) :
    RecyclerView.Adapter<DocAdapter.ViewHolder>() {

    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        var textView: TextView? = null
        init {
            textView = itemView?.findViewById(R.id.text_list_item)
        }
        var textV: TextView? = null
        init {
            textV = itemView?.findViewById(R.id.textView2)
        }
    }

    override fun getItemCount() = values.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_view, parent, false)
        return ViewHolder(itemView)
    }



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView?.text = values[position].toString()
        when(values[position].state) {
            "0" -> holder.textV?.text = "Открыт"
            "1" -> holder.textV?.text = "Взят"
            "2" -> holder.textV?.text = "Обработан"
        }
        holder.itemView.setOnClickListener {
            val m_data = ArrayList<String>()
            m_data.add(values[position].id!!)
            for(value in values[position].params) {
                m_data.add(value.toString())
            }
            val mFold = File(holder.itemView.context.filesDir, "XML")
            val mName = File(mFold, "stock_state.xml")
            val writer = FileWriter(mName)
            writer.append("${values[position].state!!} ${values[position].stock!!}")
            writer.close()
            val m_intent = Intent(holder.itemView.context, ParamsActivity::class.java)
            m_intent.putExtra("params_data", m_data)
            holder.itemView.context.startActivity(m_intent)
        }
    }
}