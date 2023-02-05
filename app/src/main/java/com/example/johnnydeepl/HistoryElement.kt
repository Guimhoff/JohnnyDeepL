package com.example.johnnydeepl

import org.intellij.lang.annotations.Language

class HistoryElement {
    lateinit var textSource: String
    lateinit var textTranslated: String
    lateinit var languageSource: String
    lateinit var languageDetect: String
    lateinit var languageDest: String

    constructor(
        textSource: String,
        textTranslated: String,
        languageSource: String,
        languageDetect: String,
        languageDest: String
    ) {
        this.textSource = textSource
        this.textTranslated = textTranslated
        this.languageSource = languageSource
        this.languageDetect = languageDetect
        this.languageDest = languageDest
    }

    constructor(fromSave: String){
        try {
            val liste = fromSave.split("[^\\/]\\|", fromSave)
            this.textSource = liste[0].replace("/|", "|")
            this.textTranslated = liste[1].replace("/|", "|")
            this.languageSource = liste[2]
            this.languageDetect = liste[3]
            this.languageDest = liste[4]
        } catch (e: java.lang.Error) {

        }
    }

    fun save(): String {
        val sourceToSave = textSource.replace("|", "/|")
        val tradToSave = textTranslated.replace("|", "/|")

        return "$sourceToSave|$tradToSave|$languageSource|$languageDetect|$languageDest"
    }

}