package com.example.zoljak_firebase_firestore_read_write_test

import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.view.isInvisible
import com.example.zoljak_firebase_firestore_read_write_test.databinding.ActivityFirebaseRecyclerViewBinding
import com.example.zoljak_firebase_firestore_read_write_test.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpInsertButton()
        setUpLoadButton()
        setUpGoToRecyclerViewTestButton()
        setAuthButton()
    }

    // 데이터 삽입 버튼 활성화
    private fun setUpInsertButton() {
        binding.insertButton.setOnClickListener {
            val foodName = binding.menuNameEditText.text.toString()
            val foodGramStr = binding.menuGramEditText.text.toString()
            val foodCalStr = binding.menuCalEditText.text.toString()
            val foodCarbonStr = binding.menuCarbonEditText.text.toString()
            val foodProteinStr = binding.menuProteinEditText.text.toString()
            val foodFatStr = binding.menuFatEditText.text.toString()

            // 문자열이 빈 문자열인지 확인
            if(foodName.isEmpty() || foodGramStr.isEmpty() || foodCalStr.isEmpty() || foodCarbonStr.isEmpty() || foodProteinStr.isEmpty() || foodFatStr.isEmpty()) {
                Toast.makeText(this, "모든 필드를 채워 주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 문자열을 정수로 변환
            val foodGram = foodGramStr.toInt()
            val foodCal = foodCalStr.toInt()
            val foodCarbon = foodCarbonStr.toInt()
            val foodProtein = foodProteinStr.toInt()
            val foodFat = foodFatStr.toInt()

            // 데이터 통신을 위해 해시맵으로 감싸기
            val data = hashMapOf(
                "foodName" to foodName,
                "totalCal" to foodCal,
                "totalGram" to foodGram,
                "totalCarbon" to foodCarbon,
                "totalProtein" to foodProtein,
                "totalFat" to foodFat
            )

            // 지정된 문서 이름으로 문서 생성
            val ref = db.collection("foods").document(foodName).set(data)
                .addOnSuccessListener {
                    Toast.makeText(this, "데이터 추가 성공!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "데이터 추가 실패..", Toast.LENGTH_SHORT).show()
                    Log.e("InsertProcess", "Insert Error! : $it")
                }
        }
    }




    // 검색 버튼 활성화
    private fun setUpLoadButton() {
        binding.loadButton.setOnClickListener {
            val searchMenuName = binding.menuLoadEditText.text.toString()

            // 문자열이 비어 있는지 확인
            if (searchMenuName.isEmpty()) {
                Toast.makeText(this, "메뉴 이름을 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val ref = db.collection("foods")
                .whereEqualTo("foodName", searchMenuName)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Toast.makeText(this, "해당 메뉴를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        for (document in documents) {
                            // document 객체에서 데이터 접근
                            val foodName = document.getString("foodName")
                            val totalCal = document.getLong("totalCal")
                            val totalGram = document.getLong("totalGram")
                            val totalCarbon = document.getLong("totalCarbon")
                            val totalProtein = document.getLong("totalProtein")
                            val totalFat = document.getLong("totalFat")

                            binding.searchResultTextView.text =
                                "음식이름 : ${foodName}, " +
                                        "무게 : ${totalGram}, " +
                                        "칼로리 : ${totalCal}, " +
                                        "탄수화물 : ${totalCarbon}, " +
                                        "단백질 : ${totalProtein}, " +
                                        "지방 : ${totalFat}"
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "데이터 로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    Log.e("LoadProcess", "Load Error! : $exception")
                }
        }
    }

    private fun setUpGoToRecyclerViewTestButton(){
        binding.gotoFirebaseRecyclerViewTestActivity.setOnClickListener {
            val intent = Intent(applicationContext, FirebaseRecyclerViewActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setAuthButton() {
        val auth = Firebase.auth
        val user = auth.currentUser

        //로그인 상태에 따라 버튼 활성/비활성
        if (user != null) {
            binding.gotoAuthButton.isInvisible = true
        } else {
            binding.gotoAuthButton.isInvisible = false
        }
        binding.gotoAuthButton.setOnClickListener {
            val intent = Intent(this@MainActivity, AuthActivity::class.java)
            startActivity(intent)
        }
    }

}