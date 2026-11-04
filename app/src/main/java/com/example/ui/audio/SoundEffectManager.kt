package com.example.ui.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.R
import java.util.concurrent.ConcurrentHashMap

/**
 * Universal SoundEffectManager for Arrow Escape.
 *
 * STRICT USER RULES:
 * 1. NO background music (BGM) of any kind.
 * 2. NO synthetic tones or extra beeps.
 * 3. ONLY play the user's authentic 4 audio pack files at the exact designated moments:
 *    - sfx_arrow_move.wav: When an arrow moves / escapes
 *    - sfx_game_win.wav: When a level is won / cleared
 *    - sfx_loss_game.wav: When a game is lost (out of lives/moves/time)
 *    - sfx_game_exit.wav: When quitting / exiting a game (confirmed yes)
 */
class SoundEffectManager(private val context: Context) {

    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (_: Throwable) {
        null
    }

    private var soundPool: SoundPool? = null
    private val readySoundIds = ConcurrentHashMap.newKeySet<Int>()

    // User's custom audio pack IDs
    @Volatile private var arrowMoveSoundId = 0
    @Volatile private var gameWinSoundId = 0
    @Volatile private var lossGameSoundId = 0
    @Volatile private var gameExitSoundId = 0

    init {
        initAudio()
    }

    private fun initAudio() {
        try {
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            val sp = SoundPool.Builder()
                .setMaxStreams(8)
                .setAudioAttributes(attributes)
                .build()

            sp.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0) {
                    readySoundIds.add(sampleId)
                }
            }

            soundPool = sp

            // Load user's custom audio pack directly from raw resources
            try {
                arrowMoveSoundId = sp.load(context, R.raw.sfx_arrow_move, 1)
            } catch (_: Throwable) {}

            try {
                gameWinSoundId = sp.load(context, R.raw.sfx_game_win, 1)
            } catch (_: Throwable) {}

            try {
                lossGameSoundId = sp.load(context, R.raw.sfx_loss_game, 1)
            } catch (_: Throwable) {}

            try {
                gameExitSoundId = sp.load(context, R.raw.sfx_game_exit, 1)
            } catch (_: Throwable) {}
        } catch (_: Throwable) {}
    }

    private fun playSample(soundId: Int, priority: Int = 5) {
        if (soundId == 0) return
        val sp = soundPool ?: return
        try {
            val streamId = sp.play(soundId, 1.0f, 1.0f, priority, 0, 1.0f)
            if (streamId == 0) {
                // If not ready yet on first frame, retry once with slight delay
                sp.play(soundId, 1.0f, 1.0f, priority, 0, 1.0f)
            }
        } catch (_: Throwable) {}
    }

    /**
     * USER SOUND EFFECT 1: Arrow Move
     * Triggered strictly when an unobstructed arrow moves / escapes.
     */
    fun playArrowMove(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (hapticsEnabled) vibrateLight()
        if (soundEnabled) {
            playSample(arrowMoveSoundId, priority = 8)
        }
    }

    fun playEscape(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        playArrowMove(soundEnabled, hapticsEnabled)
    }

    /**
     * USER SOUND EFFECT 2: Game Win
     * Triggered strictly when completing / clearing a level.
     */
    fun playGameWin(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (hapticsEnabled) vibrateSuccess()
        if (soundEnabled) {
            playSample(gameWinSoundId, priority = 10)
        }
    }

    fun playSuccessJingle(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        // No-op for random screens - Game win only plays on level completion
    }

    /**
     * USER SOUND EFFECT 3: Loss the Game
     * Triggered strictly when the player loses the game (out of lives, moves, or time).
     */
    fun playLossGame(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (hapticsEnabled) vibrateGameOver()
        if (soundEnabled) {
            playSample(lossGameSoundId, priority = 10)
        }
    }

    fun playGameOver(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        playLossGame(soundEnabled, hapticsEnabled)
    }

    /**
     * USER SOUND EFFECT 4: Game Exit
     * Triggered strictly when user confirms quitting the game.
     */
    fun playGameExit(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (hapticsEnabled) vibrateLight()
        if (soundEnabled) {
            playSample(gameExitSoundId, priority = 10)
        }
    }

    fun playBlocked(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (hapticsEnabled) vibrateMedium()
    }

    fun playHint(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        if (hapticsEnabled) vibrateLight()
    }

    fun playClick(soundEnabled: Boolean, hapticsEnabled: Boolean) {
        // Silent click, no audio distortion
    }

    private fun vibrateLight() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(14, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(14)
            }
        } catch (_: Throwable) {}
    }

    private fun vibrateMedium() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(32, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(32)
            }
        } catch (_: Throwable) {}
    }

    private fun vibrateSuccess() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 30, 60, 45), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 30, 60, 45), -1)
            }
        } catch (_: Throwable) {}
    }

    private fun vibrateGameOver() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 60, 80, 100), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 60, 80, 100), -1)
            }
        } catch (_: Throwable) {}
    }

    fun release() {
        try {
            soundPool?.release()
            soundPool = null
        } catch (_: Throwable) {}
    }
}
