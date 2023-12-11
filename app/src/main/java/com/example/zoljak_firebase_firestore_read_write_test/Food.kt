package com.example.zoljak_firebase_firestore_read_write_test

data class Food(
    var foodName: String? = null,
    var totalCal: Number? = null,
    var totalCarbon: Number? = null,
    var totalFat: Number? = null,
    var totalGram: Number? = null,
    var totalProtein: Number? = null) : java.io.Serializable