package com.example.madagascar.Mypage

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.madagascar.Login
import com.example.madagascar.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Human : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var ivProfile: ImageView
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_human)

        // Initialize UI elements
        tvName = findViewById(R.id.tvName)
        tvEmail = findViewById(R.id.tvEmail)
        ivProfile = findViewById(R.id.ivProfile)
        val btnChangePassword = findViewById<Button>(R.id.btnChangePassword)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnDeleteAccount = findViewById<Button>(R.id.btnDeleteAccount)
        val arrowBtn101 = findViewById<ImageView>(R.id.btn_arrow101)

        // Load user data
        loadUserInfo()

        // Back to Mypage
        arrowBtn101.setOnClickListener {
            val intent = Intent(this, MypageActivity::class.java)
            startActivity(intent)
        }

        // Change password
        btnChangePassword.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }

        // Logout
        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, Login::class.java))
            finish()
        }

        // Delete account
        btnDeleteAccount.setOnClickListener {
            deleteAccount()
        }

        // Profile image click to change
        initImageViewProfile()
    }

    private fun initImageViewProfile() {
        ivProfile.setOnClickListener {
            when {
                // Check if permission is granted
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                    navigateGallery()
                }

                // Show rationale if needed
                shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    showPermissionContextPopup()
                }

                // Request permission
                else -> requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    1000
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1000) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
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
            .setMessage("프로필 이미지를 바꾸기 위해서는 갤러리 접근 권한이 필요합니다.")
            .setPositiveButton("동의하기") { _, _ -> requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1000) }
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
        user?.let {
            val credential = EmailAuthProvider.getCredential(user.email!!, "현재 비밀번호")
            user.reauthenticate(credential)
                .addOnSuccessListener {
                    user.delete()
                        .addOnSuccessListener {
                            Toast.makeText(this, "계정이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, Login::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "계정 삭제 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "재인증 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
