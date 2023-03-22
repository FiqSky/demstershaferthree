package com.example.demstershaferthree

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PenyakitAdapter: RecyclerView.Adapter<PenyakitAdapter.ViewHolder>() {

    private val databaseRef = FirebaseDatabase.getInstance().reference
    private val penyakitList: MutableList<Penyakit> = mutableListOf()

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val kodePenyakit: TextView = itemView.findViewById(R.id.kode_penyakit)
        private val namaPenyakit: TextView = itemView.findViewById(R.id.nama_penyakit)
        private val daftarGejala: TextView = itemView.findViewById(R.id.daftar_gejala)

        fun bind(penyakit: Penyakit) {
            kodePenyakit.text = penyakit.kode_penyakit
            namaPenyakit.text = penyakit.nama_penyakit
            daftarGejala.text = penyakit.daftar_gejala.joinToString(", ")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_penyakit, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val penyakit = penyakitList[position]
        holder.bind(penyakit)
    }

    override fun getItemCount(): Int {
        return penyakitList.size
    }

    fun fetchPenyakitData() {
        databaseRef.child("PENYAKIT").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (penyakitSnapshot in snapshot.children) {
                    val kode_penyakit = penyakitSnapshot.child("kode_penyakit").getValue(String::class.java)
                    val nama_penyakit = penyakitSnapshot.child("nama_penyakit").getValue(String::class.java)
                    val daftar_gejala = mutableListOf<String>()
                    for (gejalaSnapshot in penyakitSnapshot.child("daftar_gejala").children) {
                        val gejala = gejalaSnapshot.getValue(String::class.java)
                        gejala?.let { daftar_gejala.add(it) }
                    }
                    val penyakit = Penyakit(kode_penyakit, nama_penyakit, daftar_gejala)
                    penyakitList.add(penyakit)
                }
                notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }
}