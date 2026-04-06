import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BakeryDao {
    // Ingredientes
    @Query("SELECT * FROM ingredients")
    fun getAllIngredients(): Flow<List<Ingredient>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: Ingredient)

    // Recetas
    @Query("SELECT * FROM recipes")
    fun getAllRecipes(): Flow<List<Recipe>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe)

    // Costos de Producción
    @Query("SELECT * FROM production_costs")
    fun getAllProductionCosts(): Flow<List<ProductionCost>>
}