package com.example.johnnydeepl

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    companion object {  // Équivalent à une variable static en Java
        // Permet de ne charger les langues qu'une fois
        var languages = hashMapOf<String, String>()                 // Dictionnaire qui stocke les langues et leur "code" DeepL
        var languagesArraySource = arrayOf<String>()                // Array qui stocke les langues sources (ie langues + Détection de langue)
        var languagesArrayDest = arrayOf<String>()                  // Array qui stocke les langues destination

        const val keyPreferenceDeepLKey = "deepLKey"
    }

    private var deepLKey = ""                               // Clé DeepL

    private var sourceLangue = ""                           // Langue du texte à traduire (dite langue source) ("" correspond à laisser DeepL déterminer la langue)
    private var destLangue = "null"                         // Langue dans laquelle traduire le texte (dite langue destination)

    private lateinit var clipboard: ClipboardManager        // Permet d'interragir avec le presse papier d'Android

    private lateinit var spinnerSource: Spinner             // Spinner permettant de choisir la langue du texte à traduire
    private lateinit var spinnerDest: Spinner               // Spinner permettant de choisir la langue de traduction

    private lateinit var sourceTextUI: EditText             // Zone de texte dans laquelle l'utilisateur entre le texte à traduire
    private lateinit var destTextUI: TextView               // Zone de texte dans laquelle est imprimée la traduction
    
    private lateinit var historyPreferences: SharedPreferences      // SharedPreferences dans laquelle est stocké l'historique
    private var fromHistory = false                                 // Indique si la vue doit afficher une traduction de l'historique

    /**
     * Fonction exécutée à la création de la vue
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialisation réseau
        AndroidNetworking.initialize(this)
        // Récupération presse papier
        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        // Initialisation interface et préférences
        confTextZone()
        loadPreferences()
        loadLanguages()
        getHistoryElement()
    }

    /**
     * Fonction exécutée quand la vue est réouverte
     */
    override fun onResume() {
        super.onResume()
        // Initialise les préférences
        loadPreferences()
        // Rafraichit les langues si une clé est rentrée (vérification pour ne pas renvoyer vers ParametersActivity en boucle)
        if(deepLKey != "") loadLanguages()
    }


    ////// INITIALISATION //////

    /**
     * Récupère la clé DeepL et le SharedPreferences dans lequel est stocké l'historique
     */
    private fun loadPreferences() {
        deepLKey = getSharedPreferences(keyPreferenceDeepLKey, MODE_PRIVATE).getString(keyPreferenceDeepLKey, "")!!
        historyPreferences = getSharedPreferences("History", Context.MODE_PRIVATE)
    }

    /**
     * Configure la zone dans laquelle l'utilisateur entre le texte à traduire
     * ainsi que celle dans laquelle est affichée la traduction
     */
    private fun confTextZone() {
        sourceTextUI = findViewById(R.id.sourceText)
        destTextUI = findViewById(R.id.translationTextView)

        // Associe un TextWatcher à la zone de texte source permettant l'appel de callbacks quand le texte est modifié
        sourceTextUI.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Appelé après la modification du texte
                // La vue de la traduction est vidée de la traduction précédente quand le texte source est modifié
                destTextUI.text = ""
                findViewById<TextView>(R.id.detectedLanguageText).text = ""
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Appelé avant que le texte ne soit modifié (nécessaire pour implémenter TextWatcher)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Appelé lorsque le texte est en train d'être modifié (nécessaire pour implémenter TextWatcher)
            }
        })

        // Associe une callback quand le focus du texte source change
        sourceTextUI.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                // On cache le clavier quand le focus est perdu
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(sourceTextUI.windowToken, 0)
            }
        }
    }

    /**
     * Chargement des langues proposées par l'API
     */
    private fun loadLanguages() {
        // Vérifie la présence d'une clé, puis que la liste des langues n'a pas déjà été chargée
        if (testKeyNull() || languages.size > 0) {
            confSpinners()      // Sinon lance une simple configuration des spinners
            return              // et met fin à la fonction
        }

        val that = this

        // Requête
        AndroidNetworking.get("https://api-free.deepl.com/v2/languages")
            .addHeaders("Authorization", "DeepL-Auth-Key $deepLKey")
            .build()
            .getAsJSONArray(object: JSONArrayRequestListener {
                override fun onResponse(response: JSONArray) {
                    try{
                        // Constitution du dictionnaire languages
                        for (i in 0 until response.length()){
                            val obj = response.getJSONObject(i)
                            languages[obj.getString("name")] = obj.getString("language")
                        }

                        // Constitution des différents tableaux de langues, basés sur les clés du dictionnaire, classées par ordre alphabétique
                        var languageArray = languages.keys.toList()
                        languageArray = languageArray.sorted()

                        languagesArraySource = arrayOf("Détecter la langue") + languageArray        // On ajoute "Détecter la langue" aux langues source
                        languagesArrayDest = languageArray.toTypedArray()

                        // On lance la configuration des spinners
                        confSpinners()
                    } catch (e: JSONException) {
                        // En cas d'erreur on prévient l'utilisateur (inutile de configurer les spinners, il n'y pas de liste de langues constituée)
                        Toast.makeText(that, "Une erreur est survenue lors de la récupération des langages\n${e.message}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onError(anError: ANError?) {
                    // En cas d'erreur on prévient l'utilisateur (inutile de configurer les spinners, il n'y pas de liste de langues constituée)
                    Toast.makeText(that, "Une erreur de connexion est survenue lors de la récupération des langages\n${anError.toString()}", Toast.LENGTH_LONG).show()
                }
            })

    }

    /**
     * Configuration des Spinners
     */
    private fun confSpinners() {
        // Récupération des spinners
        spinnerSource = findViewById(R.id.sourceLanguageSpinner)
        spinnerDest = findViewById(R.id.destinationLanguageSpinner)

        // Si les spinners ont déjà été configurés on ne recommence pas
        if(spinnerSource.adapter != null) {
            return
        }

        // Ajout d'adapters aux spinners
        val adapterSource = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, languagesArraySource)
        spinnerSource.adapter = adapterSource

        val adapterDest = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, languagesArrayDest)
        spinnerDest.adapter = adapterDest

        // Récupération et affichage des dernières langues utilisées
        // (sauf si on charge une traduction de l'historique, auquel cas on affiche ses langues (voir getHistoryElement()))
        if(!fromHistory) {
            val lastSource = historyPreferences.getString("lastSource", "")!!
            val lastDest = historyPreferences.getString("lastDest", "")!!
            setSpinners(lastSource, lastDest)
        }

        // Association de callbacks à la sélection d'options sur chacun des spinners

        spinnerSource.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Récupération de la langue
                val lastSource = parent.getItemAtPosition(position).toString()
                // Transformation en code langue DeepL
                sourceLangue = languages[lastSource].toString()
                // Traitement si absence de code langue (cas de "Détecter la langue")
                // Le remplacer par une chaîne vide simplifie les requêtes (voir onClickTranslate())
                if (sourceLangue == "null") sourceLangue = ""

                // Enregistrement de la langue pour qu'elle soit reproposée à l'ouverture
                val editor = historyPreferences.edit()
                editor.putString("lastSource", lastSource)
                editor.apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // aucun élément n'a été sélectionné (nécessaire à l'implémentation)
            }
        }

        spinnerDest.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Récupération de la langue
                val lastDest = parent.getItemAtPosition(position).toString()
                // Transformation en code langue DeepL
                destLangue = languages[lastDest].toString()

                // Enregistrement de la langue pour qu'elle soit reproposée à l'ouverture
                val editor = historyPreferences.edit()
                editor.putString("lastDest", lastDest)
                editor.apply()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // aucun élément n'a été sélectionné (nécessaire à l'implémentation)
            }
        }
    }


    ////// DIVERS //////

    /**
     * Vérifie la présence d'un élément d'historique dans l'intent
     * et le charge l'affiche le cas échéant
     */
    private fun getHistoryElement() {
        val i = intent

        if (i.getStringExtra("HistoryElement") != null) {
            // Dépréciée mais la fonction qui la remplace n'est disponible qu'à partir d'Android 33
            val ele = HistoryElement(i.getStringExtra("HistoryElement")!!)

            // Affichage de la traduction enregistrée
            displayTrad(ele.textSource, ele.textTranslated, ele.languageSource, ele.languageDetect, ele.languageDest)
            fromHistory = true
            // Changement de l'icone de l'historique
            findViewById<FloatingActionButton>(R.id.historyButton).setImageDrawable(ContextCompat.getDrawable(this, R.drawable.baseline_arrow_back_24))
        }
    }

    /**
     * Renvoie vers l'écran de configuration et envoie un toast à l'utilisateur en cas d'absence de clé DeepL
     */
    private fun testKeyNull(): Boolean {
        if (deepLKey == "") {
            Toast.makeText(this, "Veuillez entrer une clé DeepL", Toast.LENGTH_SHORT).show()
            val i = Intent(this, ParametersActivity::class.java)
            startActivity(i)
            return true
        }
        return false
    }


    ////// BOUTONS DE NAVIGATION //////

    /**
     * @param view      nécessaire à une méthode onClick
     * Méthode appelée par l'appui sur le bouton "Clé"
     * Ouvre l'écran de configuration
     */
    fun onClickParameters(view: View) {
        val i = Intent(this, ParametersActivity::class.java)
        startActivity(i)
    }

    /**
     * @param view      nécessaire à une méthode onClick
     * Méthode appelée par l'appui sur le bouton "Historique"
     * Ouvre l'historique
     */
    fun onClickHistory(view: View) {
        if(fromHistory) {
            // Si on vient déjà de l'historique, on ferme simplement la vue afin d'y retourner
            // et de ne pas empiler les vues indéfiniment
            finish()
        } else {
            val i = Intent(this, HistoryActivity::class.java)
            startActivity(i)
        }
    }


    ////// TRADUCTION //////

    /**
     * @param view      nécessaire à une méthode onClick
     * Méthode appelé par l'appui sur le bouton "Traduire"
     * Lance une requête de traduction ainsi que l'affichage et la sauvegarde de la réponse
     */
    fun onClickTranslate(view: View) {
        // On sort du focus
        sourceTextUI.clearFocus()

        val sourceText = sourceTextUI.text.toString()

        // Arrête la méthode en cas d'absence de clé ou de texte à traduire
        if (testKeyNull() || sourceText == "") {
            return
        }

        val that = this

        // Requête
        AndroidNetworking.get("https://api-free.deepl.com/v2/translate")
            .addHeaders("Authorization", "DeepL-Auth-Key $deepLKey")
            .addQueryParameter("text", sourceText)
            .addQueryParameter("source_lang", sourceLangue)     // Une sourceLanque vide ("") équivaut à l'absence de paramètre et donc à la détection de la langue
            .addQueryParameter("target_lang", destLangue)
            .build()
            .getAsJSONObject(object: JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.d("JSON", response.toString())
                    try{
                        // Récupération de la traduction
                        val trad = response.getJSONArray("translations").getJSONObject(0)
                        val destText = trad.getString("text")

                        // Récupération de la langue détectée
                        val detectLanguage = trad.getString("detected_source_language")

                        // Affichage de la traduction et de la langue détectée le cas échéant
                            // (le reste n'étant pas modifié on le laisse nul)
                            // (sauf la langue source, voir displayTrad())
                        displayTrad(null, destText, sourceLangue, detectLanguage, null)
                        // Sauvegarde de tous les paramètres de la traduction
                        appendHist(sourceText, destText, sourceLangue, detectLanguage, destLangue)
                    } catch (e: JSONException) {
                        // En cas d'erreur on prévient l'utilisateur
                        Toast.makeText(that, "Une erreur JSON est survenue\n${e.message}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onError(anError: ANError?) {
                    // En cas d'erreur on prévient l'utilisateur
                    Toast.makeText(that, "Une erreur de connexion est survenue\n${anError.toString()}", Toast.LENGTH_LONG).show()
                }
            })

    }

    /**
     * @param sourceText                texte original
     * @param destText                  texte traduit
     * @param sourceLangue              langue originale renseignée
     * @param detectLanguage            langue originale détectée
     * @param destLangue                langue de traduction
     * Affiche les éléments fournis sur l'interface
     * Un élément nul n'est pas affiché
     */
    private fun displayTrad(sourceText: String?, destText:String?, sourceLangue: String?, detectLanguage: String?, destLangue: String?) {

        val detectedLanguageUI = findViewById<TextView>(R.id.detectedLanguageText)

        // Affichage des deux textes
        if(sourceText != null) {
            sourceTextUI.setText(sourceText)
        }

        if(destText != null) {
            destTextUI.text = destText
        }

        // Si la langue est détectée (option "Détecter la langue") sourceLangue et detectLanguage sont différentes
        // on affiche donc la langue détectée
        if(detectLanguage != null && sourceLangue != null && detectLanguage != sourceLangue) {
            val text = "Langue détectée :\n${findMatchingLang(detectLanguage)}"
            detectedLanguageUI.text = text
        } else {
            detectedLanguageUI.text = ""
        }

        // Modification des spinners
        setSpinners(findMatchingLang(sourceLangue), findMatchingLang(destLangue))
    }

    /**
     * @param langSource    langue source
     * @param langDest      langue de traduction
     * Défini les spinners sur les langues renseignées
     * Si une langue n'est pas renseignée (null) le spinner conscerné n'est pas modifié
     */
    private fun setSpinners(langSource: String?, langDest: String?) {
        if(langSource != null) {
            spinnerSource.setSelection(languagesArraySource.indexOf(langSource))
        }
        if(langDest != null) {
            spinnerDest.setSelection(languagesArrayDest.indexOf(langDest))
        }
    }

    /**
     * @param code          code DeepL
     * @return langue?      langue correspondante
     * Renvoie la langue correspondant au code DeepL fourni
     */
    private fun findMatchingLang(code: String?): String? {
        // On retourne null si l'entrée est nulle ou le dictionnaire vide
        if (code == null || languages.size == 0) {
            return null
        }

        // On a défini que "" correspond à "Détecter la langue"
        if (code == "") {
            return "Détecter la langue"
        }

        // Filtrage de languages pour retrouver la langue correspondante
        return languages.filter { it.value == code }.keys.first()
    }

    /**
     * @param textSource                texte original
     * @param textTranslated            texte traduit
     * @param languageSource            langue originale renseignée
     * @param languageDetect            langue originale détectée
     * @param languageDest              langue de traduction
     * Ajoute la traduction à l'historique
     */
    private fun appendHist(textSource: String, textTranslated: String, languageSource: String, languageDetect: String, languageDest: String) {
        // Passage en mode édition
        val editor = historyPreferences.edit()

        // Initialisation des variables
        var list = listOf<String>()
        var requestNum = 0

        // Récupération des données sauvegardées
        try {
            val set = historyPreferences.getStringSet("History", setOf())
            list = set?.toList() as List<String>
            requestNum = historyPreferences.getInt("requestNum", 0)
        } catch (_: Error) {

        }

        // Création d'un élément d'historique avec les données fournies
        val ele = HistoryElement(requestNum, textSource, textTranslated, languageSource, languageDetect, languageDest)

        // On retire les éléments en trop si il y a plus de 9 éléments déjà enregistrés
        if (list.size > 9) {
            var history = arrayOf<HistoryElement>()
            for (n in list.indices) {
                history += HistoryElement(list[n])
            }
            history.sortBy { ele -> -ele.id }
            history = history.take(9).toTypedArray()
            list = listOf()
            for (ele in history.iterator()) {
                list += ele.save()
            }
        }


        // Ajout de l'élément à la liste des éléments
        val set = HashSet(list + ele.save())


        // Sauvegarde des données mises à jour
        editor.putStringSet("History", set)
        editor.putInt("requestNum", requestNum + 1)
        editor.apply()
    }

    ////// PRESSE PAPIER //////

    /**
     * @param view      nécessaire à une méthode onClick
     * Méthode appelée par l'appuie sur le bouton "Coller"
     * Colle le texte du presse papier dans la zone de texte à traduire
     */
    fun onClickPaste(view: View) {
        val clipData = clipboard.primaryClip
        if (clipData != null) {
            val item = clipData.getItemAt(0)
            val text = item.text.toString()
            sourceTextUI.setText(text)
        }
    }

    /**
     * @param view      nécessaire à une méthode onClick
     * Méthode appelée par l'appui sur le bouton "Copier"
     * Copie le texte traduit dans le presse papier
     */
    fun onClickCopy(view: View) {
        val clip = ClipData.newPlainText("label", findViewById<TextView>(R.id.translationTextView).text)
        clipboard.setPrimaryClip(clip)
    }

}