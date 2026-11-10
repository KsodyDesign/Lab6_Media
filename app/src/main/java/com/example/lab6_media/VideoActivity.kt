package com.example.lab6_media

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.MediaController
import android.widget.ProgressBar
import android.widget.VideoView
import com.google.android.material.snackbar.Snackbar

class VideoActivity : BaseActivity() {
    private lateinit var videoView: VideoView
    private lateinit var progressBar: ProgressBar
    private var prepared = false
    private var position = 0
    private var resumePlayback = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        videoView = findViewById(R.id.videoPlayer)
        progressBar = findViewById(R.id.progressBar)
        position = savedInstanceState?.getInt("position") ?: 0
        resumePlayback = savedInstanceState?.getBoolean("playing") ?: false
        val controller = MediaController(this)
        controller.setAnchorView(videoView)
        videoView.setMediaController(controller)
        videoView.setOnPreparedListener { player ->
            prepared = true
            progressBar.visibility = View.GONE
            if (position > 0) {
                player.setOnSeekCompleteListener {
                    player.setOnSeekCompleteListener(null)
                    if (resumePlayback) videoView.start()
                }
                videoView.seekTo(position)
            } else if (resumePlayback) videoView.start()
        }
        videoView.setOnCompletionListener {
            position = 0
            resumePlayback = false
        }
        videoView.setOnErrorListener { _, _, _ ->
            prepared = false
            resumePlayback = false
            progressBar.visibility = View.GONE
            Snackbar.make(videoView, "Не удалось открыть видеофайл. Нажмите Play для повтора.", Snackbar.LENGTH_LONG).show()
            true
        }
        findViewById<Button>(R.id.btnPlayVideo).setOnClickListener {
            resumePlayback = true
            if (prepared) videoView.start() else openVideo()
        }
        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
    }

    private fun openVideo() {
        prepared = false
        progressBar.visibility = View.VISIBLE
        try {
            videoView.setVideoURI(Uri.parse("android.resource://$packageName/${R.raw.sample_video}"))
        } catch (error: Exception) {
            resumePlayback = false
            progressBar.visibility = View.GONE
            Snackbar.make(videoView, "Не удалось открыть видеофайл", Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onStart() {
        super.onStart()
        openVideo()
    }

    override fun onResume() {
        super.onResume()
        if (prepared && resumePlayback) videoView.start()
    }

    override fun onPause() {
        if (prepared) {
            position = videoView.currentPosition
            resumePlayback = videoView.isPlaying
            videoView.pause()
        }
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("position", if (prepared) videoView.currentPosition else position)
        outState.putBoolean("playing", videoView.isPlaying || resumePlayback)
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        videoView.stopPlayback()
        prepared = false
        super.onStop()
    }

    override fun onDestroy() {
        videoView.stopPlayback()
        super.onDestroy()
    }
}
