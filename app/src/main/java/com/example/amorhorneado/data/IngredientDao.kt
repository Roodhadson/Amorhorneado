package com.example.amorhorneado.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Transaction
import androidx.room.Relation
import androidx.room.Junction
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredients ORDER BY name ASC")
    fun getAllIngredients(): Flow<List<Ingredient>>

    @Query("SELECT * FROM ingredients WHERE id = :id")
    fun getIngredient(id: Int): Flow<Ingredient?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: Ingredient)
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIngredients(ingredients: List<Ingredient>)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateIngredient(ingredient: Ingredient)

    @Delete
    suspend fun deleteIngredient(ingredient: Ingredient)

    // Production Costs
    @Query("SELECT * FROM production_costs")
    fun getAllProductionCosts(): Flow<List<ProductionCost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductionCost(cost: ProductionCost)

    @Delete
    suspend fun deleteProductionCost(cost: ProductionCost)

    // Recipes
    @Query("SELECT * FROM recipes")
    fun getAllRecipes(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE id = :id")
    fun getRecipe(id: Int): Flow<Recipe?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe): Long

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Delete
    suspend fun deleteRecipe(recipe: Recipe)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeIngredient(crossRef: RecipeIngredientCrossRef)

    @Query("DELETE FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun deleteIngredientsForRecipe(recipeId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeProductionCost(crossRef: RecipeProductionCostCrossRef)

    @Query("DELETE FROM recipe_production_costs WHERE recipeId = :recipeId")
    suspend fun deleteProductionCostsForRecipe(recipeId: Int)

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    fun getRecipeWithIngredients(recipeId: Int): Flow<RecipeWithIngredients>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    fun getRecipeFullDetails(recipeId: Int): Flow<RecipeFullDetails>
    
    @Query("""
        SELECT i.*, ri.quantity 
        FROM ingredients i 
        INNER JOIN recipe_ingredients ri ON i.id = ri.ingredientId 
        WHERE ri.recipeId = :recipeId
    """)
    fun getIngredientsWithQuantityForRecipe(recipeId: Int): Flow<List<IngredientWithQuantity>>

    // Production Records
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductionRecord(record: ProductionRecord)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateProductionRecord(record: ProductionRecord)

    @Delete
    suspend fun deleteProductionRecord(record: ProductionRecord)

    @Query("SELECT * FROM production_records ORDER BY date DESC")
    fun getAllProductionRecords(): Flow<List<ProductionRecord>>

    // Sales Records
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleRecord(record: SaleRecord)

    @Query("SELECT * FROM sales_records ORDER BY date DESC")
    fun getAllSalesRecords(): Flow<List<SaleRecord>>

    @Query("SELECT * FROM sales_records WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getSalesRecordsInRange(startDate: Long, endDate: Long): Flow<List<SaleRecord>>

    // User Config
    @Query("SELECT * FROM user_config WHERE id = 1")
    fun getUserConfig(): Flow<UserConfig?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserConfig(userConfig: UserConfig)

    // Customers
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Query("UPDATE customers SET isSelectedForRaffle = :isSelected WHERE id = :id")
    suspend fun updateCustomerRaffleStatus(id: Int, isSelected: Boolean)

    // Debts (Fiados)
    @Query("SELECT * FROM debts ORDER BY date DESC")
    fun getAllDebts(): Flow<List<Debt>>

    @Query("SELECT * FROM debts WHERE customerId = :customerId ORDER BY date DESC")
    fun getDebtsForCustomer(customerId: Int): Flow<List<Debt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: Debt)

    @Update
    suspend fun updateDebt(debt: Debt)

    @Delete
    suspend fun deleteDebt(debt: Debt)
}

data class IngredientWithQuantity(
    @Embedded val ingredient: Ingredient,
    val quantity: Double
)

data class RecipeWithIngredients(
    @Embedded val recipe: Recipe,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            RecipeIngredientCrossRef::class,
            parentColumn = "recipeId",
            entityColumn = "ingredientId"
        )
    )
    val ingredients: List<Ingredient>
)

data class RecipeFullDetails(
    @Embedded val recipe: Recipe,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            RecipeIngredientCrossRef::class,
            parentColumn = "recipeId",
            entityColumn = "ingredientId"
        )
    )
    val ingredients: List<Ingredient>,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            RecipeProductionCostCrossRef::class,
            parentColumn = "recipeId",
            entityColumn = "productionCostId"
        )
    )
    val productionCosts: List<ProductionCost>
)
