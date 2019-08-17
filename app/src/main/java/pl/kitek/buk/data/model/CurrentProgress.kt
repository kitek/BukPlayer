package pl.kitek.buk.data.model

import kotlin.math.round

data class CurrentProgress(
    val completedPercent: Int = 0,
    val timeLeftInSeconds: Long = 0,
    val currentFileName: String = "",
    val currentPlaybackPosition: Long = 0L,
    val currentWindowIndex: Int = 0
) {

    val timeLeftFormatted: String
        get() {
            val hourInSeconds = 60 * 60
            val hours = timeLeftInSeconds / hourInSeconds
            val minutes = timeLeftInSeconds.rem(hourInSeconds) / 60

            return if (hours > 0) {
                "$hours h $minutes min"
            } else {
                "$minutes min"
            }
        }

    companion object {

        fun of(
            playbackPosition: Long,
            fileIndex: Int,
            files: List<BookFile>,
            totalDurationInSeconds: Long
        ): CurrentProgress {
            val bookFile = files[fileIndex]
            var heardSoFar = (0 until fileIndex).map { files[it].duration }.sum()
            heardSoFar += round(playbackPosition / 1000f).toLong()

            val timeLeft = totalDurationInSeconds - heardSoFar
            val completedPercent = (heardSoFar * 100) / totalDurationInSeconds.toFloat()

            return CurrentProgress(
                round(completedPercent).toInt(),
                timeLeft,
                bookFile.path,
                playbackPosition,
                fileIndex
            )
        }
    }
}
