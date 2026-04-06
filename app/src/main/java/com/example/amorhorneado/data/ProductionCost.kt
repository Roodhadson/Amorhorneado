package com.example.amorhorneado.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "production_costs")
data class ProductionCost(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val concept: String,
    val amount: Double
)