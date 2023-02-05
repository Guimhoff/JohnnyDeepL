package com.example.johnnydeepl

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.json.JSONException
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    private var DeepLKey = ""
    private lateinit var clipboard: ClipboardManager
    private var languages = hashMapOf<String, String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AndroidNetworking.initialize(this)
        clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        loadLanguages()
    }

    override fun onResume() {
        super.onResume()

        DeepLKey = loadPreferences()!!
    }

    private fun loadPreferences(): String? {
        return getSharedPreferences("DeepLKey", MODE_PRIVATE).getString("DeepLKey", "")
    }

    private fun loadLanguages() {
        val that = this



        AndroidNetworking.get("https://api-free.deepl.com/v2/languages")
            .addHeaders("Authorization", "DeepL-Auth-Key $DeepLKey")
            .build()
            .getAsJSONObject(object: JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try{
                        val flavours = response.getJSONArray("flavours")


                        val characterCount = response.getInt("character_count")
                        val characterLimit = response.getInt("character_limit")


                    } catch (e: JSONException) {

                        Toast.makeText(that, "Une erreur est survenue ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(anError: ANError?) {

                    Toast.makeText(that, "Une erreur de connexion est survenue " + anError.toString(), Toast.LENGTH_SHORT).show()
                }
            })

    }




    fun onClickParameters(view: View) {
        val i = Intent(this, ParametersActivity::class.java)
        startActivity(i)
    }

    fun onClickTranslate(view: View) {

        if (DeepLKey == "") {
            Toast.makeText(this, "Veuillez entrer une clé DeepL", Toast.LENGTH_SHORT).show()
            val i = Intent(this, ParametersActivity::class.java)
            startActivity(i)
            return
        }

        findViewById<TextView>(R.id.translationTextView).text = DeepLKey
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