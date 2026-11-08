package com.example.lab6_media

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.MediaController
import android.widget.ProgressBar
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

class VideoActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView
    private lateinit var progressBar: ProgressBar
    private var isPrepared = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)

        videoView = findViewById(R.id.videoPlayer)
        progressBar = findViewById(R.id.progressBar)
        val btnPlay = findViewById<Button>(R.id.btnPlayVideo)
        val btnBack = findViewById<Button>(R.id.btnBack)

        val uri = Uri.parse("android.resource://$packageName/${R.raw.sample_video}")
        videoView.setVideoURI(uri)

        // Стандартный контроллер управления видео
        val controller = MediaController(this)
        controller.setAnchorView(videoView)
        videoView.setMediaController(controller)

        videoView.setOnPreparedListener {
            isPrepared = true
            progressBar.visibility = View.GONE
        }

        videoView.setOnErrorListener { _, what, extra ->
            progressBar.visibility = View.GONE
            Snackbar.make(videoView, "Ошибка воспроизведения видео: $what, $extra", Snackbar.LENGTH_LONG).show()
            true
        }

        btnPlay.setOnClickListener {
            if (!isPrepared) {
                Snackbar.make(videoView, "Файл ещё загружается...", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            videoView.start()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        if (videoView.isPlaying) {
            videoView.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        videoView.stopPlayback()
    }
}
