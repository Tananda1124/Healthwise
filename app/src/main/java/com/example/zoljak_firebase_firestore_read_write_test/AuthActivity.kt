package com.example.zoljak_firebase_firestore_read_write_test

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.zoljak_firebase_firestore_read_write_test.databinding.ActivityAuthBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthActivity : AppCompatActivity() {
    private lateinit var binding : ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setJoinButton()
        setLoginButton()
    }

    private fun setJoinButton(){
        binding.joinButton.setOnClickListener {
            val email = binding.emailInputEditText.text.toString()
            val password = binding.passwordInputEditText.text.toString()

            if(email == "" || password == "" || password.length < 6){
                Snackbar.make(binding.root, "이메일 또는 패스워드가 입력되지 않았습니다.", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Firebase.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful) {
                        Snackbar.make(binding.root, "회원가입 성공!", Snackbar.LENGTH_SHORT).show()
                        // 로그인 된 상태
                    } else{
                        Snackbar.make(binding.root, "회원가입 실패!", Snackbar.LENGTH_SHORT).show()
                    }
                }
        }
    }

    fun setLoginButton(){
        binding.loginButton.setOnClickListener {
            val email = binding.emailInputEditText.text.toString()
            val password = binding.passwordInputEditText.text.toString()

            if(Firebase.auth.currentUser == null){
                // 유저 정보가 없으면 로그인
                if(email == "" || password == ""){
                    Snackbar.make(binding.root, "이메일 또는 패스워드가 입력되지 않았습니다.", Snackbar.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                Firebase.auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task->
                        if(task.isSuccessful){
                            // 로그인 성공시 홈 화면으로 보내기
                            Snackbar.make(binding.root, "${Firebase.auth.currentUser}님 안녕하세요!", Snackbar.LENGTH_SHORT).show()
                            setViewsLoginState()
                        }else{
                            Snackbar.make(binding.root, "로그인에 실패했습니다. 이메일 혹은 패스워드를 확인해 주세요.", Snackbar.LENGTH_SHORT).show()
                        }
                    }
            } else{
                // 유저 정보가 이미 있으면 로그아웃
                Firebase.auth.signOut()
                setViewsLogoutState()
                //todo 로그인으로 바꾸기
            }

        }

    }

    fun setViewsLoginState(){
        binding.emailInputEditText.isEnabled = false
        binding.passwordInputLayout.isEnabled = false
        binding.joinButton.isEnabled = false
        binding.loginButton.text = "로그아웃"
    }

    fun setViewsLogoutState(){
        binding.emailInputEditText.isEnabled = true
        binding.passwordInputLayout.isEnabled = true
        binding.joinButton.isEnabled = true
        binding.loginButton.text = "로그인"
    }

}