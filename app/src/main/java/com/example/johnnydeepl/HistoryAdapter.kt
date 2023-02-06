package com.example.johnnydeepl

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HistoryAdapter(
    private val context: Context,
    private val history: () -> Array<HistoryElement>,
    private val removeElement: (Int) -> Unit,
    private val loadElement: (HistoryElement) -> Unit,
) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textSource: TextView = itemView.findViewById(R.id.originalTextView)
        val textTranslated: TextView = itemView.findViewById(R.id.translatedTextView)
        val buttonDelete: FloatingActionButton = itemView.findViewById(R.id.deleteElementButton)
        val entireElement: ConstraintLayout = itemView.findViewById(R.id.entireItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.history_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return history().size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textSource.text = history()[position].textSource
        holder.textTranslated.text = history()[position].textTranslated
        holder.buttonDelete.setOnClickListener {
            removeElement(history()[position].id)
        }

        holder.entireElement.setOnClickListener {
            loadElement(history()[position])
        }
    }
}


