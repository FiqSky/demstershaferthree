package com.example.demstershaferthree

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GejalaAdapter(
    private val gejalaList: List<Gejala>,
    private val listener: OnGejalaSelectedListener
) : RecyclerView.Adapter<GejalaAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gejala, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val gejala = gejalaList[position]
        holder.bind(gejala)
    }

    override fun getItemCount(): Int {
        return gejalaList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewGejala: TextView = itemView.findViewById(R.id.textViewGejala)
        private val checkBoxGejala: CheckBox = itemView.findViewById(R.id.checkBoxGejala)

        fun bind(gejala: Gejala) {
            textViewGejala.text = gejala.gejala
            checkBoxGejala.isChecked = false

            checkBoxGejala.setOnCheckedChangeListener { buttonView, isChecked ->
                listener.onGejalaSelected(gejala.kodeGejala, isChecked)
            }
        }
    }

    interface OnGejalaSelectedListener {
        fun onGejalaSelected(kodeGejala: String, isChecked: Boolean)
    }
}
