package com.example.retrofit2

import android.content.Context
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.param_item_view.view.*
import java.io.File
import java.io.FileWriter
import java.lang.StringBuilder
import kotlin.coroutines.coroutineContext

class ParamAdapter(val id_text: ArrayList<String>, val edit: ArrayList<String>) : RecyclerView.Adapter<ParamAdapter.ViewHolder>() {

    val VIEW_STOCK = 0
    val VIEW_STATUS = 1
    val VIEW_OTHER = 2

    open class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        var textView: TextView? = null
        init {
            textView = itemView?.findViewById(R.id.param_list_item)
        }
        var editText: EditText? = null
        init {
            editText = itemView?.findViewById(R.id.editText)
            editText?.addTextChangedListener(object : TextWatcher{
                override fun afterTextChanged(p0: Editable?) {

                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                        array?.set(adapterPosition - 2, editText?.text.toString())
                        val strn = StringBuilder()
                        for((index, value) in array.withIndex()) {
                            strn.append(
                                "                    <param>\n" +
                                        "                        <id_param>" + id[index] + "</id_param>\n" +
                                        "                        <value>" + value + "</value>\n" +
                                        "                    </param>\n"
                            )
                        }
                        val mFolder = File(itemView?.context?.filesDir,"XML")
                        val mName = File(mFolder, "params.xml")
                        val write = FileWriter(mName)
                        write.append(strn.toString())
                        write.close()
                }
            })
        }
    }

    class ViewHolder1(itemView: View?) : ViewHolder(itemView!!) {
        var spin: Spinner? = null
        init {
            spin = itemView?.findViewById(R.id.spinner) as Spinner
            val mFold = File(itemView.context.filesDir, "XML")
            val mState = File(mFold, "stock_state.xml")
            val arr = mState.readText()
            val mFolder = File(itemView.context.filesDir,"XML")
            val mId = File(mFolder, "id_stock.xml")
            val mName = File(mFolder, "stocks.xml")
            val stocks = mName.readText()
            val ids = mId.readText()
            val stockList = stocks.split(",") as ArrayList<String>
            if(arr.split(" ")[1] == "") {
                stockList.add(0, "Выберите склад")
            }
            val spinnerArrayAdapter = ArrayAdapter<String>(itemView!!.context, R.layout.view_text, stockList)
            spinnerArrayAdapter.setDropDownViewResource(R.layout.view_text)
            spin?.adapter = spinnerArrayAdapter
            spin!!.setSelection(ids.split(",").indexOf(arr.split(" ")[1]))
            spin!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    val mStock = File(mFolder, "stock.xml")
                    val write = FileWriter(mStock)
                    write.append(ids.split(",")[p2])
                    write.close()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }
        }
    }

    class ViewHolder2(itemView: View?) : ViewHolder(itemView!!) {
        var spin: Spinner? = null
        init {
            spin = itemView?.findViewById(R.id.spinner) as Spinner
            val mFold = File(itemView.context.filesDir, "XML")
            val mState = File(mFold, "stock_state.xml")
            val arr = mState.readText()
            val arrayList = arrayListOf("Открыт", "Взят", "Обработан")
            val mFolder = File(itemView.context.filesDir, "XML")
            val spinnerArrayAdapter = ArrayAdapter<String>(itemView!!.context, R.layout.view_text, arrayList)
            spinnerArrayAdapter.setDropDownViewResource(R.layout.view_text)
            spin?.adapter = spinnerArrayAdapter
            spin!!.setSelection(arr.split(" ")[0].toInt())
            isEditable = if(arr.split(" ")[0].toInt() == 2)
                0
            else
                1
            spin!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    val mStock = File(mFolder, "status.xml")
                    val write = FileWriter(mStock)
                    write.append(p2.toString())
                    write.close()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if(position == 0)
            return VIEW_STOCK
        if(position == 1)
            return VIEW_STATUS
        return VIEW_OTHER

    }

    override fun getItemCount() = edit.size + 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.param_item_view, parent, false)
        if(viewType == VIEW_STOCK) {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_sklad, parent, false)
            return ViewHolder1(itemView)
        }
        if(viewType == VIEW_STATUS) {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_sklad, parent, false)
            return ViewHolder2(itemView)
        }
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(position == 0 && !(array.equals(edit))) {
            array.clear()
            id.clear()
        }
        if(position > 1) {
            val args = id_text[position - 2].split(",")
            if(array.size < id_text.size) {
                array.add(edit[position - 2])
            }
            if(id.size < id_text.size) {
                id.add(args[0])
            }
            else if(id_text.size == id.size && !(id[position - 2].equals(id_text[position - 2][0]))) {
                id[position - 2] = id_text[position - 2][0].toString()
            }
            holder.textView?.text = args[1]
            holder.editText?.setText(array[position - 2])
            holder.editText?.isEnabled = isEditable != 0
        }
    }

    companion object {
        var array = ArrayList<String>()
        var id = ArrayList<String>()
        var isEditable = 1
    }
}