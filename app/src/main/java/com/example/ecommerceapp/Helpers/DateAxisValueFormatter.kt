package com.example.ecommerceapp.Helpers

import android.util.Log
import com.example.ecommerceapp.Utils.Globals
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DateAxisValueFormatter : IAxisValueFormatter {
    private val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())

    override fun getFormattedValue(value: Float, axis: AxisBase?): String {
        // Convert the timestamp to a formatted date string
        return dateFormat.format(Date(value.toLong()))
    }
}




