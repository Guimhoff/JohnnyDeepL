package com.example.johnnydeepl

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryActivity : AppCompatActivity() {
    lateinit var history: Array<HistoryElement>

    lateinit var historyView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        historyView = findViewById(R.id.history_recycler_view)

        //loadHistory()

        val historyAdapter = HistoryAdapter( this, { loadHistory() }) { id: Int -> removeElement(id) }
        historyView.adapter = historyAdapter
        historyView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadHistory(): Array<HistoryElement> {
        val sharedPreferences = getSharedPreferences("History", Context.MODE_PRIVATE)

        var list = listOf<String>()

        history = arrayOf()

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

        history.sortBy { ele -> -ele.id }

        println(history.size)

        return history
    }

    private fun removeElement(id: Int) {
        history = history.filter {it.id != id}.toTypedArray()

        var list = listOf<String>()

        for (ele in history.iterator()) {
            list += ele.save()
        }

        val set = HashSet(list)

        val sharedPreferences = getSharedPreferences("History", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putStringSet("History", set)
        editor.apply()

        loadHistory()
        historyView.adapter?.notifyDataSetChanged()
    }

    fun onClickDelAll(view: View) {
        val sharedPreferences = getSharedPreferences("History", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putStringSet("History", HashSet())
        editor.commit()

        loadHistory()
        historyView.adapter?.notifyDataSetChanged()
    }

    fun onClickBack(view: View) {
        finish()
    }
}