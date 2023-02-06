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
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.json.JSONException

class ParametersActivity : AppCompatActivity() {
    private var DeepLKey = ""
    private lateinit var preferencesFile: SharedPreferences
    private lateinit var deepLKeyText: EditText

    var textNoKey = "Rentrez une clé pour accéder à votre consommation..."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parameters)
        AndroidNetworking.initialize(this)

        confText()

        loadPreferences()
    }

    private fun loadPreferences(){
        preferencesFile = getSharedPreferences("DeepLKey", MODE_PRIVATE)
        DeepLKey = preferencesFile.getString("DeepLKey", "").toString()

        findViewById<TextView>(R.id.DeepLKeyText).text = DeepLKey

        if (DeepLKey != "") {
            getDeepLUsage()
        } else {
            showNoKey()
        }
    }

    private fun confText() {
        deepLKeyText = findViewById(R.id.DeepLKeyText)

        deepLKeyText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onClickConfirmKey(this)
                true
            } else {
                false
            }
        }
    }

    private fun saveKey() {
        val editor = preferencesFile.edit()

        editor.putString("DeepLKey", DeepLKey)
        editor.commit()
    }

    private fun getDeepLUsage() {
        val that = this

        AndroidNetworking.get("https://api-free.deepl.com/v2/usage")
            .addHeaders("Authorization", "DeepL-Auth-Key $DeepLKey")
            .build()
            .getAsJSONObject(object: JSONObjectRequestListener{
                override fun onResponse(response: JSONObject) {
                    try{
                        val characterCount = response.getInt("character_count")
                        val characterLimit = response.getInt("character_limit")

                        updateUsage(characterCount, characterLimit)
                        saveKey()
                    } catch (e: JSONException) {
                        showNoKey()
                        Toast.makeText(that, "Une erreur est survenue\n ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onError(anError: ANError?) {
                    showNoKey()
                    Toast.makeText(that, "Une erreur de connexion est survenue\n" + anError.toString(), Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun updateUsage(charCount: Int, charLimit: Int) {
        findViewById<ProgressBar>(R.id.DeepLProgressBar).progress = 100 * charCount / charLimit
        findViewById<TextView>(R.id.DeepLUsageText).text = "$charCount sur $charLimit caractères utilisés\n(${charLimit - charCount} restants)"
    }

    private fun showNoKey() {
        findViewById<ProgressBar>(R.id.DeepLProgressBar).progress = 0
        findViewById<TextView>(R.id.DeepLUsageText).text = textNoKey
    }

    fun onClickBack(view: View) {
        finish()
    }

    fun onClickConfirmKey(view: ParametersActivity) {
        // On cache le clavier et on sort du focus
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(deepLKeyText.windowToken, 0)
        deepLKeyText.clearFocus()

        // On enlève les potentiels espaces ajoutés par erreur avant ou après la clé
        DeepLKey = deepLKeyText.text.toString().trim()
        deepLKeyText.setText(DeepLKey)

        if (DeepLKey == "") {
            saveKey()
            showNoKey()
        } else {
            getDeepLUsage()
        }
    }


}