package com.example.johnnydeepl

import android.content.Context
import android.widget.ArrayAdapter

class HistoryAdapter: ArrayAdapter<HistoryElement> {

    val context: Context
    val resource: Int
    val history: Array<HistoryElement>


    constructor(
        context: Context,
        resource: Int,
        history: Array<out HistoryElement>
    ) : super(context, resource, history) {
        this.context = context
        this.resource = resource
        this.history = history as Array<HistoryElement>
    }





}

