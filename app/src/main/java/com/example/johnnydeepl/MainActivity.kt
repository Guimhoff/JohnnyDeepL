package com.example.johnnydeepl

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    private var DeepLKey = ""
    private lateinit var clipboard: ClipboardManager

    private var languages = hashMapOf<String, String>()
    private var sourceLangue = ""
    private var destLangue = "null"

    private lateinit var spinnerSource: Spinner
    private lateinit var spinnerDest: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AndroidNetworking.initialize(this)
        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    }

    override fun onResume() {
        super.onResume()

        DeepLKey = loadPreferences()!!
        loadLanguages()
    }

    private fun loadPreferences(): String? {
        return getSharedPreferences("DeepLKey", MODE_PRIVATE).getString("DeepLKey", "")
    }

    private fun loadLanguages() {
        if (testKeyNull()) {
            return
        }

        val that = this
        spinnerSource = findViewById<Spinner>(R.id.sourceLanguageSpinner)
        spinnerDest = findViewById<Spinner>(R.id.destinationLanguageSpinner)


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

                        var languagesArraySource = arrayOf("Détecter la langue") + languageArray
                        val adapterSource = ArrayAdapter(that, android.R.layout.simple_spinner_dropdown_item, languagesArraySource)
                        spinnerSource.adapter = adapterSource

                        var languagesArrayDest = languageArray.toTypedArray()
                        val adapterDest = ArrayAdapter(that, android.R.layout.simple_spinner_dropdown_item, languagesArrayDest)
                        spinnerDest.adapter = adapterDest

                    } catch (e: JSONException) {
                        Toast.makeText(that, "Une erreur est survenue lors de la récupération des langages\n ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onError(anError: ANError?) {
                    Toast.makeText(that, "Une erreur de connexion est survenue lors de la récupération des langages\n" + anError.toString(), Toast.LENGTH_LONG).show()
                }
            })

        spinnerSource.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                sourceLangue = languages[parent.getItemAtPosition(position).toString()].toString()
                if (sourceLangue == "null") sourceLangue = ""
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // aucun élément n'a été sélectionné
            }
        }

        spinnerDest.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                destLangue = languages[parent.getItemAtPosition(position).toString()].toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // aucun élément n'a été sélectionné
            }
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
        if (testKeyNull()) {
            return
        }

        val sourceTextUI = findViewById<EditText>(R.id.sourceText)
        val sourceText = sourceTextUI.text.toString()

        val destTextUI = findViewById<TextView>(R.id.translationTextView)
        var destText = ""

        val detectedLanguageUI = findViewById<TextView>(R.id.detectedLanguageText)
        detectedLanguageUI.text = ""

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
                        destText = trad.getString("text")
                        destTextUI.text = destText

                        val detectLanguage = trad.getString("detected_source_language")

                        if (detectLanguage != sourceLangue) {
                            detectedLanguageUI.text = "Langue détectée :\n${languages.filter { it.value == detectLanguage }.keys.first()}"
                        }

                    } catch (e: JSONException) {
                        Toast.makeText(that, "Une erreur est survenue\n ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onError(anError: ANError?) {
                    Toast.makeText(that, "Une erreur de connexion est survenue\n" + anError.toString(), Toast.LENGTH_LONG).show()
                }
            })



    }

    fun onClickPaste(view: View) {
        val clipData = clipboard.primaryClip
        if (clipData != null) {
            val item = clipData.getItemAt(0)
            val text = item.text.toString()
            findViewById<EditText>(R.id.sourceText).setText(text)
        }
    }

    fun onClickCopy(view: View) {
        val clip = ClipData.newPlainText("label", findViewById<TextView>(R.id.translationTextView).text)
        clipboard.setPrimaryClip(clip)
    }

}