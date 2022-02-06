package com.pandey.suno.util

import android.os.Build
import android.text.Html
import android.text.Spanned

object HtmlUtils {
    fun htmlToSpannable(htmlDesc: String): Spanned {
// 1
        var newHtmlDesc = htmlDesc.replace("\n".toRegex(), "")
        newHtmlDesc = newHtmlDesc.replace("(<(/)img>)|(<img.+?>)".
        toRegex(), "")
        // 2
        val descSpan: Spanned
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            descSpan = Html.fromHtml(newHtmlDesc,
                Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            descSpan = Html.fromHtml(newHtmlDesc)
        }
        return descSpan
    }
}
