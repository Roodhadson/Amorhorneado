package com.example.amorhorneado.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amorhorneado.data.*
import com.example.amorhorneado.network.WeatherApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class DashboardViewModel(private val repository: IngredientRepository) : ViewModel() {

    val userConfig: StateFlow<UserConfig> = repository.getUserConfigStream()
        .map { it ?: UserConfig() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), UserConfig())

    private val _weatherState = MutableStateFlow(WeatherInfo("--", "Cargando...", "☁️"))
    val weatherState: StateFlow<WeatherInfo> = _weatherState.asStateFlow()

    private var lastLat: Double = 10.4806 // Caracas Default
    private var lastLon: Double = -66.8983

    init {
        // Initial fetch with default location
        fetchWeather(lastLat, lastLon)
        
        // React to saved location changes
        viewModelScope.launch {
            userConfig.collect { config ->
                if (config.latitude != lastLat || config.longitude != lastLon) {
                    lastLat = config.latitude
                    lastLon = config.longitude
                    fetchWeather(lastLat, lastLon)
                }
            }
        }
    }

    fun updateLocation(lat: Double, lon: Double) {
        viewModelScope.launch {
            val currentConfig = userConfig.value
            repository.insertUserConfig(currentConfig.copy(latitude = lat, longitude = lon))
            fetchWeather(lat, lon)
        }
    }

    fun fetchWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            _weatherState.value = _weatherState.value.copy(condition = "Actualizando...")
            try {
                val response = WeatherApi.retrofitService.getWeather(lat, lon)
                _weatherState.value = WeatherInfo(
                    temp = "${response.current_weather.temperature.toInt()}°C",
                    condition = WeatherApi.getWeatherCondition(response.current_weather.weathercode),
                    icon = WeatherApi.getWeatherEmoji(response.current_weather.weathercode)
                )
            } catch (e: Exception) {
                _weatherState.value = WeatherInfo("--", "Sin conexión", "❓")
            }
        }
    }

    val dashboardUiState: StateFlow<DashboardUiState> = combine(
        repository.getAllSalesRecordsStream(),
        repository.getAllIngredientsStream(),
        repository.getAllRecipesStream(),
        repository.getAllProductionRecordsStream(),
        repository.getAllCustomersStream()
    ) { sales, ingredients, recipes, production, customers ->
        
        // 1. Weekly Sales and Chart Data
        val dailyTotals = mutableListOf<Double>()
        val dailyRates = mutableListOf<Double>()
        val dayLabels = mutableListOf<String>()
        val locale = Locale("es", "ES")
        
        for (i in 6 downTo 0) {
            val dayCalendar = Calendar.getInstance()
            dayCalendar.add(Calendar.DAY_OF_YEAR, -i)
            
            val dayName = dayCalendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, locale)
                ?.replaceFirstChar { it.uppercase() } ?: ""
            dayLabels.add(dayName)

            dayCalendar.set(Calendar.HOUR_OF_DAY, 0)
            dayCalendar.set(Calendar.MINUTE, 0)
            dayCalendar.set(Calendar.SECOND, 0)
            dayCalendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = dayCalendar.timeInMillis
            
            dayCalendar.set(Calendar.HOUR_OF_DAY, 23)
            dayCalendar.set(Calendar.MINUTE, 59)
            dayCalendar.set(Calendar.SECOND, 59)
            val endOfDay = dayCalendar.timeInMillis
            
            val salesOfDay = sales.filter { it.date in startOfDay..endOfDay }
            val totalForDay = salesOfDay.sumOf { it.totalAmount }
            val avgRateForDay = if (salesOfDay.isNotEmpty()) salesOfDay.map { it.exchangeRate }.average() else 0.0
            
            dailyTotals.add(totalForDay)
            dailyRates.add(avgRateForDay)
        }
        
        val weeklyTotal = dailyTotals.sum()

        // Previous Week
        val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        val prevWeeklyTotal = sales.filter { it.date in (weekAgo - (7 * 24 * 60 * 60 * 1000L))..weekAgo }.sumOf { it.totalAmount }
        val weeklyPercent = if (prevWeeklyTotal > 0) ((weeklyTotal - prevWeeklyTotal) / prevWeeklyTotal) * 100 else 0.0
        val weeklyGoal = (weeklyTotal * 1.2).coerceAtLeast(100.0)

        // 2. Monthly Sales
        val calMonth = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
        val monthlyTotal = sales.filter { it.date >= calMonth.timeInMillis }.sumOf { it.totalAmount }
        val monthlyGoal = (monthlyTotal * 1.2).coerceAtLeast(500.0)

        // 3. New: Inversion and Profit Calculation
        val weeklySalesList = sales.filter { it.date >= (System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)) }
        val weeklyInversion = weeklySalesList.sumOf { sale ->
            val recipe = recipes.find { it.id == sale.recipeId }
            (recipe?.baseCost ?: 0.0) * sale.quantity
        }
        val weeklyProfit = weeklyTotal - weeklyInversion

        val monthlySalesList = sales.filter { it.date >= calMonth.timeInMillis }
        val monthlyInversion = monthlySalesList.sumOf { sale ->
            val recipe = recipes.find { it.id == sale.recipeId }
            (recipe?.baseCost ?: 0.0) * sale.quantity
        }
        val monthlyProfit = monthlyTotal - monthlyInversion

        // 4. Top Product Calculation
        val topProductRecord = sales.filter { it.date >= weekAgo && it.recipeId > 0 && it.quantity > 0 }
            .groupBy { it.recipeId }
            .mapValues { entry -> entry.value.sumOf { it.quantity } }
            .maxByOrNull { it.value }

        val topProduct = topProductRecord?.let { (recipeId, totalSold) ->
            recipes.find { it.id == recipeId }?.let { recipe ->
                TopProduct(
                    id = recipe.id,
                    title = recipe.title,
                    totalSold = totalSold,
                    imagePath = recipe.imagePath,
                    price = recipe.manualSalePrice ?: 0.0
                )
            }
        }

        // 5. Sales History for Top Product
        val topProductSales = if (topProduct != null) {
            sales.filter { 
                it.recipeId == topProduct.id || 
                (it.recipeId == -1 && it.recipeTitle.contains(topProduct.title, ignoreCase = true)) 
            }.sortedByDescending { it.date }
        } else emptyList()

        DashboardUiState(
            weeklySales = weeklyTotal,
            monthlySales = monthlyTotal,
            weeklyInversion = weeklyInversion,
            weeklyProfit = weeklyProfit,
            monthlyInversion = monthlyInversion,
            monthlyProfit = monthlyProfit,
            weeklyPercentage = weeklyPercent,
            monthlyPercentage = 0.0,
            weeklyGoal = weeklyGoal,
            monthlyGoal = monthlyGoal,
            dailySalesData = calculateChartFractions(dailyTotals),
            dailySalesAmounts = dailyTotals,
            dailyExchangeRates = dailyRates,
            dailySalesLabels = dayLabels,
            topProduct = topProduct,
            topProductSales = topProductSales,
            customers = customers,
            lowStockIngredients = ingredients.filter { it.stock <= it.minStock },
            lowStockRecipes = recipes.filter { recipe ->
                val currentInProd = production.find { it.recipeId == recipe.id }?.quantity ?: 0
                currentInProd <= recipe.minStock
            },
            productionData = production.filter { it.quantity > 0 || it.portionQuantity > 0 }.mapNotNull { prod ->
                recipes.find { it.id == prod.recipeId }?.let { recipe ->
                    val quantityText = if (recipe.isPortionEnabled) {
                        if (prod.quantity > 0 && prod.portionQuantity > 0) {
                            "x${prod.quantity} + x${prod.portionQuantity}P"
                        } else if (prod.portionQuantity > 0) {
                            "x${prod.portionQuantity}P"
                        } else {
                            "x${prod.quantity}"
                        }
                    } else {
                        "x${prod.quantity}"
                    }
                    ProductionItem(
                        title = recipe.title,
                        quantity = prod.quantity,
                        quantityDisplay = quantityText,
                        imagePath = recipe.imagePath
                    )
                }
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DashboardUiState())

    fun updateUserConfig(name: String, imagePath: String?) {
        viewModelScope.launch { repository.insertUserConfig(userConfig.value.copy(name = name, profileImagePath = imagePath)) }
    }

    private fun calculateChartFractions(totals: List<Double>): List<Float> {
        val max = totals.maxOrNull()?.takeIf { it > 0 } ?: 1.0
        return totals.map { (it / max).toFloat() }
    }
}

