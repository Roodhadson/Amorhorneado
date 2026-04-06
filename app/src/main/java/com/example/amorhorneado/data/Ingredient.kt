package com.example.amorhorneado.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class Ingredient(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val costPrice: Double,
    val salePrice: Double,
    val unit: String, // "Unidades (ud)", "Kilogramos (kg)"
    val stock: Double,
    val minStock: Double = 0.0,
    val imagePath: String? = null
)
