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
}