data class WeatherInfo(val temp: String, val condition: String, val icon: String)

data class DashboardUiState(
    val weeklySales: Double = 0.0,
    val monthlySales: Double = 0.0,
    val weeklyInversion: Double = 0.0,
    val weeklyProfit: Double = 0.0,
    val monthlyInversion: Double = 0.0,
    val monthlyProfit: Double = 0.0,
    val weeklyPercentage: Double = 0.0,
    val monthlyPercentage: Double = 0.0,
    val weeklyGoal: Double = 0.0,
    val monthlyGoal: Double = 0.0,
    val dailySalesData: List<Float> = emptyList(),
    val dailySalesAmounts: List<Double> = emptyList(),
    val dailyExchangeRates: List<Double> = emptyList(),
    val dailySalesLabels: List<String> = emptyList(),
    val topProduct: TopProduct? = null,
    val topProductSales: List<SaleRecord> = emptyList(),
    val customers: List<Customer> = emptyList(),
    val lowStockIngredients: List<Ingredient> = emptyList(),
    val lowStockRecipes: List<Recipe> = emptyList(),
    val productionData: List<ProductionItem> = emptyList()
)

data class TopProduct(val id: Int, val title: String, val totalSold: Int, val imagePath: String?, val price: Double)
data class ProductionItem(
    val title: String,
    val quantity: Int,
    val portionQuantity: Int = 0,
    val quantityDisplay: String = "",
    val imagePath: String?
)
