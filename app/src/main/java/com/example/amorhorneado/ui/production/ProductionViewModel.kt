package com.example.amorhorneado.ui.production

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amorhorneado.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

class ProductionViewModel(private val repository: IngredientRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val productionRecords: StateFlow<List<ProductionRecordWithRecipe>> = 
        combine(
            repository.getAllProductionRecordsStream(),
            repository.getAllRecipesStream(),
            _searchQuery
        ) { records, recipes, query ->
            records.filter { it.quantity > 0 || it.portionQuantity > 0 }.map { record ->
                ProductionRecordWithRecipe(
                    record = record,
                    recipe = recipes.find { it.id == record.recipeId }
                )
            }.filter { 
                if (query.isEmpty()) true 
                else it.recipe?.title?.contains(query, ignoreCase = true) == true
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private val _salesFilter = MutableStateFlow("Hoy")
    val salesFilter: StateFlow<String> = _salesFilter.asStateFlow()

    private val _customDate = MutableStateFlow<Long?>(null)

    val customers: StateFlow<List<Customer>> = repository.getAllCustomersStream()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val salesRecords: StateFlow<List<SaleRecord>> = combine(
        repository.getAllSalesRecordsStream(),
        _salesFilter,
        _customDate
    ) { records, filter, customDate ->
        if (filter == "Personalizado" && customDate != null) {
            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                timeInMillis = customDate
            }
            val ty = utcCal.get(Calendar.YEAR)
            val tm = utcCal.get(Calendar.MONTH)
            val td = utcCal.get(Calendar.DAY_OF_MONTH)
            
            records.filter { 
                val saleCal = Calendar.getInstance().apply { timeInMillis = it.date }
                saleCal.get(Calendar.YEAR) == ty && 
                saleCal.get(Calendar.MONTH) == tm && 
                saleCal.get(Calendar.DAY_OF_MONTH) == td
            }
        } else {
            val startOfPeriod = Calendar.getInstance()
            when (filter) {
                "Hoy" -> {
                    startOfPeriod.set(Calendar.HOUR_OF_DAY, 0)
                    startOfPeriod.set(Calendar.MINUTE, 0)
                    startOfPeriod.set(Calendar.SECOND, 0)
                    startOfPeriod.set(Calendar.MILLISECOND, 0)
                }
                "Semana" -> startOfPeriod.add(Calendar.DAY_OF_YEAR, -7)
                "Mes" -> startOfPeriod.add(Calendar.MONTH, -1)
                "Año" -> startOfPeriod.add(Calendar.YEAR, -1)
                else -> return@combine records
            }
            records.filter { it.date >= startOfPeriod.timeInMillis }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    fun setSalesFilter(filter: String) {
        _customDate.value = null
        _salesFilter.value = filter
    }

    fun setCustomDateFilter(dateMillis: Long) {
        _customDate.value = dateMillis
        _salesFilter.value = "Personalizado"
    }

    val exchangeRate: StateFlow<Double> = repository.getUserConfigStream()
        .map { it?.lastExchangeRate ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0.0)

    val totalPeriodSales: StateFlow<Double> = salesRecords
        .map { it.sumOf { sale -> sale.totalAmount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0.0)

    fun addToProduction(recipeId: Int, quantity: Int = 1) {
        viewModelScope.launch {
            try {
                // 1. Discount ingredients from stock
                val recipeStream = repository.getIngredientsWithQuantityForRecipe(recipeId)
                val ingredientsNeeded = recipeStream.first()
                
                ingredientsNeeded.forEach { item ->
                    val ingredient = item.ingredient
                    val amountToSubtract = item.quantity * quantity
                    val newStock = (ingredient.stock - amountToSubtract).coerceAtLeast(0.0)
                    repository.updateIngredient(ingredient.copy(stock = newStock))
                }

                // 2. Add or Update Production Record
                val currentRecords = repository.getAllProductionRecordsStream().first()
                val existing = currentRecords.find { it.recipeId == recipeId }
                
                val now = System.currentTimeMillis()
                if (existing != null) {
                    repository.updateProductionRecord(existing.copy(
                        quantity = (existing.quantity + quantity).coerceAtLeast(0),
                        date = now
                    ))
                } else {
                    repository.insertProductionRecord(
                        ProductionRecord(recipeId = recipeId, quantity = quantity, date = now)
                    )
                }
            } catch (e: Exception) {
                // Fail-safe: even if ingredient stock calculation fails, try to add record
                try {
                    val now = System.currentTimeMillis()
                    repository.insertProductionRecord(
                        ProductionRecord(recipeId = recipeId, quantity = quantity, date = now)
                    )
                } catch (inner: Exception) {
                    inner.printStackTrace()
                }
            }
        }
    }

    fun convertToPortions(record: ProductionRecord, portions: Int) {
        viewModelScope.launch {
            if (record.quantity > 0) {
                repository.updateProductionRecord(
                    record.copy(
                        quantity = record.quantity - 1,
                        portionQuantity = record.portionQuantity + portions
                    )
                )
            }
        }
    }

    fun sellFromProduction(
        recordWithRecipe: ProductionRecordWithRecipe,
        paymentMethod: String,
        customerId: Int? = null,
        isPortion: Boolean = false,
        quantity: Int = 1
    ) {
        viewModelScope.launch {
            val record = recordWithRecipe.record
            val recipe = recordWithRecipe.recipe ?: return@launch
            
            val userConfig = repository.getUserConfigStream().firstOrNull()
            val rate = userConfig?.lastExchangeRate ?: 0.0
            val isCredit = paymentMethod == "Crédito (Fiado)"

            if (isPortion) {
                if (record.portionQuantity >= quantity) {
                    val newPortionQty = record.portionQuantity - quantity
                    repository.updateProductionRecord(record.copy(portionQuantity = newPortionQty))
                    
                    val portionPrice = recipe.portionSalePrice ?: 0.0
                    val totalPrice = portionPrice * quantity
                    
                    repository.insertSaleRecord(
                        SaleRecord(
                            recipeId = recipe.id,
                            recipeTitle = if (isCredit) "${recipe.title} (Porción - Crédito)" else "${recipe.title} (Porción)",
                            quantity = if (isCredit) 0 else quantity,
                            totalAmount = if (isCredit) 0.0 else totalPrice,
                            exchangeRate = rate,
                            totalAmountBs = if (isCredit) 0.0 else (totalPrice * rate),
                            paymentMethod = paymentMethod,
                            imagePath = recipe.imagePath,
                            customerId = customerId
                        )
                    )
                    
                    if (isCredit && customerId != null) {
                        repository.insertDebt(
                            Debt(
                                customerId = customerId,
                                amount = totalPrice,
                                remainingAmount = totalPrice,
                                concept = "Compra: ${quantity} porción(es) de ${recipe.title}",
                                date = System.currentTimeMillis(),
                                isPaid = false,
                                recipeId = recipe.id
                            )
                        )
                    }
                }
            } else {
                if (record.quantity >= quantity) {
                    val newQuantity = record.quantity - quantity
                    repository.updateProductionRecord(record.copy(quantity = newQuantity))
                    
                    val salePrice = recipe.manualSalePrice ?: 0.0
                    val totalPrice = salePrice * quantity
                    
                    repository.insertSaleRecord(
                        SaleRecord(
                            recipeId = recipe.id,
                            recipeTitle = if (isCredit) "${recipe.title} (Crédito)" else recipe.title,
                            quantity = if (isCredit) 0 else quantity,
                            totalAmount = if (isCredit) 0.0 else totalPrice,
                            exchangeRate = rate,
                            totalAmountBs = if (isCredit) 0.0 else (totalPrice * rate),
                            paymentMethod = paymentMethod,
                            imagePath = recipe.imagePath,
                            customerId = customerId
                        )
                    )

                    if (isCredit && customerId != null) {
                        repository.insertDebt(
                            Debt(
                                customerId = customerId,
                                amount = totalPrice,
                                remainingAmount = totalPrice,
                                concept = "Compra: ${quantity} ${recipe.title}",
                                date = System.currentTimeMillis(),
                                isPaid = false,
                                recipeId = recipe.id
                            )
                        )
                    }
                }
            }
        }
    }

    fun checkout(
        items: List<CartItem>,
        paymentMethod: String,
        customerId: Int? = null
    ) {
        viewModelScope.launch {
            val userConfig = repository.getUserConfigStream().firstOrNull()
            val rate = userConfig?.lastExchangeRate ?: 0.0
            val isCredit = paymentMethod == "Crédito (Fiado)"

            items.forEach { cartItem ->
                // 1. Get current production record
                val records = repository.getAllProductionRecordsStream().first()
                val record = records.find { it.recipeId == cartItem.recipeId } ?: return@forEach

                // 2. Deduct stock
                if (cartItem.isPortion) {
                    if (record.portionQuantity >= cartItem.quantity) {
                        repository.updateProductionRecord(record.copy(portionQuantity = record.portionQuantity - cartItem.quantity))
                    } else {
                        return@forEach // Not enough stock
                    }
                } else {
                    if (record.quantity >= cartItem.quantity) {
                        repository.updateProductionRecord(record.copy(quantity = record.quantity - cartItem.quantity))
                    } else {
                        return@forEach // Not enough stock
                    }
                }

                // 3. Create Sale Record
                val totalPrice = cartItem.price * cartItem.quantity
                val titleSuffix = if (cartItem.isPortion) " (Porción)" else ""
                val creditSuffix = if (isCredit) " - Crédito" else ""
                
                repository.insertSaleRecord(
                    SaleRecord(
                        recipeId = cartItem.recipeId,
                        recipeTitle = "${cartItem.title}$titleSuffix$creditSuffix",
                        quantity = if (isCredit) 0 else cartItem.quantity,
                        totalAmount = if (isCredit) 0.0 else totalPrice,
                        exchangeRate = rate,
                        totalAmountBs = if (isCredit) 0.0 else (totalPrice * rate),
                        paymentMethod = paymentMethod,
                        imagePath = cartItem.imagePath,
                        customerId = customerId
                    )
                )

                // 4. Handle Debt if Credit
                if (isCredit && customerId != null) {
                    repository.insertDebt(
                        Debt(
                            customerId = customerId,
                            amount = totalPrice,
                            remainingAmount = totalPrice,
                            concept = "Compra Carrito: ${cartItem.quantity} ${cartItem.title}$titleSuffix",
                            date = System.currentTimeMillis(),
                            isPaid = false,
                            recipeId = cartItem.recipeId
                        )
                    )
                }
            }
        }
    }
}

data class ProductionRecordWithRecipe(
    val record: ProductionRecord,
    val recipe: Recipe?
)

data class CartItem(
    val recipeId: Int,
    val title: String,
    val price: Double,
    val quantity: Int,
    val isPortion: Boolean,
    val imagePath: String? = null
)
