package com.example.johnnydeepl

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryActivity : AppCompatActivity() {
    private lateinit var history: Array<HistoryElement>         // Array représentant l'historique

    private lateinit var historyView: RecyclerView              // RecyclerView : élément d'UI affichant la liste des éléments d'historique

    /**
     * Fonction exécutée à la création de la vue
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // Chargement de l'historique
        loadHistory()

        // Initialisation de l'interface
        historyView = findViewById(R.id.history_recycler_view)

        val historyAdapter = HistoryAdapter( this, { getHistory() }, { id: Int -> removeElement(id) }, {ele -> loadElement(ele)})
        historyView.adapter = historyAdapter
        historyView.layoutManager = LinearLayoutManager(this)
    }


    ////// GESTION DE L'HISTORIQUE //////

    /**
     * @return history
     * Getter de history utilisée comme callback de HistoryAdapter
     */
    private fun getHistory(): Array<HistoryElement>  {
        return history
    }

    /**
     * Charge l'historique enregistré et le stocke history
     */
    private fun loadHistory() {
        val sharedPreferences = getSharedPreferences("History", Context.MODE_PRIVATE)

        // Variable utilisée pour le chargement
        var list = listOf<String>()

        // On vide la variable history
        history = arrayOf()

        // On essai de récupérer la liste sauvegardée
        try {
            val set = sharedPreferences.getStringSet("History", setOf())
            list = set?.toList() as List<String>
        } catch (_: Error) {

        }

        // La liste chargée est transférée dans history
        for (n in list.indices) {
            try {
                history += HistoryElement(list[n])
            } catch (e: Error) {
                Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
            }
        }

        // history est trié par ordre d'ancienneté
        history.sortBy { ele -> -ele.id }
    }

    /**
     * @param ele           élément de l'historique à charger dans la fenêtre principale
     * Charge l'élément spécifié dans une nouvelle vue de MainActivity
     */
    private fun loadElement(ele: HistoryElement){
        val i = Intent(this, MainActivity::class.java)
        i.putExtra("HistoryElement", ele)
        startActivity(i)
    }

    /**
     * @param id            id de l'élément à supprimer
     * Supprime l'élément spécifié de l'historique et recharge l'affichage
     */
    private fun removeElement(id: Int) {
        // Filtre les éléments qui ne correspondent pas à l'élément à supprimer
        history = history.filter {it.id != id}.toTypedArray()

        // Enregistre history dans une liste
        val list = mutableListOf<String>()
        for (ele in history.iterator()) {
            list += ele.save()
        }

        // Transforme la liste en HashSet
        val set = HashSet(list)

        // Enregistre le HashSet
        val sharedPreferences = getSharedPreferences("History", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putStringSet("History", set)
        editor.apply()

        // Recharge l'historique (pas réellement nécessaire mais permet de s'assurer
        // que l'historique enregistré correspond toujours à celui affiché)
        loadHistory()
        // Rafraichit l'interface
        historyView.adapter?.notifyDataSetChanged()
    }


    ////// BOUTONS //////

    /**
     * @param view      nécessaire à une méthode onClick
     * Méthode appelée par l'appui sur le bouton "Tout supprimer"
     * Supprime tous les éléments de l'historique
     */
    fun onClickDelAll(view: View) {
        // Enregistre un HashSet vide
        val sharedPreferences = getSharedPreferences("History", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        editor.putStringSet("History", HashSet())
        editor.apply()


        // Recharge l'historique (pas réellement nécessaire (il suffirait de vider la liste)
        // mais permet de s'assurer que l'historique enregistré correspond toujours à celui affiché)
        loadHistory()
        // Rafraichit l'interface
        historyView.adapter?.notifyDataSetChanged()
    }

    /**
     * @param view      nécessaire à une méthode onClick
     * Méthode appelée par l'appui sur le bouton retour
     * Ferme la view
     */
    fun onClickBack(view: View) {
        finish()
    }
}