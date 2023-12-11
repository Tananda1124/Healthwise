package com.example.zoljak_firebase_firestore_read_write_test

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.zoljak_firebase_firestore_read_write_test.databinding.ItemRecyclerviewBinding

class FoodAdapter(
    private var foodList: ArrayList<Food>,
    private val itemClickListener: OnItemClickListener? = null  // 인터페이스를 생성자 인자로 추가
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    interface OnItemClickListener {  // 온클릭 리스너 인터페이스 정의, FoodViewHolder에서 재사용
        fun onItemClick(food: Food)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : FoodAdapter.FoodViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemRecyclerviewBinding.inflate(inflater, parent, false)
        return FoodViewHolder(binding, itemClickListener)  // 인터페이스를 FoodViewHolder에 전달
    }

    override fun getItemCount(): Int {
        return foodList.size
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = foodList[position]
        holder.bind(food)
    }

    fun updateDataSet(newFoodList: ArrayList<Food>) {
        this.foodList = newFoodList
        notifyDataSetChanged()
    }

    class FoodViewHolder(
        private val binding: ItemRecyclerviewBinding,
        private val itemClickListener: OnItemClickListener?  // 인터페이스를 FoodViewHolder의 생성자 인자로 추가
    ) : RecyclerView.ViewHolder(binding.root){
        fun bind(food: Food){
            binding.foodNameTextView.text = food.foodName
            if(food.foodName?.length ?: 0 > 5){
                binding.foodNameTextView.textSize = 15.0f
            }
            else if(food.foodName?.length ?:0 > 10){
                binding.foodNameTextView.textSize = 10.0f
            }
            binding.foodNutritionTextView.text =
                "${food.totalCal.toString()}Kcal, " + "탄수 ${food.totalCarbon.toString()}g, " + "단백 ${food.totalProtein.toString()}g, " + "지방 ${food.totalFat.toString()}g"
            itemClickListener.let {listener ->
                binding.root.setOnClickListener{
                    listener?.onItemClick(food)
                }
            }
        }
    }


}