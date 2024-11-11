package com.example.madagascar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.madagascar.API.Festival
import com.example.madagascar.Hobby.hobby
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val registerButton: Button = findViewById(R.id.registerbutton) // 여기서부터 추가됨

        registerButton.setOnClickListener {  //버튼을 클릭시 이벤트
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
        auth = FirebaseAuth.getInstance() //firebase에서 인스턴스를 가져온다.

        val loginButton: Button = findViewById(R.id.loginbutton) //로그인 버튼 객체 생성

        loginButton.setOnClickListener { //로그인 버튼을 클릭하면 loginUser 함수 실행
            loginUser()
        }
    }

    private fun loginUser() { //xml에서 id를 가져온다
        val email = findViewById<EditText>(R.id.EmailAddress).text.toString()
        val password = findViewById<EditText>(R.id.Password).text.toString()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // 로그인 성공 시
                    val user = auth.currentUser
                    Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show() //로그인 성공이라는 메세지를 띄운다.
                    // Add your logic to navigate to another activity or perform other actions
                    val intent = Intent(this, hobby::class.java)
                    startActivity(intent)
                } else {
                    // 로그인 실패 시
                    Toast.makeText(this, "로그인 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}