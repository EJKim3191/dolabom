package com.dolabom.app.data

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/** 한 줄 음성 안내. */
interface Announcer {
    fun say(text: String)

    fun shutdown() {}
}

/**
 * Android 기본 음성 합성으로 읽어준다.
 * Pleos TextToSpeech SDK를 쓰게 되면 이 클래스만 바꾸면 된다.
 */
class TtsAnnouncer(context: Context) : Announcer, TextToSpeech.OnInitListener {

    private var ready = false
    private val pending = ArrayDeque<String>()
    private val tts = TextToSpeech(context.applicationContext, this)

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) return
        tts.setLanguage(Locale.KOREAN)
        ready = true
        while (pending.isNotEmpty()) speakNow(pending.removeFirst())
    }

    override fun say(text: String) {
        if (text.isBlank()) return
        if (ready) {
            speakNow(text)
        } else {
            // 엔진이 준비되기 전에는 최근 몇 문장만 기억해 둔다.
            pending.addLast(text)
            while (pending.size > MAX_PENDING) pending.removeFirst()
        }
    }

    private fun speakNow(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "dolabom-" + System.nanoTime())
    }

    override fun shutdown() {
        tts.stop()
        tts.shutdown()
    }

    private companion object {
        const val MAX_PENDING = 3
    }
}
