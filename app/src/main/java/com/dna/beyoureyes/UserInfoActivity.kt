package com.dna.beyoureyes

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
//import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import com.dna.beyoureyes.databinding.ActivityAlertDialogDefaultBinding
import com.dna.beyoureyes.databinding.ActivityUserInfoBinding
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.Serializable

//val diseaseKoreanList : List<String> = listOf("고혈압", "고지혈증", "당뇨")
//val allergyKoreanList : List<String> = listOf("메밀", "밀", "콩", "호두", "땅콩", "복숭아", "토마토", "돼지고기", "난류", "우유", "닭고기", "쇠고기", "새우", "고등어", "홍합", "전복", "굴", "조개류", "게", "오징어", "아황산")

class UserInfoActivity : BaseActivity() {
    //private val userDiseaseList : ArrayList<String> = arrayListOf()
    //private val userAllergyList : ArrayList<String> = arrayListOf()

    //google login을 위한 동작
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var oneTapClient: SignInClient

    private lateinit var auth: FirebaseAuth
    //private lateinit var launcher: ActivityResultLauncher<Intent>
    private lateinit var signInRequest: BeginSignInRequest

    //private val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity
    //private var showOneTapUI = true
    private lateinit var binding: ActivityUserInfoBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Firebase 연결
        Log.d(TAG, AppUser.id.toString()+"   AGAIN")
        auth = Firebase.auth
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        oneTapClient = Identity.getSignInClient(this)

        // 뷰 바인딩
        val diseaseChipGroup = binding.diseaseChipGroup
        val allergicChipGroup  = binding.allergyChipGroup
        var sex : Int = 2

        val infoAge = binding.infoAge
        val infoSex = binding.infoSex

        val userInfoChangeButton = binding.userInfoChangeButton
        val googleConnectButton = binding.googleConnectButton

        // 툴바
        setSupportActionBar(binding.include.toolbarDefault)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.include.toolbarTitle.text = "내 질환 확인하기"

        binding.include.toolbarBackBtn.setOnClickListener {
            goToHome() // BaseActivity에서 정의한 홈화면 이동 함수(화면전환효과적용)
        }

