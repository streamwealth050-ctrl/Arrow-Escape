package com.example.ui.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sin

/**
 * Sound effect manager implemented with Android SoundPool for ultra low-latency playback
 * of game sounds (tap clicks, escape whooshes, blocked buzzer, success jingle, button clicks).
 */
class SoundEffectManager(private val context: Context) {

    private val soundPool: SoundPool
    private var clickSoundId: Int = 0
    private var escapeSoundId: Int = 0
    private var blockedSoundId: Int = 0
    private var successJingleId: Int = 0
    private var hintSoundId: Int = 0

    private val scope = CoroutineScope(Dispatchers.IO)

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(6)
            .setAudioAttributes(audioAttributes)
            .build()

        // Generate synthetic audio files in cache and load into SoundPool
        scope.launch {
            loadSounds()
        }
    }

    private fun loadSounds() {
        try {
            val soundsDir = File(context.cacheDir, "sounds").apply { mkdirs() }

            // 1. Satisfying tactile click sound (for arrow tap)
            val clickFile = File(soundsDir, "arrow_click.wav")
            generateWav(clickFile, listOf(1200.0 to 0.025, 800.0 to 0.035), clickEnvelope = true)
            clickSoundId = soundPool.load(clickFile.absolutePath, 1)

            // 2. Whoosh / slide sound (for arrow escape)
            val escapeFile = File(soundsDir, "arrow_escape.wav")
            generateWav(escapeFile, listOf(587.33 to 0.05, 783.99 to 0.08, 1174.66 to 0.12))
            escapeSoundId = soundPool.load(escapeFile.absolutePath, 1)

            // 3. Blocked / error thud sound
            val blockedFile = File(soundsDir, "arrow_blocked.wav")
            generateWav(blockedFile, listOf(170.0 to 0.08, 130.0 to 0.12))
            blockedSoundId = soundPool.load(blockedFile.absolutePath, 1)

            // 4. Success celebration jingle upon level cleared
            val successFile = File(soundsDir, "level_success.wav")
            generateWav(
                successFile,
                listOf(
                    523.25 to 0.09, // C5
                    659.25 to 0.09, // E5
                    783.99 to 0.09, // G5
                    1046.5 to 0.28  // C6
                )
            )
            successJingleId = soundPool.load(successFile.absolutePath, 1)

            // 5. Hint sparkle
            val hintFile = File(soundsDir, "hint_sparkle.wav")
            generateWav(hintFile, listOf(880.0 to 0.08, 1318.5 to 0.16))
            hintSoundId = soundPool.load(hintFile.absolutePath, 1)
        } catch (_: Exception) {
            // Graceful fallback
        }
    }

    fun playClick(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (hapticsEnabled) vibrateLight()
        if (soundEnabled && clickSoundId != 0) {
            soundPool.play(clickSoundId, 0.9f, 0.9f, 1, 0, 1.0f)
        }
    }

    fun playEscape(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (hapticsEnabled) vibrateLight()
        if (soundEnabled && escapeSoundId != 0) {
            soundPool.play(escapeSoundId, 1.0f, 1.0f, 2, 0, 1.0f)
        }
    }

    fun playBlocked(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (hapticsEnabled) vibrateMedium()
        if (soundEnabled && blockedSoundId != 0) {
            soundPool.play(blockedSoundId, 0.9f, 0.9f, 2, 0, 1.0f)
        }
    }

    fun playSuccessJingle(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (hapticsEnabled) vibrateSuccess()
        if (soundEnabled && successJingleId != 0) {
            soundPool.play(successJingleId, 1.0f, 1.0f, 3, 0, 1.0f)
        }
    }

    fun playHint(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (hapticsEnabled) vibrateLight()
        if (soundEnabled && hintSoundId != 0) {
            soundPool.play(hintSoundId, 0.85f, 0.85f, 1, 0, 1.0f)
        }
    }

    private fun generateWav(file: File, tones: List<Pair<Double, Double>>, clickEnvelope: Boolean = false) {
        val sampleRate = 22050
        var totalSamples = 0
        tones.forEach { (_, dur) -> totalSamples += (dur * sampleRate).toInt() }
        val pcmData = ShortArray(totalSamples)

        var offset = 0
        for ((freq, dur) in tones) {
            val samples = (dur * sampleRate).toInt()
            for (i in 0 until samples) {
                val angle = 2.0 * Math.PI * i / (sampleRate / freq)
                val envelope = if (clickEnvelope) {
                    (1.0 - (i.toDouble() / samples)).coerceIn(0.0, 1.0)
                } else {
                    if (i < 80) i / 80.0
                    else if (i > samples - 80) (samples - i) / 80.0
                    else 1.0
                }
                val sample = (sin(angle) * 14000.0 * envelope).toInt().toShort()
                if (offset + i < pcmData.size) {
                    pcmData[offset + i] = sample
                }
            }
            offset += samples
        }

        val dataSize = pcmData.size * 2
        val header = createWavHeader(dataSize, sampleRate)

        FileOutputStream(file).use { fos ->
            fos.write(header)
            val byteBuffer = ByteBuffer.allocate(dataSize).order(ByteOrder.LITTLE_ENDIAN)
            for (s in pcmData) {
                byteBuffer.putShort(s)
            }
            fos.write(byteBuffer.array())
        }
    }

    private fun createWavHeader(dataLength: Int, sampleRate: Int): ByteArray {
        val totalLength = dataLength + 36
        val channels = 1
        val byteRate = sampleRate * channels * 2
        val header = ByteArray(44)

        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalLength and 0xff).toByte()
        header[5] = ((totalLength shr 8) and 0xff).toByte()
        header[6] = ((totalLength shr 16) and 0xff).toByte()
        header[7] = ((totalLength shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // Subchunk1Size for PCM
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // AudioFormat 1 = PCM
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = 2 // BlockAlign = channels * bitsPerSample / 8
        header[33] = 0
        header[34] = 16 // BitsPerSample
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (dataLength and 0xff).toByte()
        header[41] = ((dataLength shr 8) and 0xff).toByte()
        header[42] = ((dataLength shr 16) and 0xff).toByte()
        header[43] = ((dataLength shr 24) and 0xff).toByte()

        return header
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

    fun release() {
        soundPool.release()
    }
}
