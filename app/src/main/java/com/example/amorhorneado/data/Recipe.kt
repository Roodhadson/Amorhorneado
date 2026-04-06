package com.example.amorhorneado.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val manualSalePrice: Double? = null,
    val imagePath: String? = null,
    val minStock: Int = 0,
    val baseCost: Double = 0.0,
    val isPortionEnabled: Boolean = false,
    val portionsPerRecipe: Int = 1,
    val portionSalePrice: Double? = null
)

@Entity(
    tableName = "recipe_ingredients",
    primaryKeys = ["recipeId", "ingredientId"],
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Ingredient::class,
            parentColumns = ["id"],
            childColumns = ["ingredientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ingredientId")]
)
data class RecipeIngredientCrossRef(
    val recipeId: Int,
    val ingredientId: Int,
    val quantity: Double
)

@Entity(
    tableName = "recipe_production_costs",
    primaryKeys = ["recipeId", "productionCostId"],
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductionCost::class,
            parentColumns = ["id"],
            childColumns = ["productionCostId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("productionCostId")]
)
data class RecipeProductionCostCrossRef(
    val recipeId: Int,
    val productionCostId: Int
)
