package com.example.amorhorneado.data

import kotlinx.coroutines.flow.Flow

interface IngredientRepository {
    // Ingredients
    fun getAllIngredientsStream(): Flow<List<Ingredient>>
    fun getIngredientStream(id: Int): Flow<Ingredient?>
    suspend fun insertIngredient(ingredient: Ingredient)
    suspend fun updateIngredient(ingredient: Ingredient)
    suspend fun deleteIngredient(ingredient: Ingredient)

    // Production Costs
    fun getAllProductionCostsStream(): Flow<List<ProductionCost>>
    suspend fun insertProductionCost(cost: ProductionCost)
    suspend fun deleteProductionCost(cost: ProductionCost)

    // Recipes
    fun getAllRecipesStream(): Flow<List<Recipe>>
    fun getRecipeStream(id: Int): Flow<Recipe?>
    suspend fun insertRecipe(recipe: Recipe): Long
    suspend fun updateRecipe(recipe: Recipe)
    suspend fun deleteRecipe(recipe: Recipe)
    
    // Recipe Cross-references
    suspend fun insertRecipeIngredient(crossRef: RecipeIngredientCrossRef)
    suspend fun deleteIngredientsForRecipe(recipeId: Int)
    suspend fun insertRecipeProductionCost(crossRef: RecipeProductionCostCrossRef)
    suspend fun deleteProductionCostsForRecipe(recipeId: Int)
    
    // Combined Recipe Data
    fun getRecipeFullDetailsStream(recipeId: Int): Flow<RecipeFullDetails>
    fun getRecipeWithIngredientsStream(recipeId: Int): Flow<RecipeWithIngredients>
    fun getIngredientsWithQuantityForRecipe(recipeId: Int): Flow<List<IngredientWithQuantity>>

    // Production Records
    fun getAllProductionRecordsStream(): Flow<List<ProductionRecord>>
    suspend fun insertProductionRecord(record: ProductionRecord)
    suspend fun updateProductionRecord(record: ProductionRecord)
    suspend fun deleteProductionRecord(record: ProductionRecord)

    // Sales Records
    fun getAllSalesRecordsStream(): Flow<List<SaleRecord>>
    fun getSalesRecordsInRangeStream(startDate: Long, endDate: Long): Flow<List<SaleRecord>>
    suspend fun insertSaleRecord(record: SaleRecord)

    // User Config
    fun getUserConfigStream(): Flow<UserConfig?>
    suspend fun insertUserConfig(userConfig: UserConfig)

    // Customers
    fun getAllCustomersStream(): Flow<List<Customer>>
    suspend fun insertCustomer(customer: Customer)
    suspend fun deleteCustomer(customer: Customer)

    // Debts (Fiados)
    fun getAllDebtsStream(): Flow<List<Debt>>
    fun getDebtsForCustomerStream(customerId: Int): Flow<List<Debt>>
    suspend fun insertDebt(debt: Debt)
    suspend fun updateDebt(debt: Debt)
    suspend fun deleteDebt(debt: Debt)
}

class OfflineIngredientRepository(private val ingredientDao: IngredientDao) : IngredientRepository {
    override fun getAllIngredientsStream(): Flow<List<Ingredient>> = ingredientDao.getAllIngredients()
    override fun getIngredientStream(id: Int): Flow<Ingredient?> = ingredientDao.getIngredient(id)
    override suspend fun insertIngredient(ingredient: Ingredient) = ingredientDao.insertIngredient(ingredient)
    override suspend fun updateIngredient(ingredient: Ingredient) = ingredientDao.updateIngredient(ingredient)
    override suspend fun deleteIngredient(ingredient: Ingredient) = ingredientDao.deleteIngredient(ingredient)

    override fun getAllProductionCostsStream(): Flow<List<ProductionCost>> = ingredientDao.getAllProductionCosts()
    override suspend fun insertProductionCost(cost: ProductionCost) = ingredientDao.insertProductionCost(cost)
    override suspend fun deleteProductionCost(cost: ProductionCost) = ingredientDao.deleteProductionCost(cost)

