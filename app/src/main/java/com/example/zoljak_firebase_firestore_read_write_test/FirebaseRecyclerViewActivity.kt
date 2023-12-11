package com.example.zoljak_firebase_firestore_read_write_test

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.zoljak_firebase_firestore_read_write_test.databinding.ActivityFirebaseRecyclerViewBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import kotlin.math.round

class FirebaseRecyclerViewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFirebaseRecyclerViewBinding
    private lateinit var rvAdapter: FoodAdapter
    private lateinit var foodList: ArrayList<Food>
    private lateinit var selectedFoodList : ArrayList<Food>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirebaseRecyclerViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpUploadButton()

        //날짜 설정
        var date = getCurrentDate()


        // Firebase 통신 기능들 코루틴에 집어넣기(네트워크 및 태스크 사용)
        lifecycleScope.launch {
            // 검색 데이터 가져오고 intent로 전송 준비
            foodList = getDocumentMenus() as ArrayList<Food>
            setFoodAddButton(foodList)

            //텍스트뷰, 그래프에 바인딩할 데이터 설정
            var cal : Float? = 0.0f
            var protein : Float? = 0.0f
            var carbon : Float? = 0.0f
            var fat : Float? = 0.0f
            var gram : Float? = 0.0f

            //검색 탭에서 유저가 선택한 메뉴 가져오기
            val food = intent.getSerializableExtra("selectedFood") as? Food

            // intent로 가져온 데이터가 있으면
            if (food != null){
                binding.textTotalGram.text = food.foodName
                selectedFoodList = ArrayList<Food>()  // 새로운 리스트 생성
                val userFoodList = getMenuDocument()?.let { getUserFoodByDay(date, it) }
                if (userFoodList != null) {
                    selectedFoodList.addAll(userFoodList)
                }
                for(item in selectedFoodList){
                    gram = gram!! + item.totalGram!!.toFloat()
                    cal = cal!! + item.totalCal!!.toFloat()
                    carbon = carbon!! + item.totalCarbon!!.toFloat()
                    protein = protein!! + item.totalProtein!!.toFloat()
                    fat = fat!! + item.totalFat!!.toFloat()
                }
                setupRecyclerView(selectedFoodList)
                setUpTextView(cal!!, carbon!!, protein!!, fat!!)
                setupPieChart(carbon, protein, fat)
            }
            // intent로 가져온 데이터가 없으면
            else{
                val userFoodList = getMenuDocument()?.let { getUserFoodByDay(date, it) }
                if(userFoodList != null){
                    setupRecyclerView(userFoodList)
                    for(item in userFoodList){
                        gram = gram!! + item.totalGram!!.toFloat()
                        cal = cal!! + item.totalCal!!.toFloat()
                        carbon = carbon!! + item.totalCarbon!!.toFloat()
                        protein = protein!! + item.totalProtein!!.toFloat()
                        fat = fat!! + item.totalFat!!.toFloat()
                    }
                    setUpTextView(cal!!, carbon!!, protein!!, fat!!)
                    setupPieChart(carbon, protein, fat)
                }
            }

        }

    }

    override fun onRestart() {
        super.onRestart()
        // 유저 커스텀 데이터 업로드 시, 업로드한 데이터 가져오기 위해 onRestart()에서 데이터 한번 더 가져오기
        lifecycleScope.launch {
            foodList = getDocumentMenus() as ArrayList<Food>
            setFoodAddButton(foodList)
        }
    }

    //Firebase에서 모든 음식 이름을 가져와 Food의 MutableList로 재가공
    private suspend fun getDocumentMenus(): MutableList<Food> {
        val foodDocument = mutableListOf<Food>()
        val db = Firebase.firestore
        try {
            val snapshot = db.collection("foods").get().await()
            if (snapshot.isEmpty) {
                foodDocument.add(Food("null food", 0, 0, 0, 0, 0))
                Log.e("getDocumentMenus", "No responds from DB..")
            } else {
                for (item in snapshot) {
                    val food = Food(
                        item.getString("foodName") ?: "",
                        item.getLong("totalCal")?.toFloat() ?: 0.0,
                        item.getLong("totalCarbon")?.toFloat() ?: 0.0,
                        item.getLong("totalFat")?.toFloat() ?: 0.0,
                        item.getLong("totalGram")?.toFloat() ?: 0.0,
                        item.getLong("totalProtein")?.toFloat() ?: 0.0
                    )
                    foodDocument.add(food)

                }
            }
        } catch (exception: Exception) {
            Log.e("getDocumentMenus", "error during get method.. : $exception")
        }
        return foodDocument
    }

    private fun setupRecyclerView(foodList: ArrayList<Food>) {
        if(foodList.isEmpty()){
            return
        }
        else {
            rvAdapter = FoodAdapter(foodList)  // 어댑터를 생성하고 설정
            binding.foodListRecyclerView.apply {
                layoutManager = LinearLayoutManager(this@FirebaseRecyclerViewActivity)
                adapter = rvAdapter
            }
        }
    }


    // 검색 탭으로 이동
    private fun setFoodAddButton(foodList: ArrayList<Food>){
        binding.foodAddButton.setOnClickListener {
            if(foodList.isNotEmpty()){
                val intent = Intent(applicationContext, SearchFoodActivity::class.java)
                var foodNameList = ArrayList<String>()
                var foodCalList = ArrayList<Float>()
                var foodGramList = ArrayList<Float>()
                var foodCarbonList = ArrayList<Float>()
                var foodProteinList = ArrayList<Float>()
                var foodFatList = ArrayList<Float>()
                for(menu in foodList) {
                    foodNameList.add(menu.foodName.toString())
                    foodCalList.add(menu.totalCal!!.toFloat())
                    foodGramList.add(menu.totalGram!!.toFloat())
                    foodCarbonList.add(menu.totalCarbon!!.toFloat())
                    foodProteinList.add(menu.totalProtein!!.toFloat())
                    foodFatList.add(menu.totalFat!!.toFloat())
                }
                intent.putExtra("foodNameList", foodNameList)
                intent.putExtra("foodCalList", foodCalList)
                intent.putExtra("foodGramList", foodGramList)
                intent.putExtra("foodCarbonList", foodCarbonList)
                intent.putExtra("foodProteinList", foodProteinList)
                intent.putExtra("foodFatList", foodFatList)
                startActivity(intent)
            }
            else{
                Toast.makeText(this,"No food data to send..", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //todo 유저 이름과 날짜별로 먹은 메뉴 가져오기
    private suspend fun getMenuDocument(): ArrayList<UserMenu>?  {
        val currentUserUid = Firebase.auth.currentUser?.uid
        val menuList = ArrayList<UserMenu>()

        if (currentUserUid != null) {
            try {
                val result = Firebase.firestore.collection("userMenus")
                    .whereEqualTo("userName", currentUserUid)
                    .get()
                    .await()

                for (document in result.documents) {
                    val userMenu = document.toObject(UserMenu::class.java)
                    if (userMenu != null) {
                        menuList.add(userMenu)
                    }
                }
                return menuList  // menuList를 반환합니다.
            } catch (e: Exception) {
                // 데이터 가져오기에 실패한 경우의 처리
                Log.e("Firebase", "Error getting documents: ", e)
            }
        } else {
            // 현재 사용자가 로그인하지 않은 경우의 처리
            withContext(Dispatchers.Main) {
                Toast.makeText(this@FirebaseRecyclerViewActivity, "로그인을 진행해 주세요.", Toast.LENGTH_SHORT).show()
            }
        }
        return null  // 오류가 발생하거나 로그인하지 않은 경우 null을 반환
    }

    //유저가 그 날에 먹은 음식 데이터 가공하기
    private fun getUserFoodByDay(today: String, list: ArrayList<UserMenu>): ArrayList<Food> {
        val resultList = ArrayList<Food>()
        for (userMenu in list) {
            if (userMenu.Date != null && userMenu.Date == getCurrentDate()) {
                val food = Food(
                    foodName = userMenu.foodName,
                    totalCal = userMenu.totalCal,
                    totalGram = userMenu.totalGram,
                    totalCarbon = userMenu.totalCarbon,
                    totalProtein = userMenu.totalProtein,
                    totalFat = userMenu.totalFat
                )
                resultList.add(food)
            }
        }
        return resultList
    }

    // 날짜 데이터 생성 함수
    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        return sdf.format(Date())
    }

    // 텍스트뷰 세팅
    private fun setUpTextView(cal : Float, carbon : Float, fat : Float, protein : Float){
        binding.textTotalCal.text = "${cal.toInt()} Kcal 섭취하셨습니다."
        binding.textTotalCal.textSize = 25.0f
        binding.textTotalGram.text = "탄수화물 ${carbon.toInt()}g, 단백질 ${protein.toInt()}g, 지방 ${fat.toInt()}g"
    }

    // 파이차트 세팅
    private fun setupPieChart(carbs: Float, fats: Float, proteins: Float) {
        val pieChart: PieChart = findViewById(R.id.pieChart)

        // 데이터 항목 생성
        val entries = arrayListOf(
            PieEntry(carbs, "탄수화물"),
            PieEntry(fats, "지방"),
            PieEntry(proteins, "단백질")
        )

        // 데이터 세트 생성
        val dataSet = PieDataSet(entries, "영양소 비율")

        // 색상 설정
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()

        // 데이터 설정
        val data = PieData(dataSet)
        pieChart.data = data

        // 범례와 설명 레이블 숨기기
        pieChart.legend.isEnabled = false
        pieChart.description.isEnabled = false

        // 퍼센티지 데이터 설정
        val percentCarbon = round(carbs.toDouble()/(carbs + proteins + fats).toDouble() * 100)
        val percentProtein = round(proteins.toDouble()/(carbs + proteins + fats).toDouble() * 100)
        val percentFat = round(fats.toDouble()/(carbs + proteins + fats).toDouble() * 100)

        // 레이블 포맷터 생성 및 설정
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return when (value) {
                    carbs -> "탄수화물 (${percentCarbon}%)"
                    fats -> "지방 (${percentFat}%)"
                    proteins -> "단백질 (${percentProtein}%)"
                    else -> super.getFormattedValue(value)
                }
            }
        }

        // 항목 레이블의 텍스트 크기와 색상 설정
        pieChart.setEntryLabelTextSize(16f)  // 글씨 크기
        dataSet.setValueTextSize(16f)  // 값의 글씨 크기
        dataSet.setValueTextColor(Color.BLACK)  // 값의 글씨

        // 차트 갱신
        pieChart.invalidate()
    }

    private fun setUpUploadButton() {
        binding.uploadButton.setOnClickListener {
            // intent로 다음 액티비티로 이동
            val intent = Intent(this@FirebaseRecyclerViewActivity, FoodAddActivity::class.java)
            if (Firebase.auth.currentUser != null) {
                startActivity(intent)
            }
        }
    }
}