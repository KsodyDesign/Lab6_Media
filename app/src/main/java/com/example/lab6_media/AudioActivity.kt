package com.example.lab6_media

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar

class AudioActivity : BaseActivity() {
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var statusText: TextView
    private var position = 0
    private var state = "stopped"
    private var resumePlayback = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio)
        statusText = findViewById(R.id.statusText)
        position = savedInstanceState?.getInt("position") ?: 0
        state = savedInstanceState?.getString("state") ?: "stopped"
        resumePlayback = savedInstanceState?.getBoolean("playing") ?: false
        volumeControlStream = AudioManager.STREAM_MUSIC
        val audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val slider = findViewById<SeekBar>(R.id.volumeSeekBar)
        val label = findViewById<TextView>(R.id.tvVolumeLabel)
        slider.max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        slider.progress = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
        fun showVolume() {
            val percent = if (slider.max > 0) slider.progress * 100 / slider.max else 0
            label.text = "Громкость медиапотока: $percent% (${slider.progress} / ${slider.max})"
        }
        showVolume()
        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar?, value: Int, fromUser: Boolean) {
                if (fromUser) {
                    try {
                        audio.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0)
                    } catch (error: SecurityException) {
                        Snackbar.make(statusText, "Изменение громкости запрещено системой", Snackbar.LENGTH_LONG).show()
                    }
                    slider.progress = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
                    showVolume()
                }
            }
            override fun onStartTrackingTouch(bar: SeekBar?) {}
            override fun onStopTrackingTouch(bar: SeekBar?) {}
        })
        findViewById<Button>(R.id.btnPlayAudio).setOnClickListener { play() }
        findViewById<Button>(R.id.btnPauseAudio).setOnClickListener {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                state = "paused"
                renderState()
            }
        }
        findViewById<Button>(R.id.btnStopAudio).setOnClickListener {
            releasePlayer()
            position = 0
            resumePlayback = false
            state = "stopped"
            renderState()
        }
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
        renderState()
    }

    private fun preparePlayer(): MediaPlayer? {
        mediaPlayer?.let { return it }
        return try {
            val player = MediaPlayer.create(this, R.raw.sample_audio)
                ?: throw IllegalStateException("Не удалось открыть аудиофайл")
            mediaPlayer = player
            player.setOnErrorListener { _, _, _ ->
                showError()
                true
            }
            player.setOnCompletionListener {
                position = 0
                state = "completed"
                renderState()
            }
            player.seekTo(position)
            player
        } catch (error: Exception) {
            showError()
            null
        }
    }

    private fun play() {
        try {
            val player = preparePlayer() ?: return
            player.start()
            state = "playing"
            renderState()
        } catch (error: IllegalStateException) {
            showError()
        }
    }

    private fun showError() {
        releasePlayer()
        position = 0
        resumePlayback = false
        state = "error"
        renderState()
        Snackbar.make(statusText, "Не удалось воспроизвести аудиофайл", Snackbar.LENGTH_LONG).show()
    }

    private fun renderState() {
        statusText.text = when (state) {
            "playing" -> "▶ Воспроизводится"
            "paused" -> "⏸ На паузе"
            "completed" -> "Воспроизведение завершено"
            "error" -> "Ошибка воспроизведения"
            else -> "⏹ Остановлено"
        }
    }

    private fun releasePlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onResume() {
        super.onResume()
        if (resumePlayback) {
            resumePlayback = false
            play()
        }
    }

    override fun onPause() {
        resumePlayback = mediaPlayer?.isPlaying == true
        mediaPlayer?.let {
            position = it.currentPosition
            if (it.isPlaying) it.pause()
        }
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("position", mediaPlayer?.currentPosition ?: position)
        outState.putString("state", state)
        outState.putBoolean("playing", mediaPlayer?.isPlaying == true || resumePlayback)
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        releasePlayer()
        super.onStop()
    }

    override fun onDestroy() {
        releasePlayer()
        super.onDestroy()
    }
}
