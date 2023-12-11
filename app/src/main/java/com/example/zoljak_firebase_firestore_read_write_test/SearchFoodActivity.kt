package com.example.zoljak_firebase_firestore_read_write_test

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zoljak_firebase_firestore_read_write_test.databinding.ActivitySearchFoodBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

class SearchFoodActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchFoodBinding
    private lateinit var foodList: ArrayList<Food>
    private lateinit var rvAdapter: FoodAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchFoodBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //검색에 필요한 데이터 가져오기
        foodList = getMenuData()

        //todo 데이터 수신 체크, 추후 제거
        if (foodList.isNotEmpty()) {
            binding.dataTestTextView.text = "데이터 로드 성공"
        } else {
            binding.dataTestTextView.text = "No food data available"
        }

        //검색 버튼을 누르면 알맞은 RecyclerView를 생성
        setSearchButton()
    }

    //Intent를 이용해 ArrayList로 식품 데이터 가져오기
    private fun getMenuData(): ArrayList<Food> {
        var foodList = ArrayList<Food>()
        var foodNameList = intent.getStringArrayListExtra("foodNameList")
        var foodCalList = intent.getSerializableExtra("foodCalList") as? ArrayList<Float>
        var foodGramList = intent.getSerializableExtra("foodGramList") as? ArrayList<Float>
        var foodCarbonList = intent.getSerializableExtra("foodCarbonList") as? ArrayList<Float>
        var foodProteinList = intent.getSerializableExtra("foodProteinList") as? ArrayList<Float>
        var foodFatList = intent.getSerializableExtra("foodFatList") as? ArrayList<Float>

        if (foodNameList != null && foodCalList != null && foodGramList != null &&
            foodCarbonList != null && foodProteinList != null && foodFatList != null
        ) {

            val count = foodNameList.size
            for (i in 0 until count) {
                foodList.add(
                    Food(
                        foodName = foodNameList[i],
                        totalCal = foodCalList[i],
                        totalGram = foodGramList[i],
                        totalCarbon = foodCarbonList[i],
                        totalProtein = foodProteinList[i],
                        totalFat = foodFatList[i]
                    )
                )
            }
        } else {
            Toast.makeText(this, "Failed to receive food data", Toast.LENGTH_SHORT).show()
        }

        return foodList
    }

    //spinner의 옵션에 따라 ArrayList<Food>를 가공해 RecyclerView에 나타내기 위해 반환
    private fun searchBySpinnerItemAndGetFoodList(): ArrayList<Food> {
        val searchSpinnerItem = binding.foodSearchOptionSpinner.selectedItem.toString()
        val userSearchString = binding.searchFoodEditText.text.toString()
        var searchedFoodList = ArrayList<Food>()
        if (foodList.isNotEmpty()) {
            when (searchSpinnerItem) {
                "이름" -> {
                    for (item in foodList) {
                        if (item.foodName.toString() == userSearchString || item.foodName.toString().contains(userSearchString)) {
                            searchedFoodList.add(item)
                        }
                    }
                    return searchedFoodList
                }
                "칼로리" -> {
                    for (item in foodList){
                        if ((item.totalCal?.toFloat() ?: 0f) <= userSearchString.toFloat()){
                            searchedFoodList.add(item)
                        }
                    }
                    return searchedFoodList
                }
                "단백질" -> {
                    for (item in foodList){
                        if ((item.totalProtein?.toFloat() ?: 0f) <= userSearchString.toFloat()){
                            searchedFoodList.add(item)
                        }
                    }
                    return searchedFoodList
                }
                "탄수화물" -> {
                    for (item in foodList){
                        if ((item.totalCarbon?.toFloat() ?: 0f) <= userSearchString.toFloat()){
                            searchedFoodList.add(item)
                        }
                    }
                    return searchedFoodList
                }
                "지방" -> {
                    for (item in foodList){
                        if ((item.totalFat?.toFloat() ?: 0f) <= userSearchString.toFloat()){
                            searchedFoodList.add(item)
                        }
                    }
                    return searchedFoodList
                }
                else -> {
                    return searchedFoodList
                }
            }
        }
        else{
            return searchedFoodList
        }

    }

    //검색 버튼의 이벤트 리스너 설정
    private fun setSearchButton(){
        binding.searchButton.setOnClickListener {
            val rvList = searchBySpinnerItemAndGetFoodList()
            setRecyclerViewAndTouchEvent(rvList)
        }
    }

    //리사이클러뷰 세팅 함수, 터치 이벤트까지 내부 구현
    private fun setRecyclerViewAndTouchEvent(rvList : ArrayList<Food>) {
        rvAdapter = FoodAdapter(rvList, object : FoodAdapter.OnItemClickListener{
            // 사전에 구현한 Interface 구현
            override fun onItemClick(food: Food) {
                //todo 리사이클러뷰 아이템 클릭 리스너, List 객체가 넘어가도록 세팅
                Toast.makeText(this@SearchFoodActivity, "${food.foodName} clicked", Toast.LENGTH_SHORT).show()

                    // 코루틴으로 업로드 기능
                    lifecycleScope.launch {
                        // 업로드할 데이터 해시맵으로 감싸기
                        val userPickedFoodData = hashMapOf(
                            "userName" to Firebase.auth.currentUser?.uid.toString(),
                            "Date" to getCurrentDate(),
                            "foodName" to food.foodName,
                            "totalCal" to food.totalCal,
                            "totalGram" to food.totalGram,
                            "totalCarbon" to food.totalCarbon,
                            "totalProtein" to food.totalProtein,
                            "totalFat" to food.totalFat
                        )


                        // 해당 날짜의 메뉴를 Firebase와 통신해 추가
                        Firebase.firestore.collection("userMenus").add(userPickedFoodData)
                            .addOnSuccessListener {
                            Toast.makeText(applicationContext,"Firebase 데이터 추가 성공", Toast.LENGTH_SHORT).show()
                        }.addOnFailureListener {
                            Toast.makeText(applicationContext,"Firebase 데이터 추가 실패", Toast.LENGTH_SHORT).show()
                            Log.e("onFailureListener", "exception : $it")
                        }

                        // intent로 다음 액티비티로 이동
                        val intent = Intent(this@SearchFoodActivity, FirebaseRecyclerViewActivity::class.java)
                        if(Firebase.auth.currentUser != null){
                            intent.putExtra("selectedFood", food)
                            startActivity(intent)
                    }

                }
            }
        })
        binding.foodSearchRecyclerView.apply{
            layoutManager = LinearLayoutManager(this@SearchFoodActivity)
            adapter = rvAdapter
        }
    }

    // 날짜 데이터 생성 함수
    fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        return sdf.format(Date())
    }

}