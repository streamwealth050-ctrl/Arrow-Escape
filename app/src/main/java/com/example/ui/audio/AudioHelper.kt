package com.example.ui.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class AudioHelper(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    /**
     * Plays a pleasant ascending whoosh/chime tone when an arrow escapes cleanly.
     */
    fun playArrowEscape(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (hapticsEnabled) {
            vibrateLight()
        }
        if (!soundEnabled) return

        scope.launch {
            // Ascending chord frequencies (C5 -> E5 -> G5)
            playTones(listOf(523.25 to 0.06, 659.25 to 0.06, 783.99 to 0.09))
        }
    }

    /**
     * Plays a blocked/thud tone when the player taps a blocked arrow.
     */
    fun playArrowBlocked(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (hapticsEnabled) {
            vibrateMedium()
        }
        if (!soundEnabled) return

        scope.launch {
            // Low buzz/thud (150Hz -> 100Hz)
            playTones(listOf(160.0 to 0.08, 120.0 to 0.12))
        }
    }

    /**
     * Plays a sparkle chime when Hint is activated.
     */
    fun playHint(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (hapticsEnabled) {
            vibrateLight()
        }
        if (!soundEnabled) return

        scope.launch {
            playTones(listOf(783.99 to 0.07, 1046.5 to 0.15))
        }
    }

    /**
     * Plays a fanfare celebration when level is completed.
     */
    fun playLevelCompleted(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (hapticsEnabled) {
            vibrateSuccess()
        }
        if (!soundEnabled) return

        scope.launch {
            playTones(
                listOf(
                    523.25 to 0.08, // C5
                    659.25 to 0.08, // E5
                    783.99 to 0.08, // G5
                    1046.50 to 0.25 // C6
                )
            )
        }
    }

    /**
     * Plays a game over / time's up tone.
     */
    fun playGameOver(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (hapticsEnabled) {
            vibrateMedium()
        }
        if (!soundEnabled) return

        scope.launch {
            playTones(
                listOf(
                    440.0 to 0.12,
                    370.0 to 0.12,
                    311.0 to 0.20
                )
            )
        }
    }

    private fun playTones(tones: List<Pair<Double, Double>>) {
        try {
            val sampleRate = 22050
            var totalSamples = 0
            tones.forEach { (_, dur) -> totalSamples += (dur * sampleRate).toInt() }
            val buffer = ShortArray(totalSamples)

            var offset = 0
            for ((freq, dur) in tones) {
                val samples = (dur * sampleRate).toInt()
                for (i in 0 until samples) {
                    val angle = 2.0 * Math.PI * i / (sampleRate / freq)
                    // Apply envelope to avoid clicking at starts and ends
                    val envelope = if (i < 100) {
                        i / 100.0
                    } else if (i > samples - 100) {
                        (samples - i) / 100.0
                    } else {
                        1.0
                    }
                    val sample = (sin(angle) * 12000.0 * envelope).toInt().toShort()
                    if (offset + i < buffer.size) {
                        buffer[offset + i] = sample
                    }
                }
                offset += samples
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            Thread.sleep((offset * 1000L / sampleRate) + 50)
            audioTrack.release()
        } catch (_: Exception) {
            // Ignore audio generation failures gracefully
        }
    }

    private fun vibrateLight() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(25)
            }
        } catch (_: Exception) {}
    }

    private fun vibrateMedium() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(60)
            }
        } catch (_: Exception) {}
    }

    private fun vibrateSuccess() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 40, 60, 80), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(100)
            }
        } catch (_: Exception) {}
    }
}
