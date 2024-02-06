package com.dna.beyoureyes

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

// 모든 화면에 공통 적용되는 사항을 정의한 base activity
abstract class BaseActivity : AppCompatActivity() {

    /*
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

     */

    fun goToHome() {
        super.finish()
        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        overridePendingTransition(R.anim.horizon_exit, R.anim.horizon_exit_to)
    }

    fun goToNext(intent : Intent) { //다음 페이지로 넘어가기
        startActivity(intent)
        overridePendingTransition(R.anim.horizon_enter, R.anim.horizon_enter_to)
    }

    fun goToBack(intent : Intent) { //이전 페이지로 돌아가기(이전 페이지 갱신 필요)
        startActivity(intent)
        overridePendingTransition(R.anim.horizon_exit, R.anim.horizon_exit_to)
    }

    fun goToBack() { // 이전 페이지로 돌아가기
        super.finish()
        overridePendingTransition(R.anim.horizon_exit, R.anim.horizon_exit_to)
    }

    override fun onBackPressed() {
        goToHome()
    }
}