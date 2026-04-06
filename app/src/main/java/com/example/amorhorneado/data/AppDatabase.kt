package com.example.amorhorneado.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Ingredient::class,
        ProductionCost::class,
        Recipe::class,
        RecipeIngredientCrossRef::class,
        RecipeProductionCostCrossRef::class,
        ProductionRecord::class,
        SaleRecord::class,
        UserConfig::class,
        ExchangeRate::class,
        Customer::class,
        Debt::class
    ],
    version = 37, // Incrementado a 37 para añadir createdAt en Customer
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "bakery_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            try {
                                // Configuración inicial con valores por defecto de ubicación (Caracas)
                                db.execSQL("""
                                    INSERT INTO user_config (id, name, lastExchangeRate, city, latitude, longitude) 
                                    VALUES (1, 'Bakery Central', 0.0, 'Caracas', 10.4806, -66.8983)
                                """.trimIndent())
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    })
                    .build()
                    .also { Instance = it }
            }
        }
    }
}
