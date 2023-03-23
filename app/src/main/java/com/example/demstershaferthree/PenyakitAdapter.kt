package com.example.demstershaferthree

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PenyakitAdapter(private val penyakitList: List<Pair<Penyakit, Double>>) :
    RecyclerView.Adapter<PenyakitAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_penyakit, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val penyakit = penyakitList[position].first
        val belief = penyakitList[position].second
        holder.bind(penyakit, belief)
    }

    override fun getItemCount(): Int {
        return penyakitList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewNamaPenyakit: TextView = itemView.findViewById(R.id.textViewNamaPenyakit)
        private val textViewBelief: TextView = itemView.findViewById(R.id.textViewBelief)

        fun bind(penyakit: Penyakit, belief: Double) {
            val beliefPercentage = (belief * 100).toInt()
            textViewNamaPenyakit.text = penyakit.namaPenyakit
            textViewBelief.text = "Kepercayaan: $beliefPercentage%"

            itemView.setOnClickListener {
                // Lakukan sesuatu jika item penyakit di-klik
            }
        }
    }
}
