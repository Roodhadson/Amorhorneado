package com.example.amorhorneado

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ProductionQuantityLimits
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.amorhorneado.ui.AppViewModelProvider
import com.example.amorhorneado.ui.CurrencyViewModel
import com.example.amorhorneado.ui.dashboard.DashboardScreen
import com.example.amorhorneado.ui.dashboard.DashboardViewModel
import com.example.amorhorneado.ui.dashboard.ProfileScreen
import com.example.amorhorneado.ui.dashboard.SalesSummaryScreen
import com.example.amorhorneado.ui.dashboard.SalesSummaryViewModel
import com.example.amorhorneado.ui.debts.DebtsScreen
import com.example.amorhorneado.ui.ingredients.AddIngredientScreen
import com.example.amorhorneado.ui.ingredients.IngredientsScreen
import com.example.amorhorneado.ui.ingredients.IngredientsViewModel
import com.example.amorhorneado.ui.production.ProductionCostScreen
import com.example.amorhorneado.ui.production.ProductionScreen
import com.example.amorhorneado.ui.production.ProductionViewModel
import com.example.amorhorneado.ui.raffle.RaffleScreen
import com.example.amorhorneado.ui.raffle.RaffleViewModel
import com.example.amorhorneado.ui.recipes.AddRecipeScreen
import com.example.amorhorneado.ui.recipes.RecipeDetailScreen
import com.example.amorhorneado.ui.recipes.RecipesScreen
import com.example.amorhorneado.ui.recipes.RecipesViewModel
import com.example.amorhorneado.ui.theme.AmorhorneadoTheme
import com.example.amorhorneado.ui.theme.BakeryOrange

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AmorhorneadoTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val currencyViewModel: CurrencyViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val dashboardViewModel: DashboardViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val productionViewModel: ProductionViewModel = viewModel(factory = AppViewModelProvider.Factory)
    val exchangeRate by currencyViewModel.exchangeRate.collectAsState()
    var showRateDialog by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute !in listOf("add_ingredient", "add_recipe", "settings", "debts", "raffle", "sales_summary/{isMonthly}")) {
                val backgroundColor = Color(0xFF2D2013)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(75.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    val barHeight = 52.dp
                    // Fondo personalizado con el hueco (Corte circular)
                    Canvas(modifier = Modifier.fillMaxWidth().height(barHeight)) {
                        val width = size.width
                        val height = size.height
                        val centerX = width / 2
                        val cornerRadius = 20.dp.toPx()
                        
                        // Parámetros de la curva central
                        val cutoutRadius = 40.dp.toPx() 
                        val curveDepth = 36.dp.toPx()  
                        val curveExtension = 30.dp.toPx() 

                        val path = Path().apply {
                            moveTo(0f, height)
                            lineTo(0f, cornerRadius)
                            
                            // Esquina superior izquierda
                            quadraticTo(0f, 0f, cornerRadius, 0f)
                            
                            // Línea hasta el inicio del redondeado del hueco
                            lineTo(centerX - cutoutRadius - curveExtension, 0f)
                            
                            // Curva S de entrada (Muy redondeada)
                            cubicTo(
                                centerX - cutoutRadius, 0f,
                                centerX - cutoutRadius, curveDepth,
                                centerX, curveDepth
                            )
                            
                            // Curva S de salida
                            cubicTo(
                                centerX + cutoutRadius, curveDepth,
                                centerX + cutoutRadius, 0f,
                                centerX + cutoutRadius + curveExtension, 0f
                            )
                            
                            // Línea hasta la esquina superior derecha
                            lineTo(width - cornerRadius, 0f)
                            quadraticTo(width, 0f, width, cornerRadius)
                            lineTo(width, height)
                            close()
                        }
                        drawPath(path = path, color = backgroundColor, style = Fill)
                    }

                    // Iconos de navegación
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(barHeight)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dashboard
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            IconButton(onClick = {
                                navController.navigate("dashboard") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }) {
                                Icon(
                                    Icons.Default.Dashboard, 
                                    contentDescription = null,
                                    tint = if (currentRoute == "dashboard") BakeryOrange else Color.Gray,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        
                        // Insumos
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            IconButton(onClick = {
                                navController.navigate("ingredients") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }) {
                                Icon(
                                    Icons.Default.Inventory, 
                                    contentDescription = null,
                                    tint = if (currentRoute == "ingredients") BakeryOrange else Color.Gray,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        // Espacio para el FAB central
                        Spacer(modifier = Modifier.weight(1.2f))

                        // Recetas
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            IconButton(onClick = {
                                navController.navigate("recipes") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }) {
                                Icon(
                                    Icons.Default.Cake, 
                                    contentDescription = null,
                                    tint = if (currentRoute == "recipes") BakeryOrange else Color.Gray,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        // Costos
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            IconButton(onClick = {
                                navController.navigate("settings") {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = if (currentRoute == "settings") BakeryOrange else Color.Gray,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }

                    // Botón de Producción (FAB)
                    FloatingActionButton(
                        onClick = {
                            navController.navigate("production") {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        containerColor = BakeryOrange,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-1).dp) // Sobresale por arriba
                            .size(50.dp),
                        elevation = FloatingActionButtonDefaults.elevation(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ProductionQuantityLimits,
                            contentDescription = "Producción",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (currentRoute == "ingredients") {
                FloatingActionButton(
                    onClick = { navController.navigate("add_ingredient") },
                    containerColor = BakeryOrange,
                    contentColor = Color.White
                ) { Icon(Icons.Default.Add, contentDescription = "Añadir Insumo") }
            }
            if (currentRoute == "recipes") {
                FloatingActionButton(
                    onClick = { navController.navigate("add_recipe") },
                    containerColor = BakeryOrange,
                    contentColor = Color.White
                ) { Icon(Icons.Default.Add, contentDescription = "Añadir Receta") }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    exchangeRate = exchangeRate,
                    onProfileClick = { navController.navigate("profile") },
                    onManageInventory = { navController.navigate("ingredients") },
                    onEditIngredient = { id -> navController.navigate("edit_ingredient/$id") },
                    onViewRecipeDetails = { id -> navController.navigate("recipe_details/$id") },
                    onManageRecipes = { id -> navController.navigate("recipes") },
                    onUpdateExchangeRate = { showRateDialog = true },
                    onFiadosClick = { navController.navigate("debts") },
                    onViewSalesSummary = { isMonthly -> 
                        navController.navigate("sales_summary/$isMonthly")
                    },
                    onRefreshLocation = { dashboardViewModel.fetchWeather(10.4806, -66.8983) }
                )
            }
            composable("profile") {
                ProfileScreen(
                    viewModel = dashboardViewModel,
                    exchangeRate = exchangeRate,
                    onBack = { navController.popBackStack() },
                    onFiadosClick = { navController.navigate("debts") }
                )
            }
            composable(
                route = "sales_summary/{isMonthly}",
                arguments = listOf(navArgument("isMonthly") { type = NavType.BoolType })
            ) { backStackEntry ->
                val isMonthly = backStackEntry.arguments?.getBoolean("isMonthly") ?: false
                val vm: SalesSummaryViewModel = viewModel(factory = AppViewModelProvider.Factory)
                SalesSummaryScreen(
                    viewModel = vm,
                    isMonthly = isMonthly,
                    exchangeRate = exchangeRate,
                    onBack = { navController.popBackStack() },
                    onViewRecipeDetails = { id -> navController.navigate("recipe_details/$id") }
                )
            }
            composable("raffle") {
                val vm: RaffleViewModel = viewModel(factory = AppViewModelProvider.Factory)
                RaffleScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("ingredients") {
                IngredientsScreen(
                    viewModel = viewModel(factory = AppViewModelProvider.Factory),
                    onEditIngredient = { id -> navController.navigate("edit_ingredient/$id") }
                )
            }
            composable("add_ingredient") {
                AddIngredientScreen(
                    viewModel = viewModel(factory = AppViewModelProvider.Factory),
                    onBack = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() }
                )
            }
            composable(
                route = "edit_ingredient/{ingredientId}",
                arguments = listOf(navArgument("ingredientId") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("ingredientId") ?: 0
                val vm: IngredientsViewModel = viewModel(factory = AppViewModelProvider.Factory)
                LaunchedEffect(id) { vm.loadIngredient(id) }
                AddIngredientScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() }
                )
            }
            composable("recipes") {
                val context = androidx.compose.ui.platform.LocalContext.current
                RecipesScreen(
                    viewModel = viewModel(factory = AppViewModelProvider.Factory),
                    dashboardViewModel = dashboardViewModel,
                    exchangeRate = exchangeRate,
                    onRecipeClick = { id -> navController.navigate("recipe_details/$id") },
                    onEditRecipe = { id -> navController.navigate("edit_recipe/$id") },
                    onAddToProduction = { id -> 
                        productionViewModel.addToProduction(id)
                        android.widget.Toast.makeText(context, "Receta agregada a producción", android.widget.Toast.LENGTH_SHORT).show()
                    }
                )
            }
            composable("add_recipe") {
                AddRecipeScreen(
                    viewModel = viewModel(factory = AppViewModelProvider.Factory),
                    exchangeRate = exchangeRate,
                    onBack = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() }
                )
            }
            composable(
                route = "edit_recipe/{recipeId}",
                arguments = listOf(navArgument("recipeId") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("recipeId") ?: 0
                val vm: RecipesViewModel = viewModel(factory = AppViewModelProvider.Factory)
                LaunchedEffect(id) { vm.loadRecipe(id) }
                AddRecipeScreen(
                    viewModel = vm,
                    exchangeRate = exchangeRate,
                    onBack = { navController.popBackStack() },
                    onSaveSuccess = { navController.popBackStack() }
                )
            }
            composable(
                route = "recipe_details/{recipeId}",
                arguments = listOf(navArgument("recipeId") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("recipeId") ?: 0
                val vm: RecipesViewModel = viewModel(factory = AppViewModelProvider.Factory)
                RecipeDetailScreen(
                    recipeId = id,
                    viewModel = vm,
                    exchangeRate = exchangeRate,
                    onBack = { navController.popBackStack() },
                    onEdit = { recipeId -> navController.navigate("edit_recipe/$recipeId") }
                )
            }
            composable("production") {
                ProductionScreen(
                    viewModel = productionViewModel,
                    exchangeRate = exchangeRate,
                    onRecordClick = { id -> navController.navigate("recipe_details/$id") }
                )
            }
            composable("settings") {
                ProductionCostScreen(
                    viewModel = viewModel(factory = AppViewModelProvider.Factory),
                    exchangeRate = exchangeRate
                )
            }
            composable("debts") {
                DebtsScreen(
                    viewModel = viewModel(factory = AppViewModelProvider.Factory),
                    exchangeRate = exchangeRate,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }

    if (showRateDialog) {
        ExchangeRateDialog(
            initialRate = exchangeRate,
            onConfirm = { currencyViewModel.updateExchangeRate(it) },
            onDismiss = { showRateDialog = false }
        )
    }
}

@Composable
fun ExchangeRateDialog(
    initialRate: Double,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var rateText by remember { mutableStateOf(initialRate.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Actualizar Tasa") },
        text = {
            OutlinedTextField(
                value = rateText,
                onValueChange = { rateText = it },
                label = { Text("Tasa (Bs/$)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        },
        confirmButton = {
            Button(onClick = {
                rateText.toDoubleOrNull()?.let { onConfirm(it) }
                onDismiss()
            }) { Text("Confirmar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
