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
import com.bumptech.glide.Glide
import com.example.madagascar.Login
import com.example.madagascar.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

@Suppress("DEPRECATION")
class Human : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var ivProfile: ImageView
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_human)

        tvName = findViewById(R.id.tvName)
        tvEmail = findViewById(R.id.tvEmail)
        ivProfile = findViewById(R.id.ivProfile)
        val btnChangePassword = findViewById<TextView>(R.id.btnChangePassword)
        val btnLogout = findViewById<TextView>(R.id.btnLogout)
        val btnDeleteAccount = findViewById<TextView>(R.id.btnDeleteAccount)
        val arrowBtn101 = findViewById<ImageView>(R.id.btn_arrow101)

        loadUserInfo()

        // 마이페이지로 돌아가는 버튼
        arrowBtn101.setOnClickListener {
            startActivity(Intent(this, MypageActivity::class.java))
        }

        btnChangePassword.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            tvName.text = ""
            tvEmail.text = ""
            startActivity(Intent(this, Login::class.java))
            finish()
        }

        btnDeleteAccount.setOnClickListener {
            deleteAccount()
        }

        initImageViewProfile()
    }

    private fun initImageViewProfile() {
        ivProfile.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                    navigateGallery()
                }
                else -> {
                    requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1000)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1000 && grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            navigateGallery()
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

        if (requestCode == 2000) {
            selectedImageUri = data?.data
            selectedImageUri?.let {
                ivProfile.setImageURI(it)
                uploadImageToFirebase(it)
            }
        }
    }

    private fun uploadImageToFirebase(uri: Uri) {
        val user = auth.currentUser
        user?.let {
            val storageRef = storage.reference.child("profileImages/${user.uid}.jpg")
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        saveProfileImageUrlToFirestore(downloadUri.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveProfileImageUrlToFirestore(imageUrl: String) {
        val user = auth.currentUser
        user?.let {
            db.collection("users").document(user.uid)
                .update("profileImage", imageUrl)
                .addOnSuccessListener {
                    Log.d("Profile", "프로필 이미지 URL 저장 성공")
                }
                .addOnFailureListener {
                    Toast.makeText(this, "프로필 이미지 URL 저장 실패", Toast.LENGTH_SHORT).show()
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
                        val phoneNumber = document.getString("phoneNumber") ?: "번호 없음"
                        val profileImage = document.getString("profileImage")

                        tvName.text = username
                        tvEmail.text = phoneNumber

                        // Glide를 사용해 프로필 이미지 로드
                        if (profileImage != null) {
                            Glide.with(this).load(profileImage).into(ivProfile)
                        } else {
                            ivProfile.setImageResource(R.drawable.default_profile)
                        }
                    }
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

                            // Firestore 문서와 서브컬렉션 삭제
                            deleteDocumentWithSubcollections("users/$uid") { isDeleted ->
                                if (isDeleted) {
                                    // Firebase Authentication 계정 삭제
                                    deleteAuthAccount(user)
                                } else {
                                    Toast.makeText(this, "계정 삭제 중 문제가 발생했습니다.", Toast.LENGTH_SHORT).show()
                                }
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

    // 문서 및 서브컬렉션 삭제 함수
    private fun deleteDocumentWithSubcollections(documentPath: String, callback: (Boolean) -> Unit) {
        val docRef = db.document(documentPath)

        // 서브컬렉션 조회 및 삭제
        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // 서브컬렉션 목록 가져오기
                    docRef.collection("interests").get()
                        .addOnSuccessListener { subcollections ->
                            // 서브컬렉션 내 모든 문서 삭제
                            for (subDoc in subcollections.documents) {
                                subDoc.reference.delete()
                            }

                            // 부모 문서 삭제
                            docRef.delete()
                                .addOnSuccessListener {
                                    Log.d("DeleteAccount", "문서 및 서브컬렉션 삭제 성공")
                                    callback(true)
                                }
                                .addOnFailureListener { e ->
                                    Log.e("DeleteAccount", "문서 삭제 실패: ${e.message}")
                                    callback(false)
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e("DeleteAccount", "서브컬렉션 조회 실패: ${e.message}")
                            callback(false)
                        }
                } else {
                    Log.e("DeleteAccount", "삭제하려는 문서가 존재하지 않음")
                    callback(false)
                }
            }
            .addOnFailureListener { e ->
                Log.e("DeleteAccount", "문서 조회 실패: ${e.message}")
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
