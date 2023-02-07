package com.example.johnnydeepl

import java.io.Serializable

class HistoryElement: Serializable {
    var id = 0                                  // Id permettant de classer les éléments par ordre d'ancienneté
    lateinit var textSource: String             // Texte original
    lateinit var textTranslated: String         // Texte traduit
    lateinit var languageSource: String         // Langue originale renseignée
    lateinit var languageDetect: String         // Langue originale détectée
    lateinit var languageDest: String           // Langue de traduction

    /**
     * Constructeur explicite
     */
    constructor(
        id: Int,
        textSource: String,
        textTranslated: String,
        languageSource: String,
        languageDetect: String,
        languageDest: String
    ) {
        this.id = id
        this.textSource = textSource
        this.textTranslated = textTranslated
        this.languageSource = languageSource
        this.languageDetect = languageDetect
        this.languageDest = languageDest
    }

    /**
     * Constructeur prenant en entrée un chaine de caractères
     * représentant une sauvegarde (obtenue avec la fonction "save()")
     */
    constructor(fromSave: String){
        try {
            val list = fromSave.split("|||")
            this.id = list[0].toInt()
            this.textSource = list[1].replace("||-", "||")
            this.textTranslated = list[2].replace("||-", "||")
            this.languageSource =  list[3]
            this.languageDetect =  list[4]
            this.languageDest = list[5]
        } catch (e: java.lang.Error) {
            println(e.message)
        }
    }

    /**
     * Fonction qui concatène la classe en une string
     * Pourrait être remplacé par l'utilisation de méthodes de Serializable,
     * mais prendrait plus de place et serait moins intéressant :)
     */
    fun save(): String {
        // Prévention des injections de séparateurs
        val sourceToSave = textSource.replace("||", "||-")
        val tradToSave = textTranslated.replace("||", "||-")

        return "$id|||$sourceToSave|||$tradToSave|||$languageSource|||$languageDetect|||$languageDest"
    }

}