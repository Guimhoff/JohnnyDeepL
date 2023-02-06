package com.example.johnnydeepl

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager

class HistoryActivity : AppCompatActivity() {
    lateinit var history: Array<HistoryElement>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val historyView = findViewById<RecyclerView>(R.id.history_recycler_view)

        history = arrayOf()

        loadHistory()




        val historyAdapter = HistoryAdapter( this, history)
        historyView.adapter = historyAdapter
        historyView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadHistory() {
        val sharedPreferences = getSharedPreferences("History", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        var list = listOf<String>()

        try {
            val set = sharedPreferences.getStringSet("History", setOf())
            list = set?.toList() as List<String>
        } catch (_: Error) {

        }

        for (n in list.indices) {
            try {
                history += HistoryElement(list[n])
            } catch (e: Error) {
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }
        }


    }

    fun onClickDelAll(view: View) {
        val sharedPreferences = getSharedPreferences("History", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putStringSet("History", HashSet())
        editor.commit()


    }

    fun onClickBack(view: View) {
        finish()
    }
}