        // 사용자 정보 화면 표시 ---------------------------------------------
        AppUser.info?.let {
            Log.d("USERINFO : ", "${AppUser.id}")
            // 나이 정보 표시
            infoAge.text = it.age.toString() + "세"
            // 성별 정보 표시
            when(it.gender) {
                0 -> infoSex.setText("여성")
                1 -> infoSex.setText("남성")
                2 -> infoSex.setText("정보가 없습니다. 추가해주세요!")
            }
            // 질환 정보 표시
            if (it.hasDisease()){
                for (diseaseItem in it.disease) {
                    val chip = Chip(this)
                    chip.text = diseaseItem
                    // Chip 뷰의 크기 및 여백 설정
                    // 원하는 폰트 파일을 res/font 디렉토리에 추가한 후 R.font.custom_font로 참조
                    val customTypeface = ResourcesCompat.getFont(this, R.font.pretendard600)

                    // 폰트 설정
                    chip.typeface = customTypeface
                    val params = ChipGroup.LayoutParams(
                        ChipGroup.LayoutParams.WRAP_CONTENT,
                        ChipGroup.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(8, 8, 8, 8) // 여백을 8로
                    chip.layoutParams = params
                    // 글씨 크기
                    chip.textSize = 24f
                    // 가운데 정렬
                    chip.textAlignment = View.TEXT_ALIGNMENT_CENTER
                    chip.setChipBackgroundColorResource(R.color.red)
                    chip.setTextColor(Color.WHITE)
                    diseaseChipGroup.addView(chip)
                }
            }else{
                val chip = Chip(this)
                chip.text = "없음"
                // Chip 뷰의 크기 및 여백 설정
                // 원하는 폰트 파일을 res/font 디렉토리에 추가한 후 R.font.custom_font로 참조
                val customTypeface = ResourcesCompat.getFont(this, R.font.pretendard600)

                // 폰트 설정
                chip.typeface = customTypeface
                val params = ChipGroup.LayoutParams(
                        ChipGroup.LayoutParams.WRAP_CONTENT,
                        ChipGroup.LayoutParams.WRAP_CONTENT
                    )
                params.setMargins(8, 8, 8, 8) // 여백을 8로
                chip.layoutParams = params
                // 글씨 크기
                chip.textSize = 24f
                // 가운데 정렬
                chip.textAlignment = View.TEXT_ALIGNMENT_CENTER
                chip.setChipBackgroundColorResource(R.color.grey)
                chip.setTextColor(Color.WHITE)
                diseaseChipGroup.addView(chip)
            }

            // 알러지 정보 표시
            if ( it.hasAllergy()){
                for (allergyItem in it.allergic ) {
                    val chip = Chip(this)
                    chip.text = allergyItem
                    // 원하는 폰트 파일을 res/font 디렉토리에 추가한 후 R.font.custom_font로 참조
                    val customTypeface = ResourcesCompat.getFont(this, R.font.pretendard600)

                    // 폰트 설정
                    chip.typeface = customTypeface
                    // Chip 뷰의 크기 및 여백 설정
                    val params = ChipGroup.LayoutParams(
                        ChipGroup.LayoutParams.WRAP_CONTENT,
                        ChipGroup.LayoutParams.WRAP_CONTENT
                    )
                    params.setMargins(8, 8, 8, 8) // 여백을 8로
                    chip.layoutParams = params
                    // 글씨 크기
                    chip.textSize = 24f
                    // 가운데 정렬
                    chip.textAlignment = View.TEXT_ALIGNMENT_CENTER
                    chip.setChipBackgroundColorResource(R.color.red)
                    chip.setTextColor(Color.WHITE)
                    allergicChipGroup.addView(chip)
                }
            }else{
                val chip = Chip(this)
                chip.text = "없음"
                // Chip 뷰의 크기 및 여백 설정
                // 원하는 폰트 파일을 res/font 디렉토리에 추가한 후 R.font.custom_font로 참조
                val customTypeface = ResourcesCompat.getFont(this, R.font.pretendard600)

                // 폰트 설정
                chip.typeface = customTypeface
                val params = ChipGroup.LayoutParams(
                    ChipGroup.LayoutParams.WRAP_CONTENT,
                    ChipGroup.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(8, 8, 8, 8) // 여백을 8로
                chip.layoutParams = params
                // 글씨 크기
                chip.textSize = 24f
                // 가운데 정렬
                chip.textAlignment = View.TEXT_ALIGNMENT_CENTER
                chip.setChipBackgroundColorResource(R.color.grey)
                chip.setTextColor(Color.WHITE)
                allergicChipGroup.addView(chip)
            }
        }?:run{ // 사용자 정보 null일 시 -> 처리 조건 상 이 분기는 아마 진입할 일이 없긴 할 것
            // 나이 정보 표시
            infoAge.text = "-"
            // 성별 정보 표시
            infoSex.text = "-"
        }


        // 수정하기 버튼 클릭 시 작용
        userInfoChangeButton.setOnClickListener {
            val intent = Intent(this, UserInfoRegisterActivity::class.java)
            goToNext(intent)
        }
        if(auth.currentUser!!.isEmailVerified) {
            googleConnectButton.isVisible = false
            binding.googleLogoutButton.isVisible = true
        }
        else {
            googleConnectButton.isVisible = true
            binding.googleLogoutButton.isVisible = false
        }
        // 로그아웃 버튼 클릭 시
        binding.googleLogoutButton.setOnClickListener{
            val dialogView =
                LayoutInflater.from(this).inflate(R.layout.activity_alert_dialog_login, null)

            val alertDialogBuilder = AlertDialog.Builder(this)
            val alertDialogBinding = ActivityAlertDialogDefaultBinding.inflate(layoutInflater)
            alertDialogBuilder.setView(alertDialogBinding.root)

            val alertDialog = alertDialogBuilder.create()

            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            alertDialogBinding.rightBtn.setOnClickListener {
                //Firebase.auth.signOut()
                //moveTaskToBack(true)  // finish 후 다른 Activity 뜨지 않도록 함
                //finish()  // 현재 액티비티 종료
                //finishAffinity()  // 모든 루트 액티비티 종료
                //overridePendingTransition(0, 0)   // 인텐트 애니메이션 종료
            }

            alertDialogBinding.leftBtn.setOnClickListener {
                alertDialog.dismiss()
            }

            alertDialogBinding.title.text = "로그아웃"
            alertDialogBinding.text.text = "로그아웃하시겠어요?\n로그아웃하시면 어플리케이션이 종료됩니다."

            //alertDialog.show()
        }
        // Google 로그인 버튼 클릭 이벤트 처리
        googleConnectButton.setOnClickListener {
            // 로그인 요청
            Log.d("GOOGLE : ", "이벤트 시작")
            signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(
                    BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(getString(R.string.your_web_client_id))
                        // Only show accounts previously used to sign in.
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .build()
            Log.d("GOOGLE : ", "이벤트 수행중?")
            signIn()
        }

    } // onCreate
    // [START on_start_check_user]

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        Log.d("GOOGLE : ", "onStart")
        updateUI(currentUser)
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
            }
        } else {
            Log.d(TAG, "onActivityResult: resultCode = $resultCode")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    Log.d("USERINFO UPDATE : ", "${AppUser.id} => ${AppUser.info!!.age}")
                    val user = auth.currentUser
                    updateUI(user)
                    Log.d("USERINFO UPDATE : ", "${AppUser.id} => ${user!!.uid}")

                    // 안드로이드 파이어베이스에서 google 연동 정보 있는 지 확인함
                    val db = Firebase.firestore
                    // google 계정 uid가 포함된 데이터가 있는 지 확인, 있으면 그거 그대로 불러오고, 없으면 기존 싱긅톤 객체에 있던 데이터를 send함
                    db.collection("userInfo")
                        .whereEqualTo("userID", user.uid)
                        .get()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val result = task.result
                                // 유저 정보가 firebase에 존재하는 경우
                                if (result != null && !result.isEmpty) {
                                    for (document in result) {
                                        Log.d("FIRESTORE : ", "${document.id} => ${document.data}")

                                        // Firebase 문서에서 사용자 정보 파싱하여 UserInfo 객체 생성
                                        val user = UserInfo.parseFirebaseDoc(document)

                                        AppUser.id = auth.currentUser!!.uid
                                        AppUser.info?.age = user!!.age
                                        AppUser.info?.gender = user!!.gender
                                        AppUser.info = user
                                        val intent = intent
                                        finish()
                                        startActivity(intent)

                                    }
                                } // 만약 유저 정보가 firebase에 존재하지 않는 경우
                                else{
                                    // 싱글톤 객체 유저 정보 업뎃
                                    if (user != null) {
                                        AppUser.id = user.uid
                                        Log.d("USERINFO UPDATE2 : ", "${AppUser.id} => ${AppUser.info!!.age}")
                                        val userInfo = hashMapOf(
                                            "userID" to AppUser.id!!,
                                            "userAge" to AppUser.info!!.age,
                                            "userSex" to AppUser.info!!.gender,
                                            "userDisease" to AppUser.info?.disease.let { ArrayList(it) },
                                            "userAllergic" to AppUser.info?.allergic.let { ArrayList(it) }
                                        )
                                        sendData(userInfo, "userInfo")
                                        val intent = intent
                                        finish()
                                        startActivity(intent)
                                    }
                                }
                            } else {
                                // 쿼리 중에 예외가 발생한 경우
                                // 쿼리 실패의 경우 인터넷 연결 상태와도 연관이 있으므로
                                // 추후 대응 필요성을 고려해 else문 분기 유지
                                Log.d("HOMEFIRESTORE : ", "Error getting documents.", task.exception)
                            }
                        }


                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }//if(task.isSuccessful)

                Log.d("USERINFO UPDATE2 : ", "${AppUser.id} => ${AppUser.info!!.age}")

            }
    }

    fun updateUI(user: FirebaseUser?) {

    }
    companion object {
        private const val TAG = "GOOGLE : "
        private const val RC_SIGN_IN = 9001
    }

    private fun sendData(userInfo : HashMap<String, Serializable>, collectionName : String){
        val db = Firebase.firestore
        db.collection(collectionName)
            .add(userInfo)
            .addOnSuccessListener { documentReference ->
                Log.d("REGISTERFIRESTORE :", "SUCCESS added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("REGISTERFIRESTORE :", "Error adding document", e)
            }
    }
    private fun deleteData(userId: String, collectionName: String, onSuccess: () -> Unit) {
        val firestore = Firebase.firestore
        firestore.collection(collectionName)
            .whereEqualTo("userID", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    // 찾은 문서를 삭제
                    firestore.collection(collectionName)
                        .document(document.id)
                        .delete()
                        .addOnCompleteListener {
                            Log.d("REGISTERFIRESTORE : ", "DELETE SUCCESS")
                            // 삭제 완료 시 onSuccess 호출
                            onSuccess()
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.d("REGISTERFIRESTORE : ", "Error deleting documents.", exception)
            }
    }


}