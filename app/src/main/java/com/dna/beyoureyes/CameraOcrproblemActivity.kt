package com.dna.beyoureyes


import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.core.content.ContextCompat
import com.dna.beyoureyes.databinding.ActivityCameraOcrproblemBinding
import org.opencv.android.OpenCVLoader

class CameraOcrproblemActivity : BaseActivity() {

    private lateinit var binding: ActivityCameraOcrproblemBinding
    private val camera = Camera()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCameraOcrproblemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 툴바
        setSupportActionBar(binding.include.toolbarDefault)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.include.toolbarTitle.text = "다시 촬영해주세요."

        binding.include.toolbarBackBtn.setOnClickListener {
            goToHome() // BaseActivity에서 정의한 홈화면 이동 함수(화면전환효과적용)
        }

        // 카메라
        binding.buttoncamera.setOnClickListener {
            camera.start(this)
        }

        //openCV
        OpenCVLoader.initDebug()

        // Kotlin 코드에서 해당 TextView를 찾아서 SpannableString을 사용하여 스타일을 적용
        val spannable = SpannableString("⚠ 글자 인식에 실패했습니다.")
        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.red)),
            0,
            1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.textView1.text = spannable
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            when(requestCode) {
                Camera.FLAG_REQ_CAMERA -> {
                    camera.processPhoto(this)
                }
            }
        }
    }

}