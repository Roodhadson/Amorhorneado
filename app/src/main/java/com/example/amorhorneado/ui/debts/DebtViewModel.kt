package com.example.amorhorneado.ui.debts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amorhorneado.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DebtViewModel(private val repository: IngredientRepository) : ViewModel() {

    val debtsUiState: StateFlow<DebtsUiState> = combine(
        repository.getAllDebtsStream(),
        repository.getAllCustomersStream(),
        repository.getAllRecipesStream(),
        repository.getAllSalesRecordsStream()
    ) { debts, customers, recipes, sales ->
        val mappedDebts = debts.map { debt ->
            DebtDetails(
                debt = debt,
                customerName = customers.find { it.id == debt.customerId }?.name ?: "Desconocido",
                recipe = recipes.find { it.id == debt.recipeId }
            )
        }

        val payments = sales.filter { it.paymentMethod == "Abono" }.map { sale ->
            PaymentDetails(
                customerName = customers.find { it.id == sale.customerId }?.name ?: "Cliente",
                concept = sale.recipeTitle,
                amount = sale.totalAmount,
                date = sale.date,
                exchangeRate = sale.exchangeRate
            )
        }

        DebtsUiState(
            debts = mappedDebts,
            customers = customers.sortedByDescending { it.id }, // Newest first
            sales = sales,
            payments = payments
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DebtsUiState()
    )

    fun addDebt(customerId: Int, amount: Double, concept: String, recipeId: Int? = null) {
        viewModelScope.launch {
            repository.insertDebt(Debt(customerId = customerId, amount = amount, remainingAmount = amount, concept = concept, recipeId = recipeId))
        }
    }

    fun markAsPaid(debtDetails: DebtDetails, paymentMethod: String) {
        viewModelScope.launch {
            val userConfig = repository.getUserConfigStream().first()
            val rate = userConfig?.lastExchangeRate ?: 0.0
            
            val debt = debtDetails.debt
            val customerName = debtDetails.customerName
            
            // 1. Record the cash received
            repository.insertSaleRecord(
                SaleRecord(
                    recipeId = -1, // Financial transaction
                    recipeTitle = "$customerName (Crédito pagado)",
                    quantity = 0,
                    totalAmount = debt.remainingAmount,
                    exchangeRate = rate,
                    totalAmountBs = debt.remainingAmount * rate,
                    paymentMethod = paymentMethod,
                    customerId = debt.customerId
                )
            )

            // 2. Record the unit sale for Top Product ranking
            if (debt.recipeId != null && debt.recipeId > 0) {
                repository.insertSaleRecord(
                    SaleRecord(
                        recipeId = debt.recipeId,
                        recipeTitle = "${debtDetails.recipe?.title ?: debt.concept} (Liquidado - $customerName)",
                        quantity = 1,
                        totalAmount = 0.0,
                        exchangeRate = rate,
                        totalAmountBs = 0.0,
                        paymentMethod = "Crédito Finalizado",
                        customerId = debt.customerId
                    )
                )
            }

            repository.updateDebt(debt.copy(remainingAmount = 0.0, isPaid = true))
        }
    }

    fun makePartialPayment(debt: Debt, amountPaid: Double) {
        viewModelScope.launch {
            val userConfig = repository.getUserConfigStream().first()
            val rate = userConfig?.lastExchangeRate ?: 0.0
            
            val customer = debtsUiState.value.customers.find { it.id == debt.customerId }
            val customerName = customer?.name ?: "Cliente"
            val isFullyPaid = amountPaid >= debt.remainingAmount
            val recipe = debtsUiState.value.debts.find { it.debt.id == debt.id }?.recipe

            // 1. Record the cash received
            repository.insertSaleRecord(
                SaleRecord(
                    recipeId = -1,
                    recipeTitle = "${recipe?.title ?: debt.concept} - $customerName (Abono)",
                    quantity = 0,
                    totalAmount = amountPaid,
                    exchangeRate = rate,
                    totalAmountBs = amountPaid * rate,
                    paymentMethod = "Abono",
                    customerId = debt.customerId
                )
            )

            if (isFullyPaid) {
                // 2. Record the unit sale for Top Product ranking
                if (debt.recipeId != null && debt.recipeId > 0) {
                    repository.insertSaleRecord(
                        SaleRecord(
                            recipeId = debt.recipeId,
                            recipeTitle = "${recipe?.title ?: debt.concept} (Liquidado - $customerName)",
                            quantity = 1,
                            totalAmount = 0.0,
                            exchangeRate = rate,
                            totalAmountBs = 0.0,
                            paymentMethod = "Crédito Finalizado",
                            customerId = debt.customerId
                        )
                    )
                }
                repository.updateDebt(debt.copy(remainingAmount = 0.0, isPaid = true))
            } else {
                val newAmount = debt.remainingAmount - amountPaid
                repository.updateDebt(debt.copy(remainingAmount = newAmount))
            }
        }
    }

    fun makeTotalPartialPayment(customerId: Int, amountPaid: Double) {
        viewModelScope.launch {
            val userConfig = repository.getUserConfigStream().first()
            val rate = userConfig?.lastExchangeRate ?: 0.0
            
            val customer = debtsUiState.value.customers.find { it.id == customerId }
            val customerName = customer?.name ?: "Cliente"

            // 1. Record the total cash received
            repository.insertSaleRecord(
                SaleRecord(
                    recipeId = -1,
                    recipeTitle = "$customerName (Abono General)",
                    quantity = 0,
                    totalAmount = amountPaid,
                    exchangeRate = rate,
                    totalAmountBs = amountPaid * rate,
                    paymentMethod = "Abono",
                    customerId = customerId
                )
            )

            var remainingPayment = amountPaid
            val pendingDebts = repository.getDebtsForCustomerStream(customerId).first()
                .filter { !it.isPaid }
                .sortedBy { it.date } // FIFO: Pay oldest first

            for (debt in pendingDebts) {
                if (remainingPayment <= 0) break
                
                val recipe = repository.getRecipeStream(debt.recipeId ?: 0).first()

                if (remainingPayment >= debt.remainingAmount) {
                    remainingPayment -= debt.remainingAmount
                    
                    // 2. This product is fully paid - Record the unit for ranking
                    if (debt.recipeId != null && debt.recipeId > 0) {
                        repository.insertSaleRecord(
                            SaleRecord(
                                recipeId = debt.recipeId,
                                recipeTitle = "${recipe?.title ?: debt.concept} (Liquidado - $customerName)",
                                quantity = 1,
                                totalAmount = 0.0,
                                exchangeRate = rate,
                                totalAmountBs = 0.0,
                                paymentMethod = "Crédito Finalizado",
                                customerId = customerId
                            )
                        )
                    }
                    repository.updateDebt(debt.copy(remainingAmount = 0.0, isPaid = true))
                } else {
                    val newAmount = debt.remainingAmount - remainingPayment
                    repository.updateDebt(debt.copy(remainingAmount = newAmount))
                    remainingPayment = 0.0
                }
            }
        }
    }

    fun addPayment(debtId: Int, amount: Double) {
        viewModelScope.launch {
            val debtDetails = debtsUiState.value.debts.find { it.debt.id == debtId }
            debtDetails?.let {
                makePartialPayment(it.debt, amount)
            }
        }
    }

    fun deleteDebt(debt: Debt) {
        viewModelScope.launch {
            repository.deleteDebt(debt)
        }
    }
    
    fun addCustomer(name: String, phone: String, address: String) {
        viewModelScope.launch {
            repository.insertCustomer(Customer(name = name, phone = phone, address = address))
        }
    }

    fun updateCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.insertCustomer(customer)
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }
}

data class DebtsUiState(
    val debts: List<DebtDetails> = emptyList(),
    val customers: List<Customer> = emptyList(),
    val sales: List<SaleRecord> = emptyList(),
    val payments: List<PaymentDetails> = emptyList()
)

data class DebtDetails(
    val debt: Debt,
    val customerName: String,
    val recipe: Recipe? = null
)

data class PaymentDetails(
    val customerName: String,
    val concept: String,
    val amount: Double,
    val date: Long,
    val exchangeRate: Double
)