    override fun getAllRecipesStream(): Flow<List<Recipe>> = ingredientDao.getAllRecipes()
    override fun getRecipeStream(id: Int): Flow<Recipe?> = ingredientDao.getRecipe(id)
    override suspend fun insertRecipe(recipe: Recipe): Long = ingredientDao.insertRecipe(recipe)
    override suspend fun updateRecipe(recipe: Recipe) = ingredientDao.updateRecipe(recipe)
    override suspend fun deleteRecipe(recipe: Recipe) = ingredientDao.deleteRecipe(recipe)
    
    override suspend fun insertRecipeIngredient(crossRef: RecipeIngredientCrossRef) = 
        ingredientDao.insertRecipeIngredient(crossRef)
    override suspend fun deleteIngredientsForRecipe(recipeId: Int) = 
        ingredientDao.deleteIngredientsForRecipe(recipeId)
        
    override suspend fun insertRecipeProductionCost(crossRef: RecipeProductionCostCrossRef) =
        ingredientDao.insertRecipeProductionCost(crossRef)
    override suspend fun deleteProductionCostsForRecipe(recipeId: Int) =
        ingredientDao.deleteProductionCostsForRecipe(recipeId)
        
    override fun getRecipeFullDetailsStream(recipeId: Int): Flow<RecipeFullDetails> =
        ingredientDao.getRecipeFullDetails(recipeId)

    override fun getRecipeWithIngredientsStream(recipeId: Int): Flow<RecipeWithIngredients> =
        ingredientDao.getRecipeWithIngredients(recipeId)
        
    override fun getIngredientsWithQuantityForRecipe(recipeId: Int): Flow<List<IngredientWithQuantity>> =
        ingredientDao.getIngredientsWithQuantityForRecipe(recipeId)

    override fun getAllProductionRecordsStream(): Flow<List<ProductionRecord>> =
        ingredientDao.getAllProductionRecords()
    override suspend fun insertProductionRecord(record: ProductionRecord) =
        ingredientDao.insertProductionRecord(record)
    override suspend fun updateProductionRecord(record: ProductionRecord) =
        ingredientDao.updateProductionRecord(record)
    override suspend fun deleteProductionRecord(record: ProductionRecord) =
        ingredientDao.deleteProductionRecord(record)

    override fun getAllSalesRecordsStream(): Flow<List<SaleRecord>> =
        ingredientDao.getAllSalesRecords()
    override fun getSalesRecordsInRangeStream(startDate: Long, endDate: Long): Flow<List<SaleRecord>> =
        ingredientDao.getSalesRecordsInRange(startDate, endDate)
    override suspend fun insertSaleRecord(record: SaleRecord) =
        ingredientDao.insertSaleRecord(record)

    override fun getUserConfigStream(): Flow<UserConfig?> = ingredientDao.getUserConfig()
    override suspend fun insertUserConfig(userConfig: UserConfig) = ingredientDao.insertUserConfig(userConfig)

    override fun getAllCustomersStream(): Flow<List<Customer>> = ingredientDao.getAllCustomers()
    override suspend fun insertCustomer(customer: Customer) = ingredientDao.insertCustomer(customer)
    override suspend fun deleteCustomer(customer: Customer) = ingredientDao.deleteCustomer(customer)

    override fun getAllDebtsStream(): Flow<List<Debt>> = ingredientDao.getAllDebts()
    override fun getDebtsForCustomerStream(customerId: Int): Flow<List<Debt>> = ingredientDao.getDebtsForCustomer(customerId)
    override suspend fun insertDebt(debt: Debt) = ingredientDao.insertDebt(debt)
    override suspend fun updateDebt(debt: Debt) = ingredientDao.updateDebt(debt)
    override suspend fun deleteDebt(debt: Debt) = ingredientDao.deleteDebt(debt)
}
