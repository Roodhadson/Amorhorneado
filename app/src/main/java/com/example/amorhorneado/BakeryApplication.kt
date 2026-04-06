package com.example.amorhorneado

import android.app.Application
import com.example.amorhorneado.data.AppDatabase
import com.example.amorhorneado.data.IngredientRepository
import com.example.amorhorneado.data.OfflineIngredientRepository

class BakeryApplication : Application() {
    lateinit var ingredientRepository: IngredientRepository

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getDatabase(this)
        ingredientRepository = OfflineIngredientRepository(database.ingredientDao())
    }
}