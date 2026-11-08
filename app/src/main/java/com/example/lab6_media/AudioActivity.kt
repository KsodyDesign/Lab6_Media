package com.example.lab6_media

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AudioActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private lateinit var audioManager: AudioManager
    private lateinit var statusText: TextView
    private lateinit var volumeSeekBar: SeekBar
    private lateinit var tvVolumeLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio)

        statusText = findViewById(R.id.statusText)
        volumeSeekBar = findViewById(R.id.volumeSeekBar)
        tvVolumeLabel = findViewById(R.id.tvVolumeLabel)

        // Настройка AudioManager для медиапотока
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        volumeSeekBar.max = maxVolume
        volumeSeekBar.progress = currentVolume
        updateVolumeLabel(currentVolume, maxVolume)

        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0)
                    updateVolumeLabel(progress, maxVolume)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val playBtn = findViewById<Button>(R.id.btnPlayAudio)
        val pauseBtn = findViewById<Button>(R.id.btnPauseAudio)
        val stopBtn = findViewById<Button>(R.id.btnStopAudio)
        val backBtn = findViewById<Button>(R.id.btnBack)

        playBtn.setOnClickListener {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(this, R.raw.sample_audio)
                mediaPlayer?.setOnErrorListener { _, _, _ ->
                    statusText.text = "Ошибка воспроизведения"
                    statusText.setBackgroundColor(0xFFFFCDD2.toInt())
                    true
                }
                mediaPlayer?.setOnCompletionListener {
                    statusText.text = "Воспроизведение завершено"
                    statusText.setBackgroundColor(0xFFE8F5E9.toInt())
                }
            }

            mediaPlayer?.let { player ->
                if (!player.isPlaying) {
                    player.start()
                    statusText.text = "▶ Воспроизводится"
                    statusText.setBackgroundColor(0xFFE8F5E9.toInt())
                }
            }
        }

        pauseBtn.setOnClickListener {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                    statusText.text = "⏸ На паузе"
                    statusText.setBackgroundColor(0xFFFFF9C4.toInt())
                }
            }
        }

        stopBtn.setOnClickListener {
            mediaPlayer?.let { player ->
                player.stop()
                player.release()
            }
            mediaPlayer = null
            statusText.text = "⏹ Остановлено"
            statusText.setBackgroundColor(0xFFEEEEEE.toInt())
        }

        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun updateVolumeLabel(current: Int, max: Int) {
        val percent = if (max > 0) (current * 100) / max else 0
        tvVolumeLabel.text = "Громкость медиапотока: $percent% ($current / $max)"
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
