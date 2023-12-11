package com.example.zoljak_firebase_firestore_read_write_test

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.zoljak_firebase_firestore_read_write_test.databinding.ActivityFoodAddBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FoodAddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFoodAddBinding
    private var db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setFoodAddButton()
    }

    private fun setFoodAddButton() {
        binding.addButton.setOnClickListener {
            val foodName = binding.foodNameEditText.text.toString()
            val foodGramStr = binding.gramInputEditText.text.toString()
            val foodCalStr = binding.calInputEditText.text.toString()
            val foodCarbonStr = binding.carbonInputEditText.text.toString()
            val foodProteinStr = binding.proteinInputEditText.text.toString()
            val foodFatStr = binding.fatInputEditText.text.toString()

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
}