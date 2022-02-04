package com.pandey.suno.util

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun jsonDateToShortDate(jsonDate: String?): String {
        if (jsonDate == null) {
            return "-"
        }
        val inFormat = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss",
            Locale.getDefault()
        )
        val date = inFormat.parse(jsonDate) ?: return "-"

        val outputFormat =
            DateFormat.getDateInstance(
                DateFormat.SHORT,
                Locale.getDefault()
            )
        return outputFormat.format(date)
    }
}