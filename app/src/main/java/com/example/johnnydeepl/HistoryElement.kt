package com.example.johnnydeepl

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
        println(fromSave)
        try {
            val list = fromSave.split("|||")
            println(list)
            this.textSource = list[0]
            this.textTranslated = list[1]
            this.languageSource =  list[2]
            this.languageDetect =  list[3]
            this.languageDest = list[4]
        } catch (e: java.lang.Error) {

        }
    }

    fun save(): String {

        return "$textSource|||$textTranslated|||$languageSource|||$languageDetect|||$languageDest"
    }

}