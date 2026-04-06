package com.example.amorhorneado.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exchange_rates")
data class ExchangeRate(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val rate: Double,
    val date: Long = System.currentTimeMillis()
)
