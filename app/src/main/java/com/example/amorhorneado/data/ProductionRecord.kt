package com.example.amorhorneado.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "production_records")
data class ProductionRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val recipeId: Int,
    val quantity: Int, // Represents full cakes
    val portionQuantity: Int = 0, // Represents individual portions available
    val date: Long = System.currentTimeMillis()
)
