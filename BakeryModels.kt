import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class Ingredient(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val costPrice: Double,
    val salePrice: Double,
    val unit: String // kg, gr, unidad, etc.
)

@Entity(tableName = "production_costs")
data class ProductionCost(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val concept: String, // Gas, Luz, Mano de obra
    val amount: Double
)

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val instructions: String,
    val baseCost: Double, // Suma de ingredientes + costos fijos
    val finalSalePrice: Double
)