package com.example.amorhorneado.ui

import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.amorhorneado.BakeryApplication
import com.example.amorhorneado.ui.dashboard.DashboardViewModel
import com.example.amorhorneado.ui.dashboard.SalesSummaryViewModel
import com.example.amorhorneado.ui.debts.DebtViewModel
import com.example.amorhorneado.ui.ingredients.IngredientsViewModel
import com.example.amorhorneado.ui.production.ProductionCostViewModel
import com.example.amorhorneado.ui.production.ProductionViewModel
import com.example.amorhorneado.ui.raffle.RaffleViewModel
import com.example.amorhorneado.ui.recipes.RecipesViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            IngredientsViewModel(bakeryApplication().ingredientRepository)
        }
        initializer {
            ProductionCostViewModel(bakeryApplication().ingredientRepository)
        }
        initializer {
            RecipesViewModel(bakeryApplication().ingredientRepository)
        }
        initializer {
            ProductionViewModel(bakeryApplication().ingredientRepository)
        }
        initializer {
            CurrencyViewModel(bakeryApplication().ingredientRepository)
        }
        initializer {
            DashboardViewModel(bakeryApplication().ingredientRepository)
        }
        initializer {
            DebtViewModel(bakeryApplication().ingredientRepository)
        }
        initializer {
            SalesSummaryViewModel(bakeryApplication().ingredientRepository)
        }
        initializer {
            RaffleViewModel(bakeryApplication().ingredientRepository)
        }
    }
}

fun CreationExtras.bakeryApplication(): BakeryApplication =
    (this[AndroidViewModelFactory.APPLICATION_KEY] as BakeryApplication)
