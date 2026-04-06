package com.example.amorhorneado.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sales_records")
data class SaleRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val recipeId: Int,
    val recipeTitle: String,
    val quantity: Int,
    val totalAmount: Double, // Always in USD
    val exchangeRate: Double, // The rate at the time of sale
    val totalAmountBs: Double, // The total in Bs at the time of sale
    val paymentMethod: String, // "Pagomovil", "Efectivo Bs", "Efectivo $", etc
    val date: Long = System.currentTimeMillis(),
    val imagePath: String? = null,
    val customerId: Int? = null // To track which customer made the purchase
)
