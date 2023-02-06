package com.example.johnnydeepl

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import org.json.JSONObject
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.json.JSONException

class ParametersActivity : AppCompatActivity() {
    companion object {  // Équivalent à une variable static en Java
        const val textNoKey = "Rentrez une clé pour accéder à votre consommation..."
        const val keyPreferenceDeepLKey = "deepLKey"
    }

    private var deepLKey = ""                                       // Clé DeepL
    private lateinit var preferencesFile: SharedPreferences         // Fichier SharedPreferences dans lequel est stocké la clé
    private lateinit var deepLKeyText: EditText                     // Texte d'information dans lequel est affichée la consommation de l'API (ou l'absence de clé)

    /**
     * Fonction exécutée à la création de la vue
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parameters)

        // Initialisation réseau
        AndroidNetworking.initialize(this)

        // Initialisation interfaces et préférences
        confText()
        loadPreferences()
    }


    ////// INITIALISATION //////

    /**
     * Fonction qui va charger la clé DeepL dans la variable deepLKey
     * et l'afficher dans l'encart texte modifiable par l'utilisateur
     */
    private fun loadPreferences(){
        // Récupération de la clé
        preferencesFile = getSharedPreferences(keyPreferenceDeepLKey, MODE_PRIVATE)
        deepLKey = preferencesFile.getString(keyPreferenceDeepLKey, "").toString()

        // Affichage dans l'encart
        findViewById<TextView>(R.id.DeepLKeyText).text = deepLKey

        // Affichage de la consommation si clé présente
        if (deepLKey != "") {
            getDeepLUsage()
        } else {
            showNoKey()
        }
    }

    /**
     * Configure l'entrée texte utilisateur en reliant l'appui sur la touche
     * "entrée" au bouton "Valider"
     */
    private fun confText() {
        deepLKeyText = findViewById(R.id.DeepLKeyText)

        deepLKeyText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onClickConfirmKey(deepLKeyText)
                true
            } else {
                false
            }
        }
    }


    ////// GESTION CLÉ //////

    /**
     * Sauvegarde la clé DeepL
     */
    private fun saveKey() {
        val editor = preferencesFile.edit()

        editor.putString(keyPreferenceDeepLKey, deepLKey)
        editor.apply()
    }

    /**
     * Mise à jour de l'interface en cas d'absence de clé fonctionnelle
     * ou d'échec de connexion
     */
    private fun showNoKey() {
        findViewById<ProgressBar>(R.id.DeepLProgressBar).progress = 0
        findViewById<TextView>(R.id.DeepLUsageText).text = textNoKey
    }


    ////// GESTION USAGE API //////

    /**
     * Envoie une requête pour récupérer la consommation de l'API
     * Déclenche ensuite une maj de l'interface et sauvegarde la clé
     * (ce afin de ne sauvegarder que des clés qui fonctionnent)
     */
    private fun getDeepLUsage() {
        val that = this

        AndroidNetworking.get("https://api-free.deepl.com/v2/usage")
            .addHeaders("Authorization", "DeepL-Auth-Key $deepLKey")
            .build()
            .getAsJSONObject(object: JSONObjectRequestListener{
                override fun onResponse(response: JSONObject) {
                    try{
                        val characterCount = response.getInt("character_count")
                        val characterLimit = response.getInt("character_limit")

                        updateUsage(characterCount, characterLimit)
                        saveKey()
                    } catch (e: JSONException) {
                        // En cas d'erreur on laisse ou remet le message demandant d'entrer une clé et on prévient l'utilisateur
                        showNoKey()
                        Toast.makeText(that, "Une erreur JSON est survenue\n${e.message}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onError(anError: ANError?) {
                    // En cas d'erreur on laisse ou remet le message demandant d'entrer une clé et on prévient l'utilisateur
                    showNoKey()
                    Toast.makeText(that, "Une erreur de connexion est survenue\n${anError?.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    /**
     * Met à jour l'interface en fonction de la consommation reçue
     */
    private fun updateUsage(charCount: Int, charLimit: Int) {
        // Mise à jour de la barre de progression
        findViewById<ProgressBar>(R.id.DeepLProgressBar).progress = 100 * charCount / charLimit
        // Mise à jour de l'encart de texte
        val txt = "$charCount sur $charLimit caractères utilisés\n(${charLimit - charCount} restants)"
        findViewById<TextView>(R.id.DeepLUsageText).text = txt
    }


    ////// GESTION BOUTONS //////

    /**
     * @param view      nécessaire à une méthode onClick
     * Méthode appelée par l'appui sur le bouton retour
     * Ferme la view
     */
    fun onClickBack(view: View) {
        finish()
    }

    /**
     * @param view      nécessaire à une méthode onClick
     * Méthode appelée par le bouton "VALIDER" et l'appuie sur la touche "entrée du clavier"
     * Va tenter une requête pour récupérer l'usage de l'API et mettre à jour l'interface
     */
    fun onClickConfirmKey(view: View) {
        // On cache le clavier et on sort du focus
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        deepLKeyText.clearFocus()

        // On enlève les potentiels espaces ajoutés par erreur avant ou après la clé
        deepLKey = deepLKeyText.text.toString().trim()
        deepLKeyText.setText(deepLKey)

        if (deepLKey == "") {
            // Si aucune clé n'est rentrée on sauvegarde, afin de permettre à l'utilisateur de retirer sa clé
            saveKey()
            showNoKey()
        } else {
            getDeepLUsage()
        }
    }


}