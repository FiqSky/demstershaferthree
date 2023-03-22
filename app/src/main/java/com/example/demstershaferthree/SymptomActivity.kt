package com.example.demstershaferthree

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class SymptomActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var databaseRef: DatabaseReference
    private lateinit var gejalaList: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symptom)

        listView = findViewById(R.id.list_gejala)
        databaseRef = FirebaseDatabase.getInstance().reference.child("GEJALA")
        gejalaList = mutableListOf()

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, gejalaList)
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        listView.adapter = adapter

        fetchGejalaData()
    }

    private fun fetchGejalaData() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (gejalaSnapshot in snapshot.children) {
                    val gejala = gejalaSnapshot.child("gejala").getValue(String::class.java)
                    gejala?.let { gejalaList.add(it) }
                }
                (listView.adapter as ArrayAdapter<*>).notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    fun onButtonClick(view: View) {
        val selectedGejala = mutableListOf<String>()
        val checkedItems = listView.checkedItemPositions
        for (i in 0 until checkedItems.size()) {
            val position = checkedItems.keyAt(i)
            if (checkedItems.valueAt(i)) {
                selectedGejala.add(gejalaList[position])
            }
        }
        // Do something with selectedGejala
    }
}