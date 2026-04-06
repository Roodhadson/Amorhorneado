package com.example.amorhorneado.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "debts",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Debt(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val customerId: Int,
    val amount: Double, // Original Amount
    val remainingAmount: Double = amount, // Current Balance
    val concept: String,
    val date: Long = System.currentTimeMillis(),
    val isPaid: Boolean = false,
    val recipeId: Int? = null // To track which product was bought on credit
)
