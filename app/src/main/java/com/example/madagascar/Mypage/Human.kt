package com.example.madagascar.Mypage

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.madagascar.Login
import com.example.madagascar.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

@Suppress("DEPRECATION")
class Human : AppCompatActivity() {

    private lateinit var tvName: TextView //textview 및 imageview 선언
    private lateinit var tvEmail: TextView
    private lateinit var ivProfile: ImageView
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_human)
        Log.d("DeleteAccount", "FirebaseAuth 초기화: ${auth != null}")
        Log.d("DeleteAccount", "FirebaseFirestore 초기화: ${db != null}")
        // UI 요소 초기화
        tvName = findViewById(R.id.tvName)
        tvEmail = findViewById(R.id.tvEmail)
        ivProfile = findViewById(R.id.ivProfile)
        val btnChangePassword = findViewById<Button>(R.id.btnChangePassword)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnDeleteAccount = findViewById<Button>(R.id.btnDeleteAccount)
        val arrowBtn101 = findViewById<ImageView>(R.id.btn_arrow101)

        //사용자 데이터 로드
        loadUserInfo()

        // 마이페이지 돌아가는 버튼
        arrowBtn101.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            startActivity(intent)
        }

        // 비밀번호 변경 버튼
        btnChangePassword.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }

        // 로그아웃 변경 버튼
        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, Login::class.java))
            finish()
        }

        // 계정삭제 버튼
        btnDeleteAccount.setOnClickListener {
            deleteAccount()
        }

        // 프로필 이미지 변경 가능 초기화
        initImageViewProfile()
    }

    //프로필 눌렀을 때 갤러리 접근 권한을 확인하고 권한 요청
    private fun initImageViewProfile() {
        ivProfile.setOnClickListener {
            when {
                // 권한이 이미 허용된 경우
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                    navigateGallery()
                }

                // Android 13 이상: READ_MEDIA_IMAGES 권한 요청 필요
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_MEDIA_IMAGES
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                    navigateGallery()
                }

                // 권한이 거부된 적이 있는 경우 설명 표시
                shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    showPermissionContextPopup()
                }

                // Android 13 이상: 새로운 권한 요청
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU -> {
                    requestPermissions(
                        arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                        1000
                    )
                }

                // Android 12 이하: 기존 권한 요청
                else -> {
                    requestPermissions(
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        1000
                    )
                }
            }
        }
    }


    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1000) {
            // 권한 요청 처리
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "권한이 허용되었습니다.", Toast.LENGTH_SHORT).show()
                navigateGallery()
            } else {
                Toast.makeText(this, "권한을 거부하셨습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun navigateGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 2000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            2000 -> {
                val selectedImageUri: Uri? = data?.data
                if (selectedImageUri != null) {
                    ivProfile.setImageURI(selectedImageUri)
                } else {
                    Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showPermissionContextPopup() {
        AlertDialog.Builder(this)
            .setTitle("권한이 필요합니다.")
            .setMessage("프로필 이미지를 변경하려면 갤러리 접근 권한이 필요합니다. 설정에서 권한을 활성화해주세요.")
            .setPositiveButton("설정으로 이동") { _, _ ->
                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
            .setNegativeButton("취소하기") { _, _ -> }
            .create()
            .show()
    }


    private fun loadUserInfo() {
        val user = auth.currentUser
        user?.let {
            tvEmail.text = user.email
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        tvName.text = document.getString("name") ?: "이름 없음"
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "사용자 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun deleteAccount() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "현재 로그인된 계정이 없습니다.", Toast.LENGTH_SHORT).show()
            Log.e("DeleteAccount", "현재 로그인된 계정이 없습니다.")
            return
        }

        val idInput = EditText(this)
        val passwordInput = EditText(this)
        passwordInput.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(idInput.apply { hint = "아이디를 입력하세요" })
            addView(passwordInput.apply { hint = "비밀번호를 입력하세요" })
        }

        AlertDialog.Builder(this)
            .setTitle("계정 삭제")
            .setMessage("아이디와 비밀번호를 입력하세요.")
            .setView(layout)
            .setPositiveButton("확인") { _, _ ->
                val inputId = idInput.text.toString()
                val inputPassword = passwordInput.text.toString()

                if (inputId.isBlank() || inputPassword.isBlank()) {
                    Toast.makeText(this, "아이디와 비밀번호를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                    Log.e("DeleteAccount", "아이디 또는 비밀번호가 비어 있습니다.")
                    return@setPositiveButton
                }

                Log.d("DeleteAccount", "입력된 아이디: $inputId, 비밀번호: $inputPassword")

                // Firestore에서 ID와 비밀번호 확인
                db.collection("users")
                    .whereEqualTo("id", inputId)
                    .whereEqualTo("password", inputPassword)
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            val document = documents.documents[0]
                            val uid = document.id // UID 가져오기
                            Log.d("DeleteAccount", "Firestore 인증 성공, UID: $uid")

                            // Firestore 데이터 삭제
                            db.collection("users").document(uid)
                                .delete()
                                .addOnSuccessListener {
                                    Log.d("DeleteAccount", "Firestore 데이터 삭제 성공")

                                    // Firebase Authentication 계정 삭제
                                    auth.currentUser?.delete()
                                        ?.addOnSuccessListener {
                                            Log.d("DeleteAccount", "Firebase 계정 삭제 성공")
                                            Toast.makeText(this, "계정 및 데이터가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                                            startActivity(Intent(this, Login::class.java))
                                            finish()
                                        }
                                        ?.addOnFailureListener { authError ->
                                            Log.e("DeleteAccount", "Firebase 계정 삭제 실패: ${authError.message}")
                                            Toast.makeText(this, "Firebase 계정 삭제 실패: ${authError.message}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                                .addOnFailureListener { firestoreError ->
                                    Log.e("DeleteAccount", "Firestore 데이터 삭제 실패: ${firestoreError.message}")
                                    Toast.makeText(this, "Firestore 데이터 삭제 실패: ${firestoreError.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(this, "아이디 또는 비밀번호가 잘못되었습니다.", Toast.LENGTH_SHORT).show()
                            Log.e("DeleteAccount", "Firestore에서 해당 아이디 또는 비밀번호를 찾을 수 없음.")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("DeleteAccount", "Firestore 조회 실패: ${e.message}")
                        Toast.makeText(this, "계정 인증 중 오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("취소") { dialog, _ ->
                Log.d("DeleteAccount", "계정 삭제 취소됨")
                dialog.dismiss()
            }
            .show()
    }




    // Firestore 데이터 삭제
    private fun deleteFirestoreData(uid: String, callback: (Boolean) -> Unit) {
        Log.d("DeleteAccount", "Firestore 데이터 삭제 시도: user.uid=$uid")
        db.collection("users").document(uid)
            .delete()
            .addOnSuccessListener {
                Log.d("DeleteAccount", "Firestore 데이터 삭제 성공")
                callback(true)
            }
            .addOnFailureListener { firestoreError ->
                Log.e("DeleteAccount", "Firestore 데이터 삭제 실패: ${firestoreError.message}")
                Toast.makeText(this, "Firestore 데이터 삭제 실패: ${firestoreError.message}", Toast.LENGTH_SHORT).show()
                callback(false)
            }
    }

    // Firebase Authentication 계정 삭제
    private fun deleteAuthAccount(user: FirebaseUser) {
        Log.d("DeleteAccount", "Firebase Authentication 계정 삭제 시도")
        user.delete()
            .addOnSuccessListener {
                Log.d("DeleteAccount", "Firebase 계정 삭제 성공")
                Toast.makeText(this, "계정 및 데이터가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, Login::class.java))
                finish()
            }
            .addOnFailureListener { authError ->
                Log.e("DeleteAccount", "Firebase 계정 삭제 실패: ${authError.message}")
                Toast.makeText(this, "Firebase 계정 삭제 실패: ${authError.message}", Toast.LENGTH_SHORT).show()
            }
    }






}
