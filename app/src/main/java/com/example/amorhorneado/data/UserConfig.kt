package com.example.amorhorneado.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_config")
data class UserConfig(
    @PrimaryKey val id: Int = 1,
    val name: String = "Bakery Central",
    val profileImagePath: String? = null,
    val lastExchangeRate: Double = 0.0,
    val city: String = "Caracas",
    val latitude: Double = 10.4806,
    val longitude: Double = -66.8983
)
