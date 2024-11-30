package com.example.madagascar.Mypage

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.madagascar.Login
import com.example.madagascar.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

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
        val btnChangePassword = findViewById<TextView>(R.id.btnChangePassword)
        val btnLogout = findViewById<TextView>(R.id.btnLogout)
        val btnDeleteAccount = findViewById<TextView>(R.id.btnDeleteAccount)
        val arrowBtn101 = findViewById<ImageView>(R.id.btn_arrow101)

        // 사용자 데이터 로드
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

        // 로그아웃 버튼
        btnLogout.setOnClickListener {
            auth.signOut() // Firebase 로그아웃
            // 사용자 데이터 초기화
            tvName.text = ""
            tvEmail.text = ""
            // 로그인 화면으로 이동
            startActivity(Intent(this, Login::class.java))
            finish() // 현재 Activity 종료
        }

        // 계정 삭제 버튼
        btnDeleteAccount.setOnClickListener {
            deleteAccount()
        }

        // 프로필 이미지 변경 가능 초기화
        initImageViewProfile()
    }

    // 프로필 이미지 변경 시 갤러리 접근
    private fun initImageViewProfile() {
        ivProfile.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                    navigateGallery()
                }

                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                    navigateGallery()
                }

                shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    showPermissionContextPopup()
                }

                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU -> {
                    requestPermissions(arrayOf(Manifest.permission.READ_MEDIA_IMAGES), 1000)
                }

                else -> {
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1000)
                }
            }
        }
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1000) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "권한이 허용되었습니다.", Toast.LENGTH_SHORT).show()
                navigateGallery()
            } else {
                Toast.makeText(this, "권한을 거부하셨습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 갤러리로 이동
    private fun navigateGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 2000)
    }

    // 갤러리에서 선택한 이미지 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            2000 -> {
                val selectedImageUri: Uri? = data?.data
                if (selectedImageUri != null) {
                    // 이미지 미리보기 업데이트
                    ivProfile.setImageURI(selectedImageUri)

                    // Firebase Storage에 이미지 업로드
                    uploadImageToFirebaseStorage(selectedImageUri)
                } else {
                    Toast.makeText(this, "사진을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    // Firebase Storage에 이미지 업로드 및 Firestore에 URL 저장
    private fun uploadImageToFirebaseStorage(imageUri: Uri) {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "로그인된 사용자가 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val storageRef = FirebaseStorage.getInstance().reference.child("profileImages/${user.uid}.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                // 업로드 성공 시 다운로드 URL 가져오기
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveProfileImageUrlToFirestore(uri.toString())
                }.addOnFailureListener {
                    Toast.makeText(this, "프로필 이미지 URL을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "프로필 이미지 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }

    // Firestore에 프로필 이미지 URL 저장
    private fun saveProfileImageUrlToFirestore(imageUrl: String) {
        val user = auth.currentUser
        user?.let {
            db.collection("users").document(user.uid)
                .update("profileImage", imageUrl)
                .addOnSuccessListener {
                    Log.d("Profile", "프로필 이미지 URL Firestore에 저장됨")
                    Toast.makeText(this, "프로필 이미지가 저장되었습니다.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "프로필 이미지 URL 저장 실패: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadUserInfo() {
        val user = auth.currentUser
        user?.let {
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val username = document.getString("username") ?: "이름 없음"
                        val email = user.email ?: "이메일 없음" // Firestore에서 이메일 정보 가져오기
                        val profileImage = document.getString("profileImage") // Firestore에 저장된 프로필 이미지 URL

                        // TextView에 사용자 정보 설정
                        tvName.text = username
                        tvEmail.text = email

                        // Glide를 사용하여 프로필 이미지 로드
                        if (!profileImage.isNullOrEmpty()) {
                            Glide.with(this).load(profileImage).into(ivProfile)
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "사용자 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show()
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



    private fun deleteAccount() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "현재 로그인된 계정이 없습니다.", Toast.LENGTH_SHORT).show()
            Log.e("DeleteAccount", "현재 로그인된 계정이 없습니다.")
            return
        }

        val passwordInput = EditText(this)
        passwordInput.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(passwordInput.apply { hint = "비밀번호를 입력하세요" })
        }

        AlertDialog.Builder(this)
            .setTitle("계정 삭제")
            .setMessage("비밀번호를 입력하세요.")
            .setView(layout)
            .setPositiveButton("확인") { _, _ ->
                val inputPassword = passwordInput.text.toString()

                if (inputPassword.isBlank()) {
                    Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                    Log.e("DeleteAccount", "비밀번호가 비어 있습니다.")
                    return@setPositiveButton
                }

                Log.d("DeleteAccount", "입력된 비밀번호: $inputPassword")

                // FirebaseAuth 비밀번호 재인증
                val userEmail = user.email
                if (userEmail != null) {
                    val credential = EmailAuthProvider.getCredential(userEmail, inputPassword)
                    user.reauthenticate(credential)
                        .addOnSuccessListener {
                            Log.d("DeleteAccount", "재인증 성공")
                            // Firestore 및 Firebase Authentication에서 계정 삭제
                            deleteUserAccount(user)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "비밀번호가 잘못되었습니다.", Toast.LENGTH_SHORT).show()
                            Log.e("DeleteAccount", "재인증 실패: ${e.message}")
                        }
                } else {
                    Toast.makeText(this, "사용자 이메일을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    Log.e("DeleteAccount", "사용자 이메일을 찾을 수 없습니다.")
                }
            }
            .setNegativeButton("취소") { dialog, _ ->
                Log.d("DeleteAccount", "계정 삭제 취소됨")
                dialog.dismiss()
            }
            .show()
    }

    // Firestore 문서 및 Firebase Authentication 계정 삭제
    private fun deleteUserAccount(user: FirebaseUser) {
        val userId = user.uid
        db.collection("users").document(userId)
            .delete()
            .addOnSuccessListener {
                Log.d("DeleteAccount", "Firestore에서 사용자 데이터 삭제 완료")
                user.delete()
                    .addOnSuccessListener {
                        Log.d("DeleteAccount", "Firebase 계정 삭제 성공")
                        Toast.makeText(this, "계정 및 데이터가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, Login::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Log.e("DeleteAccount", "Firebase 계정 삭제 실패: ${e.message}")
                        Toast.makeText(this, "Firebase 계정 삭제 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Log.e("DeleteAccount", "Firestore 사용자 데이터 삭제 실패: ${e.message}")
                Toast.makeText(this, "계정 삭제 중 문제가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
