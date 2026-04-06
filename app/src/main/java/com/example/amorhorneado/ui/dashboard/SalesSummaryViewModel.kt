package com.example.amorhorneado.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amorhorneado.data.*
import kotlinx.coroutines.flow.*
import java.util.*

class SalesSummaryViewModel(private val repository: IngredientRepository) : ViewModel() {

    private val _isMonthly = MutableStateFlow(false)
    val isMonthly: StateFlow<Boolean> = _isMonthly.asStateFlow()

    fun setPeriod(monthly: Boolean) {
        _isMonthly.value = monthly
    }

    val uiState: StateFlow<SalesSummaryUiState> = combine(
        repository.getAllSalesRecordsStream(),
        repository.getAllRecipesStream(),
        repository.getAllCustomersStream(),
        _isMonthly
    ) { sales, recipes, customers, isMonthly ->
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis
        
        if (isMonthly) {
            calendar.set(Calendar.DAY_OF_MONTH, 1)
        } else {
            calendar.add(Calendar.DAY_OF_YEAR, -6)
        }
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startDate = calendar.timeInMillis

        val filteredSales = sales.filter { it.date in startDate..endDate }.sortedByDescending { it.date }
        
        // Chart data
        val dailyTotals = mutableListOf<Double>()
        val dailyRates = mutableListOf<Double>()
        val dayLabels = mutableListOf<String>()
        val locale = Locale("es", "ES")
        
        val daysCount = if (isMonthly) {
            val cal = Calendar.getInstance()
            cal.get(Calendar.DAY_OF_MONTH)
        } else 7

        for (i in (daysCount - 1) downTo 0) {
            val dayCal = Calendar.getInstance()
            dayCal.add(Calendar.DAY_OF_YEAR, -i)
            
            val label = if (isMonthly) {
                dayCal.get(Calendar.DAY_OF_MONTH).toString()
            } else {
                dayCal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, locale)
                    ?.replaceFirstChar { it.uppercase() } ?: ""
            }
            dayLabels.add(label)

            dayCal.set(Calendar.HOUR_OF_DAY, 0)
            dayCal.set(Calendar.MINUTE, 0)
            dayCal.set(Calendar.SECOND, 0)
            dayCal.set(Calendar.MILLISECOND, 0)
            val start = dayCal.timeInMillis
            
            dayCal.set(Calendar.HOUR_OF_DAY, 23)
            dayCal.set(Calendar.MINUTE, 59)
            dayCal.set(Calendar.SECOND, 59)
            val end = dayCal.timeInMillis
            
            val salesOfDay = sales.filter { it.date in start..end }
            dailyTotals.add(salesOfDay.sumOf { it.totalAmount })
            dailyRates.add(if (salesOfDay.isNotEmpty()) salesOfDay.map { it.exchangeRate }.average() else 0.0)
        }

        // Top Product for this period
        val topProductRecord = filteredSales.filter { it.recipeId > 0 && it.quantity > 0 }
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

        // Sales History for Top Product in this period
        val topProductSales = if (topProduct != null) {
            filteredSales.filter { 
                it.recipeId == topProduct.id || 
                (it.recipeId == -1 && it.recipeTitle.contains(topProduct.title, ignoreCase = true)) 
            }
        } else emptyList()

        SalesSummaryUiState(
            title = if (isMonthly) "Resumen Mensual" else "Resumen Semanal",
            totalSales = filteredSales.sumOf { it.totalAmount },
            salesList = filteredSales,
            topProduct = topProduct,
            topProductSales = topProductSales,
            chartData = calculateChartFractions(dailyTotals),
            chartAmounts = dailyTotals,
            chartRates = dailyRates,
            chartLabels = dayLabels,
            customers = customers
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), SalesSummaryUiState())

    private fun calculateChartFractions(totals: List<Double>): List<Float> {
        val max = totals.maxOrNull()?.takeIf { it > 0 } ?: 1.0
        return totals.map { (it / max).toFloat() }
    }
}

data class SalesSummaryUiState(
    val title: String = "",
    val totalSales: Double = 0.0,
    val salesList: List<SaleRecord> = emptyList(),
    val topProduct: TopProduct? = null,
    val topProductSales: List<SaleRecord> = emptyList(),
    val chartData: List<Float> = emptyList(),
    val chartAmounts: List<Double> = emptyList(),
    val chartRates: List<Double> = emptyList(),
    val chartLabels: List<String> = emptyList(),
    val customers: List<Customer> = emptyList()
)
