package com.globalcampus.support.util

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import java.util.*

class TextToSpeechManager(context: Context) {
    private var tts: TextToSpeech? = null
    private var isReady = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.ENGLISH
                isReady = true
            }
        }
    }

    fun speak(text: String, languageCode: String = "en") {
        if (!isReady || tts == null) return
        val locale = when (languageCode.lowercase()) {
            "hi", "hindi" -> Locale("hi", "IN")
            "pa", "punjabi" -> Locale("pa", "IN")
            else -> Locale.ENGLISH
        }
        val res = tts?.setLanguage(locale)
        if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts?.setLanguage(Locale("hi", "IN"))
        }
        tts?.speak(text.take(300), TextToSpeech.QUEUE_FLUSH, null, "LPU_AI_SPEECH")
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}

fun createSpeechToTextIntent(languageCode: String = "en"): Intent {
    val locale = when (languageCode.lowercase()) {
        "hi", "hindi" -> "hi-IN"
        "pa", "punjabi" -> "pa-IN"
        else -> "en-IN"
    }
    return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale)
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now to LPU Campus AI Assistant...")
    }
}
