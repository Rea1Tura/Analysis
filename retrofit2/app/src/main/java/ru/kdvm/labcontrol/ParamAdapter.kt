package ru.kdvm.labcontrol

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileWriter
import java.lang.StringBuilder

class ParamAdapter(val id_text: ArrayList<String>, val edit: ArrayList<String>) : RecyclerView.Adapter<ParamAdapter.ViewHolder>() {

    val VIEW_STOCK = 0
    val VIEW_OTHER = 1

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
                        array?.set(adapterPosition - 1, editText?.text.toString())
                        val id = File("${itemView?.context?.filesDir}/XML", "id_params.xml").readText().split("\n")
                        val strn = StringBuilder()
                        for((index, value) in array.withIndex()) {
                            strn.append(
                                "                    <param>\n" +
                                        "                        <id_param>" + id[index] + "</id_param>\n" +
                                        "                        <value>" + value + "</value>\n" +
                                        "                    </param>\n"
                            )
                        val mFolder = File(itemView?.context?.filesDir,"XML")
                        val mName = File(mFolder, "params.xml")
                        val write = FileWriter(mName)
                        write.append(strn.toString())
                        write.close()}
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
            var stockList = ArrayList<String>()
            if(stocks != "")
                 stockList = stocks.split(",") as ArrayList<String>
            if(arr.split(" ")[1] == "" || arr.split(" ")[1] == "0") {
                stockList.add(0, "Выберите склад")
            }
            val spinnerArrayAdapter = ArrayAdapter<String>(itemView!!.context,
                R.layout.view_text, stockList)
            spinnerArrayAdapter.setDropDownViewResource(R.layout.view_text)
            spin?.adapter = spinnerArrayAdapter
            spin!!.setSelection(ids.split(",").indexOf(arr.split(" ")[1]))
            spin!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    val mStock = File(mFolder, "stock.xml")
                    val write = FileWriter(mStock)
                    if(arr.split(" ")[1] == "" || arr.split(" ")[1] == "0") {
                        if(p2 == 0) {
                            write.append("")
                            write.close()
                        }
                        else {
                            write.append(ids.split(",")[p2 - 1])
                            write.close()
                        }
                    } else {
                        write.append(ids.split(",")[p2])
                        write.close()
                    }

                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }
        }
        var spinn: Spinner? = null
        init {
            spinn = itemView?.findViewById(R.id.spinner2) as Spinner
            val mFold = File(itemView.context.filesDir, "XML")
            val mState = File(mFold, "stock_state.xml")
            val arr = mState.readText()
            val arrayList = arrayListOf("Открыт", "Взят", "Обработан")
            val mFolder = File(itemView.context.filesDir, "XML")
            val spinnerArrayAdapter = ArrayAdapter<String>(itemView!!.context,
                R.layout.view_text, arrayList)
            spinnerArrayAdapter.setDropDownViewResource(R.layout.view_text)
            spinn?.adapter = spinnerArrayAdapter
            spinn!!.setSelection(arr.split(" ")[0].toInt())
            isEditable = if(arr.split(" ")[0].toInt() == 2)
                0
            else
                1
            spinn!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
        return VIEW_OTHER

    }

    override fun getItemCount() = edit.size + 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.param_item_view, parent, false)
        if(viewType == VIEW_STOCK) {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.list_sklad, parent, false)
            return ViewHolder1(itemView)
        }
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(position == 0 && array != edit) {
            array.clear()
        }
        if(position > 0) {
            val args = id_text[position - 1].split(",")
            if(array.size < id_text.size) {
                array.add(edit[position - 1])
            }
            holder.textView?.text = args[1]
            holder.editText?.setText(array[position - 1])
            holder.editText?.isEnabled = isEditable != 0
        }
    }

    companion object {
        var array = ArrayList<String>()
        var isEditable = 1
    }
}