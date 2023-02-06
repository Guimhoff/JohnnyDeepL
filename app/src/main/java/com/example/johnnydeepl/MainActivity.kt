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
import androidx.core.widget.doOnTextChanged
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    companion object {
        var languages = hashMapOf<String, String>()
        var languagesArraySource = arrayOf<String>()
        var languagesArrayDest = arrayOf<String>()
    }

    private var DeepLKey = ""
    private var lastSource = ""
    private var lastDest = ""
    private lateinit var clipboard: ClipboardManager

    private var sourceLangue = ""
    private var destLangue = "null"

    private lateinit var spinnerSource: Spinner
    private lateinit var spinnerDest: Spinner

    private lateinit var sourceTextUI: EditText
    private lateinit var destTextUI: TextView
    
    private lateinit var historyPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AndroidNetworking.initialize(this)
        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        confTextZone()
        loadPreferences()
        loadLanguages()
        getHistoryElement()
    }

    override fun onResume() {
        super.onResume()
        loadPreferences()
        loadLanguages()
    }

    private fun loadPreferences() {
        DeepLKey = getSharedPreferences("DeepLKey", MODE_PRIVATE).getString("DeepLKey", "")!!
        historyPreferences = getSharedPreferences("History", Context.MODE_PRIVATE)
        lastSource = historyPreferences.getString("lastSource", "")!!
        lastDest = historyPreferences.getString("lastDest", "")!!
    }

    private fun confTextZone() {
        sourceTextUI = findViewById(R.id.sourceText)
        destTextUI = findViewById(R.id.translationTextView)

        sourceTextUI.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                destTextUI.text = ""
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Appelé avant que le texte ne soit modifié
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Appelé lorsque le texte est en train d'être modifié
            }
        })

        sourceTextUI.setOnFocusChangeListener { view, hasFocus ->
            if (!hasFocus) {
                // On cache le clavier quand le focus est perdu
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(sourceTextUI.windowToken, 0)
            }
        }
    }

    private fun loadLanguages() {
        if (testKeyNull() || languages.size > 0) {
            confSpinners()
            return
        }

        val that = this


        AndroidNetworking.get("https://api-free.deepl.com/v2/languages")
            .addHeaders("Authorization", "DeepL-Auth-Key $DeepLKey")
            .build()
            .getAsJSONArray(object: JSONArrayRequestListener {
                override fun onResponse(response: JSONArray) {
                    try{
                        for (i in 0 until response.length()){
                            val obj = response.getJSONObject(i)
                            languages[obj.getString("name")] = obj.getString("language")
                        }

                        var languageArray = languages.keys.toList()
                        languageArray = languageArray.sorted()

                        languagesArraySource = arrayOf("Détecter la langue") + languageArray
                        languagesArrayDest = languageArray.toTypedArray()

                        confSpinners()
                    } catch (e: JSONException) {
                        Toast.makeText(that, "Une erreur est survenue lors de la récupération des langages\n ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onError(anError: ANError?) {
                    Toast.makeText(that, "Une erreur de connexion est survenue lors de la récupération des langages\n" + anError.toString(), Toast.LENGTH_LONG).show()
                }
            })

    }

    private fun confSpinners() {
        spinnerSource = findViewById<Spinner>(R.id.sourceLanguageSpinner)
        spinnerDest = findViewById<Spinner>(R.id.destinationLanguageSpinner)


        val adapterSource = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, languagesArraySource)
        spinnerSource.adapter = adapterSource

        val adapterDest = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, languagesArrayDest)
        spinnerDest.adapter = adapterDest
        
        setSpinners(lastSource, lastDest)
        
        spinnerSource.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                lastSource = parent.getItemAtPosition(position).toString()
                sourceLangue = languages[lastSource].toString()
                if (sourceLangue == "null") sourceLangue = ""

                val editor = historyPreferences.edit()
                editor.putString("lastSource", lastSource)
                editor.commit()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // aucun élément n'a été sélectionné
            }
        }

        spinnerDest.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                lastDest = parent.getItemAtPosition(position).toString()
                destLangue = languages[lastDest].toString()

                val editor = historyPreferences.edit()
                editor.putString("lastDest", lastDest)
                editor.commit()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // aucun élément n'a été sélectionné
            }
        }
    }

    private fun getHistoryElement() {
        val i = intent

        if (i.getSerializableExtra("HistoryElement") != null) {
            // dépréciée mais la fonction qui la remplace n'est disponible qu'à partir d'Android 33
            val ele = i.getSerializableExtra("HistoryElement") as HistoryElement

            displayTrad(ele.textSource, ele.textTranslated, ele.languageSource, ele.languageDetect, ele.languageDest)
        }
    }

    private fun testKeyNull(): Boolean {
        if (DeepLKey == "") {
            Toast.makeText(this, "Veuillez entrer une clé DeepL", Toast.LENGTH_SHORT).show()
            val i = Intent(this, ParametersActivity::class.java)
            startActivity(i)
            return true
        }
        return false
    }



    fun onClickParameters(view: View) {
        val i = Intent(this, ParametersActivity::class.java)
        startActivity(i)
    }

    fun onClickHistory(view: View) {
        val i = Intent(this, HistoryActivity::class.java)
        startActivity(i)
    }


    fun onClickTranslate(view: View) {
        // On sort du focus
        sourceTextUI.clearFocus()

        val sourceText = sourceTextUI.text.toString()

        if (testKeyNull() || sourceText == "") {
            return
        }

        val that = this

        AndroidNetworking.get("https://api-free.deepl.com/v2/translate")
            .addHeaders("Authorization", "DeepL-Auth-Key $DeepLKey")
            .addQueryParameter("text", sourceText)
            .addQueryParameter("source_lang", sourceLangue)
            .addQueryParameter("target_lang", destLangue)
            .build()
            .getAsJSONObject(object: JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.d("JSON", response.toString())
                    try{
                        val trad = response.getJSONArray("translations").getJSONObject(0)
                        val destText = trad.getString("text")

                        val detectLanguage = trad.getString("detected_source_language")

                        displayTrad(null, destText, null, detectLanguage, destLangue)
                        appendHist(sourceText, destText, sourceLangue, detectLanguage, destLangue)

                    } catch (e: JSONException) {
                        Toast.makeText(that, "Une erreur est survenue\n ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onError(anError: ANError?) {
                    Toast.makeText(that, "Une erreur de connexion est survenue\n" + anError.toString(), Toast.LENGTH_LONG).show()
                }
            })

    }

    private fun displayTrad(sourceText: String?, destText:String?, sourceLangue: String?, detectLanguage: String?, destLangue: String?) {

        val detectedLanguageUI = findViewById<TextView>(R.id.detectedLanguageText)

        if(sourceText != null) {
            sourceTextUI.setText(sourceText)
        }
        if(destText != null) {
            destTextUI.text = destText
        }
        if(detectLanguage != null && destLangue != null && detectLanguage != destLangue) {
            detectedLanguageUI.text = "Langue détectée :\n${findMatchingLang(detectLanguage)}"
        } else {
            detectedLanguageUI.text = ""
        }

        setSpinners(findMatchingLang(sourceLangue), findMatchingLang(destLangue))
    }

    private fun setSpinners(langSource: String?, langDest: String?) {
        if(langSource != null) {
            spinnerSource.setSelection(languagesArraySource.indexOf(langSource))
        }
        if(langDest != null) {
            spinnerDest.setSelection(languagesArrayDest.indexOf(langDest))
        }
    }

    private fun findMatchingLang(code: String?): String? {
        if (code == null || languages.size == 0) {
            return null
        }

        if (code == "") {
            return "Détecter la langue"
        }

        return languages.filter { it.value == code }.keys.first()
    }

    private fun appendHist(textSource: String, textTranslated: String, languageSource: String, languageDetect: String, languageDest: String) {
        
        val editor = historyPreferences.edit()

        var list = listOf<String>()
        var requestNum = 0

        try {
            val set = historyPreferences.getStringSet("History", setOf())
            list = set?.toList() as List<String>
            requestNum = historyPreferences.getInt("requestNum", 0)
        } catch (_: Error) {

        }

        val ele = HistoryElement(requestNum, textSource, textTranslated, languageSource, languageDetect, languageDest)

        val set = HashSet(list + ele.save())

        editor.putStringSet("History", set)
        editor.putInt("requestNum", requestNum + 1)
        editor.apply()
    }


    fun onClickPaste(view: View) {
        val clipData = clipboard.primaryClip
        if (clipData != null) {
            val item = clipData.getItemAt(0)
            val text = item.text.toString()
            sourceTextUI.setText(text)
        }
    }

    fun onClickCopy(view: View) {
        val clip = ClipData.newPlainText("label", findViewById<TextView>(R.id.translationTextView).text)
        clipboard.setPrimaryClip(clip)
    }

}