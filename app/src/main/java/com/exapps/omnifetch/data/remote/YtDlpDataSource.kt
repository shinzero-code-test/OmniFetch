package com.exapps.omnifetch.data.remote

import android.content.Context
import android.util.Log
import com.exapps.omnifetch.domain.model.FormatOption
import com.exapps.omnifetch.domain.model.VideoInfo
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.YoutubeDLResponse
import com.yausername.youtubedl_android.mapper.VideoInfo as YtDlpVideoInfo
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
        return mapVideoInfo(streamInfo, url)
    }

    suspend fun fetchPlaylistInfo(url: String): VideoInfo {
        val request = YoutubeDLRequest(url)
            .addOption("--dump-json")
            .addOption("--flat-playlist")

        val streamInfo = youtubeDL.getInfo(request)
        return mapVideoInfo(streamInfo, url)
    }

    fun downloadVideo(
        url: String,
        formatId: String,
        outputFile: File,
        progressListener: (Float, Long, String) -> Unit
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
        youtubeDL.updateYoutubeDL(context, YoutubeDL.UpdateChannel.STABLE)
    }

    private fun mapVideoInfo(streamInfo: YtDlpVideoInfo, url: String): VideoInfo {
        return try {
            val formats = mutableListOf<FormatOption>()

            streamInfo.formats?.let { ytFormats ->
                for (i in 0 until ytFormats.size) {
                    val fmt = ytFormats[i]
                    val format = FormatOption(
                        formatId = fmt.formatId ?: "",
                        extension = fmt.ext ?: "",
                        resolution = when {
                            (fmt.height ?: 0) > 0 -> "${fmt.height}p"
                            else -> "audio"
                        },
                        fileSize = fmt.fileSize.takeIf { it > 0 } ?: fmt.fileSizeApproximate.takeIf { it > 0 },
                        vcodec = fmt.vcodec ?: "none",
                        acodec = fmt.acodec ?: "none",
                        fps = fmt.fps.takeIf { it > 0 },
                        isAudioOnly = (fmt.vcodec == "none" || fmt.vcodec.isNullOrEmpty()) && !fmt.acodec.isNullOrEmpty(),
                        width = fmt.width.takeIf { it > 0 },
                        height = fmt.height.takeIf { it > 0 },
                        tbr = fmt.tbr.toDouble().takeIf { it > 0 }
                    )
                    if (format.formatId.isNotEmpty() && format.extension.isNotEmpty()) {
                        formats.add(format)
                    }
                }
            }

            VideoInfo(
                id = streamInfo.id ?: "",
                title = streamInfo.title ?: "Unknown",
                thumbnail = streamInfo.thumbnail ?: "",
                duration = streamInfo.duration.toLong(),
                uploader = streamInfo.uploader ?: "Unknown",
                webpageUrl = streamInfo.webpageUrl ?: url,
                formats = buildRecommendedFormats(formats)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error mapping video info", e)
            VideoInfo(
                title = "Error parsing video",
                webpageUrl = url
            )
        }
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
