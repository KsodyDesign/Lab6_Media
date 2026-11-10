package com.example.lab6_media

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.widget.EditText
import android.widget.TextView
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import java.io.File
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

@RunWith(AndroidJUnit4::class)
class LabRegressionTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private lateinit var activity: Activity
    private fun main(action: () -> Unit) = instrumentation.runOnMainSync(action)
    private fun launch(type: Class<out Activity>) {
        activity = instrumentation.startActivitySync(Intent(context, type).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        main { activity.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
        Thread.sleep(1200)
        instrumentation.waitForIdleSync()
        current()
    }
    private fun enter(id: Int, text: String) = main { activity.findViewById<EditText>(id).setText(text) }
    private fun click(id: Int) {
        Thread.sleep(2400)
        main { activity.findViewById<android.view.View>(id).performClick() }
        instrumentation.waitForIdleSync()
        Thread.sleep(350)
    }
    private fun text(id: Int): String {
        var result = ""
        main { result = activity.findViewById<TextView>(id).text.toString() }
        return result
    }
    private fun current() {
        main { activity = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).first() }
    }
    private fun rotate() {
        val old = activity
        main { activity.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
        Thread.sleep(1800)
        instrumentation.waitForIdleSync()
        current()
        assertNotSame("Activity must recreate on rotation", old, activity)
    }
    private fun screenshot(name: String) {
        instrumentation.waitForIdleSync()
        Thread.sleep(1000)
        val file = File(context.getExternalFilesDir(null), "audit/$name.png")
        file.parentFile!!.mkdirs()
        instrumentation.uiAutomation.takeScreenshot().let { bitmap ->
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }
    private fun finish() {
        main { activity.finish() }
        instrumentation.waitForIdleSync()
    }
    private fun field(name: String): Any? {
        val field = activity.javaClass.getDeclaredField(name)
        field.isAccessible = true
        return field.get(activity)
    }

    @Test fun audioVideoLifecycle() {
        launch(AudioActivity::class.java)
        click(R.id.btnPlayAudio)
        assertTrue((field("mediaPlayer") as MediaPlayer).isPlaying)
        screenshot("01-audio-play")
        click(R.id.btnPauseAudio)
        assertFalse((field("mediaPlayer") as MediaPlayer).isPlaying)
        val paused = (field("mediaPlayer") as MediaPlayer).currentPosition
        screenshot("02-audio-pause")
        rotate()
        assertTrue(text(R.id.statusText).contains("паузе"))
        assertEquals(paused, field("position"))
        click(R.id.btnPlayAudio)
        assertTrue((field("mediaPlayer") as MediaPlayer).isPlaying)
        screenshot("03-audio-rotated")
        val oldPlayer = field("mediaPlayer") as MediaPlayer
        main { activity.recreate() }
        Thread.sleep(1600)
        current()
        assertTrue((field("mediaPlayer") as MediaPlayer).isPlaying)
        assertNotSame(oldPlayer, field("mediaPlayer"))
        assertTrue(runCatching { oldPlayer.isPlaying }.isFailure)
        click(R.id.btnStopAudio)
        assertNull(field("mediaPlayer"))
        assertEquals(0, field("position"))
        screenshot("04-audio-stop")
        finish()
        launch(VideoActivity::class.java)
        Thread.sleep(1500)
        click(R.id.btnPlayVideo)
        Thread.sleep(800)
        assertTrue(activity.findViewById<android.widget.VideoView>(R.id.videoPlayer).isPlaying)
        screenshot("05-video-play")
        rotate()
        Thread.sleep(1000)
        assertTrue(activity.findViewById<android.widget.VideoView>(R.id.videoPlayer).isPlaying)
        screenshot("06-video-rotated")
        var pausedPosition = 0
        main {
            val video = activity.findViewById<android.widget.VideoView>(R.id.videoPlayer)
            video.pause()
            pausedPosition = video.currentPosition
            activity.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        Thread.sleep(2000)
        current()
        val pausedVideo = activity.findViewById<android.widget.VideoView>(R.id.videoPlayer)
        assertFalse(pausedVideo.isPlaying)
        assertTrue(kotlin.math.abs(pausedVideo.currentPosition - pausedPosition) < 1500)
        screenshot("07-video-paused-restored")
        finish()
    }
}
