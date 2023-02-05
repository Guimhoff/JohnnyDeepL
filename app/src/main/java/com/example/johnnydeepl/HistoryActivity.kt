package com.example.johnnydeepl

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager

class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val historyView = findViewById<RecyclerView>(R.id.history_recycler_view)

        val history = listOf<HistoryElement>(
            HistoryElement("Texte source", "Texte Traduit", "EN", "EN", "FR"),
            HistoryElement("Texte source 2", "Texte Traduit 2", "EN", "EN", "FR")
        )


        val historyAdapter = HistoryAdapter( this, history)
        historyView.adapter = historyAdapter
        historyView.layoutManager = LinearLayoutManager(this)
    }
}