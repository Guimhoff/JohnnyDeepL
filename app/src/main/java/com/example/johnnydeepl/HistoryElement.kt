package com.example.johnnydeepl

class HistoryElement {
    var id = 0
    lateinit var textSource: String
    lateinit var textTranslated: String
    lateinit var languageSource: String
    lateinit var languageDetect: String
    lateinit var languageDest: String

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

    constructor(fromSave: String){
        println(fromSave)
        try {
            val list = fromSave.split("|||")
            println(list)
            this.id = list[0].toInt()
            this.textSource = list[1]
            this.textTranslated = list[2]
            this.languageSource =  list[3]
            this.languageDetect =  list[4]
            this.languageDest = list[5]
        } catch (e: java.lang.Error) {

        }
    }

    fun save(): String {

        return "$id|||$textSource|||$textTranslated|||$languageSource|||$languageDetect|||$languageDest"
    }

}