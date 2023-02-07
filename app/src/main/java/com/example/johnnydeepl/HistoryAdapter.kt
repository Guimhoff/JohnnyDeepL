package com.example.johnnydeepl

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * @param context                   contexte lié à l'activité courante
 * @param history                   callback qui renvoie l'historique
 * @param removeElement             callback qui supprime un élément en fonction de son id
 * @param loadElement               callback qui charge un élément sur MainActivity
 * Implémentation de RecyclerView.Adapter (va permettre de relier la recycler_view à son contenu)
 */
class HistoryAdapter(
    private val context: Context,
    private val history: () -> Array<HistoryElement>,
    private val removeElement: (Int) -> Unit,
    private val loadElement: (HistoryElement) -> Unit,
) :
    RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    /**
     * @param itemView          vue associée à history_item
     * Implémentation de RecyclerView.ViewHolder
     * On associe aux holders créés les éléments de history_item que l'on souhaite modifier par la suite
     */
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textSource: TextView = itemView.findViewById(R.id.originalTextView)
        val textTranslated: TextView = itemView.findViewById(R.id.translatedTextView)
        val buttonDelete: FloatingActionButton = itemView.findViewById(R.id.deleteElementButton)
        val entireElement: ConstraintLayout = itemView.findViewById(R.id.entireItem)
    }

    /**
     * @param parent
     * @param viewType
     * @return viewHolder
     * Retourne un objet viewHolder qui contient la vue d'un item de la liste
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.history_item, parent, false)
        return ViewHolder(view)
    }

    /**
     * @return count
     * Retourne le nombre d'éléments qui seront affichés dans la liste
     */
    override fun getItemCount(): Int {
        return history().size
    }

    /**
     * @param holder
     * @param position
     * Configure les éléments d'UI d'un item en fonction de sa position
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Affichage du début des textes de la traduction
        holder.textSource.text = history()[position].textSource
        holder.textTranslated.text = history()[position].textTranslated
        // On lie aux callbacks correspondantes les deux actions que l'on souhaite détecter :
        // appui sur le bouton supprimer et appui sur l'item
        holder.buttonDelete.setOnClickListener {
            removeElement(history()[position].id)
        }
        holder.entireElement.setOnClickListener {
            loadElement(history()[position])
        }
    }
}


