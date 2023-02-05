package com.example.johnnydeepl

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(private val context: Context, private val history: List<HistoryElement>) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textSource: TextView = itemView.findViewById<TextView>(R.id.originalTextView)
        val textTranslated: TextView = itemView.findViewById<TextView>(R.id.translatedTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.history_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return history.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textSource.text = history[position].textSource
        holder.textTranslated.text = history[position].textTranslated
    }
}


