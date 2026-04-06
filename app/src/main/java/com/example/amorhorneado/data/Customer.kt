package com.example.amorhorneado.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val isSelectedForRaffle: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
