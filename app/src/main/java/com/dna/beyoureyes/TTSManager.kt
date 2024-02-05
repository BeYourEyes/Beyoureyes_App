import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.widget.Toast
import java.util.Locale

class TTSManager(private val context: Context, private val onInitCallback: () -> Unit) :
    TextToSpeech.OnInitListener,  UtteranceProgressListener() {

    private var isSpeaking: Boolean = false
    private var textToSpeech: TextToSpeech? = null

    init {
        textToSpeech = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale.KOREAN)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTSManager", "Language is not supported or missing data")
            } else {
                // TTS 초기화 성공
                Log.d("TTSManager", "TextToSpeech initialization successful")
                onInitCallback.invoke()
            }
        } else {
            Log.e("TTSManager", "TextToSpeech initialization failed")
        }
    }

    override fun onStart(p0: String?) {
        //TODO("Not yet implemented")
    }

    override fun onDone(utteranceId: String?) {
        isSpeaking = false
    }

    override fun onError(p0: String?) {
        //TODO("Not yet implemented")
    }

    fun isSpeaking(): Boolean {
        return isSpeaking
    }

    fun speak(text: String) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            val params = Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "")
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "UniqueID")
        } else {
            // LOLLIPOP 이하의 버전에서는 UtteranceId를 지원하지 않음
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null)
        }

        isSpeaking = true
    }


    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun stop() {
        textToSpeech?.let {
            if (it.isSpeaking) {
                it.stop()
            }
            isSpeaking = false
        }
    }

    fun shutdown() {
        textToSpeech?.let {
            if (it.isSpeaking) {
                it.stop()
            }
            it.shutdown()
        }
    }
}
