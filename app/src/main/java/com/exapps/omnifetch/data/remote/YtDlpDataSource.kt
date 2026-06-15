package com.exapps.omnifetch.data.remote

import android.content.Context
import android.util.Log
import com.exapps.omnifetch.domain.model.FormatOption
import com.exapps.omnifetch.domain.model.VideoInfo
import com.github.yausername.youtubedl_android.YoutubeDL
import com.github.yausername.youtubedl_android.YoutubeDLRequest
import com.github.yausername.youtubedl_android.YoutubeDLResponse
import com.github.yausername.youtubedl_android.YoutubeDLUpdateChannel
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YtDlpDataSource @Inject constructor(
    private val context: Context
) {
    private val youtubeDL = YoutubeDL.getInstance()

    suspend fun fetchVideoInfo(url: String): VideoInfo {
        val request = YoutubeDLRequest(url)
            .addOption("--dump-json")
            .addOption("--no-playlist")

        val streamInfo = youtubeDL.getInfo(request)
        return parseVideoInfo(streamInfo, url)
    }

    suspend fun fetchPlaylistInfo(url: String): VideoInfo {
        val request = YoutubeDLRequest(url)
            .addOption("--dump-json")
            .addOption("--flat-playlist")

        val streamInfo = youtubeDL.getInfo(request)
        val jsonStr = streamInfo.toString()

        return try {
            if (jsonStr.trimStart().startsWith("[")) {
                val array = JSONArray(jsonStr)
                val items = mutableListOf<VideoInfo>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    items.add(parsePlaylistItem(obj))
                }
                VideoInfo(
                    id = url.hashCode().toString(),
                    title = "Playlist (${items.size} videos)",
                    isPlaylist = true,
                    playlistItems = items,
                    webpageUrl = url
                )
            } else {
                val json = JSONObject(jsonStr)
                parseVideoInfo(json, url)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing playlist info", e)
            VideoInfo(title = "Error parsing playlist", webpageUrl = url)
        }
    }

    fun downloadVideo(
        url: String,
        formatId: String,
        outputFile: File,
        progressListener: (Float, Long) -> Unit
    ): YoutubeDLResponse {
        val request = YoutubeDLRequest(url)

        if (formatId == "audio_only") {
            request.addOption("-x")
            request.addOption("--audio-format", "mp3")
            request.addOption("--embed-thumbnail")
            request.addOption("--add-metadata")
        } else if (formatId.contains("+")) {
            val parts = formatId.split("+")
            request.addOption("-f", "${parts[0]}+${parts[1]}")
            request.addOption("--merge-output-format", "mp4")
        } else {
            request.addOption("-f", "$formatId+bestaudio/best")
            request.addOption("--merge-output-format", "mp4")
        }

        request.addOption("-o", outputFile.absolutePath)
        request.addOption("--newline")
        request.addOption("--no-playlist")

        return youtubeDL.execute(request, null, progressListener)
    }

    suspend fun updateBinary() {
        youtubeDL.updateYoutubeDL(context, YoutubeDLUpdateChannel.STABLE)
    }

    private fun parseVideoInfo(streamInfo: com.github.yausername.youtubedl_android.VideoInfo, url: String): VideoInfo {
        return try {
            val jsonStr = streamInfo.toString()
            val json = JSONObject(jsonStr)

            val formats = mutableListOf<FormatOption>()

            if (json.has("formats")) {
                val formatsArray = json.getJSONArray("formats")
                for (i in 0 until formatsArray.length()) {
                    val formatObj = formatsArray.getJSONObject(i)
                    val format = parseFormat(formatObj)
                    if (format != null) {
                        formats.add(format)
                    }
                }
            }

            VideoInfo(
                id = json.optString("id", ""),
                title = json.optString("title", streamInfo.title ?: "Unknown"),
                thumbnail = json.optString("thumbnail", streamInfo.thumbnail ?: ""),
                duration = json.optLong("duration", 0L),
                uploader = json.optString("uploader", json.optString("channel", "Unknown")),
                webpageUrl = json.optString("webpage_url", url),
                formats = buildRecommendedFormats(formats)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing video info", e)
            VideoInfo(
                title = streamInfo.title ?: "Error parsing video",
                thumbnail = streamInfo.thumbnail ?: "",
                webpageUrl = url
            )
        }
    }

    private fun parsePlaylistItem(json: JSONObject): VideoInfo {
        return VideoInfo(
            id = json.optString("id", ""),
            title = json.optString("title", json.optString("url", "Unknown")),
            thumbnail = json.optString("thumbnail", ""),
            webpageUrl = json.optString("url", ""),
            duration = json.optLong("duration", 0L)
        )
    }

    private fun parseFormat(formatObj: JSONObject): FormatOption? {
        val formatId = formatObj.optString("format_id", "")
        val ext = formatObj.optString("ext", "")
        val vcodec = formatObj.optString("vcodec", "none")
        val acodec = formatObj.optString("acodec", "none")
        val width = formatObj.optInt("width", 0)
        val height = formatObj.optInt("height", 0)
        val fps = formatObj.optInt("fps", 0).takeIf { it > 0 }
        val fileSize = formatObj.optLong("filesize", 0L).takeIf { it > 0 }
            ?: formatObj.optLong("filesize_approx", 0L).takeIf { it > 0 }
        val tbr = formatObj.optDouble("tbr", 0.0).takeIf { it > 0 }

        val isAudioOnly = vcodec == "none" && acodec != "none"

        if (formatId.isEmpty() || ext.isEmpty()) return null

        val resolution = when {
            isAudioOnly -> "audio"
            height > 0 -> "${height}p"
            width > 0 -> "${width}p"
            else -> "unknown"
        }

        return FormatOption(
            formatId = formatId,
            extension = ext,
            resolution = resolution,
            fileSize = fileSize,
            vcodec = vcodec,
            acodec = acodec,
            fps = fps,
            isAudioOnly = isAudioOnly,
            width = width.takeIf { it > 0 },
            height = height.takeIf { it > 0 },
            tbr = tbr
        )
    }

    private fun buildRecommendedFormats(allFormats: List<FormatOption>): List<FormatOption> {
        val recommended = mutableListOf<FormatOption>()

        val videoResolutions = listOf("2160p", "1080p", "720p", "480p", "360p")
        for (res in videoResolutions) {
            val bestFormat = allFormats
                .filter { it.resolution == res && !it.isAudioOnly }
                .maxByOrNull { it.tbr ?: 0.0 }
            if (bestFormat != null) {
                recommended.add(bestFormat)
            }
        }

        val bestAudio = allFormats
            .filter { it.isAudioOnly }
            .maxByOrNull { it.tbr ?: 0.0 }
        if (bestAudio != null) {
            recommended.add(bestAudio.copy(
                formatId = "audio_only",
                resolution = "Audio Only"
            ))
        }

        if (recommended.isEmpty()) {
            return allFormats.take(10)
        }

        return recommended
    }

    companion object {
        private const val TAG = "YtDlpDataSource"
    }
}
