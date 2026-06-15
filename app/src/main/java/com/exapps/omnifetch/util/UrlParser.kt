package com.exapps.omnifetch.util

import android.webkit.URLUtil

object UrlParser {

    fun isValidUrl(url: String): Boolean {
        return try {
            val trimmed = url.trim()
            (trimmed.startsWith("http://") || trimmed.startsWith("https://")) &&
                    URLUtil.isValidUrl(trimmed)
        } catch (_: Exception) {
            false
        }
    }

    fun extractUrlFromText(text: String): String? {
        val urlPattern = Regex("""https?://[^\s<>\"']+""")
        return urlPattern.find(text.trim())?.value
    }

    fun isPlaylistUrl(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("list=") ||
                lower.contains("/playlist") ||
                lower.contains("playlist?") ||
                lower.contains("playlist/") ||
                lower.contains("sets?")
    }
}
