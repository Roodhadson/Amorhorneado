package com.example.amorhorneado.ui.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.amorhorneado.data.Customer
import com.example.amorhorneado.data.SaleRecord
import com.example.amorhorneado.ui.components.PriceInBs
import com.example.amorhorneado.ui.theme.BakeryOrange
import com.example.amorhorneado.ui.theme.BakeryTextGold
import com.example.amorhorneado.ui.theme.CriticalRed
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    exchangeRate: Double,
    onProfileClick: () -> Unit,
    onManageInventory: () -> Unit,
    onEditIngredient: (Int) -> Unit,
    onViewRecipeDetails: (Int) -> Unit,
    onManageRecipes: (Int) -> Unit,
    onUpdateExchangeRate: () -> Unit,
    onFiadosClick: () -> Unit,
    onViewSalesSummary: (Boolean) -> Unit,
    onRefreshLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.dashboardUiState.collectAsState()
    val userConfig by viewModel.userConfig.collectAsState()
    val weather by viewModel.weatherState.collectAsState()
    
    var showTopProductDetails by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1.3f).clickable { onProfileClick() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp).clip(CircleShape).background(BakeryOrange.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (userConfig.profileImagePath != null) {
                                AsyncImage(model = userConfig.profileImagePath, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            } else {
                                Text("🎂", fontSize = 24.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = userConfig.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                            Text("Perfil", style = MaterialTheme.typography.bodySmall, color = BakeryOrange)
                        }
                    }

                    Column(modifier = Modifier.weight(1.5f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🇻🇪 Tasa", fontSize = 10.sp, color = Color.Gray)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = String.format(Locale.getDefault(), "%.2f", exchangeRate), fontWeight = FontWeight.ExtraBold, color = Color(0xFFFF2C2C), fontSize = 32.sp)
                            IconButton(onClick = onUpdateExchangeRate, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text("Bs/$", fontSize = 10.sp, color = Color.Gray)
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onRefreshLocation() },
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(weather.temp, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(weather.icon, fontSize = 18.sp)
                        }
                        Text(weather.condition, fontSize = 10.sp, color = Color.Gray)
                        Text("📍 Actualizar", fontSize = 8.sp, color = BakeryOrange.copy(alpha = 0.6f))
                    }
                }
            }

            // Sales Indicators
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SalesIndicatorCard(
                        title = "Ventas Mensuales",
                        amount = uiState.monthlySales,
                        goal = uiState.monthlyGoal,
                        percentage = uiState.monthlyPercentage,
                        modifier = Modifier.weight(1f).clickable { onViewSalesSummary(true) }
                    )
                    SalesIndicatorCard(
                        title = "Ventas Semanales",
                        amount = uiState.weeklySales,
                        goal = uiState.weeklyGoal,
                        percentage = uiState.weeklyPercentage,
                        modifier = Modifier.weight(1f).clickable { onViewSalesSummary(false) }
                    )
                }
            }

            // Fiados Button
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onFiadosClick() },
                    colors = CardDefaults.cardColors(containerColor = BakeryOrange.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = BakeryOrange, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Fiados y Cuentas", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    }
                }
            }

            // Grafica Lineal
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Ventas de los Últimos 7 Días", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(20.dp))
                        SalesLineChart(
                            data = uiState.dailySalesData,
                            amounts = uiState.dailySalesAmounts,
                            exchangeRates = uiState.dailyExchangeRates,
                            modifier = Modifier.fillMaxWidth().height(130.dp)
                        )
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            uiState.dailySalesLabels.forEach { label -> Text(text = label, color = Color.Gray, fontSize = 10.sp) }
                        }
                    }
                }
            }

            // Producto Top
            uiState.topProduct?.let { top ->
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTopProductDetails = true }
                            .border(
                                width = 2.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(BakeryTextGold, Color.White, BakeryTextGold)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(90.dp).clip(RoundedCornerShape(12.dp)).background(Color.DarkGray)) {
                                if (top.imagePath != null) {
                                    AsyncImage(model = top.imagePath, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                } else {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = BakeryOrange, modifier = Modifier.align(Alignment.Center))
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = BakeryTextGold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("PRODUCTO TOP", style = MaterialTheme.typography.labelSmall, color = BakeryTextGold, fontWeight = FontWeight.Bold)
                                }
                                Text(top.title, fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 18.sp)
                                Text("${top.totalSold} unidades vendidas", color = Color.Gray, fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(String.format(Locale.getDefault(), "$%.2f", top.price), color = BakeryOrange, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BakeryTextGold)
                                        .clickable { 
                                            onViewRecipeDetails(top.id) 
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "DETALLES", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }
            }

            // Alertas
            if (uiState.lowStockIngredients.isNotEmpty() || uiState.lowStockRecipes.isNotEmpty()) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = CriticalRed, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Alertas de Stock Bajo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                items(uiState.lowStockIngredients) { ingredient ->
                    StockAlertCard(name = ingredient.name, status = "Quedan ${ingredient.stock} ${ingredient.unit.split(" ").last()}", onManage = { onEditIngredient(ingredient.id) })
                }
                items(uiState.lowStockRecipes) { recipe ->
                    StockAlertCard(name = recipe.title, status = "Stock en producción bajo", onManage = { onManageRecipes(recipe.id) })
                }
            }

            item { Text(text = "Inventario de Productos Listos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White) }
            items(uiState.productionData) { info ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)).background(Color.DarkGray)) {
                                if (info.imagePath != null) {
                                    AsyncImage(model = info.imagePath, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                } else {
                                    Text("📦", modifier = Modifier.align(Alignment.Center), fontSize = 24.sp)
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(text = info.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Text(text = info.quantityDisplay, color = BakeryOrange, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                    }
                }
            }
        }
    }

    if (showTopProductDetails && uiState.topProduct != null) {
        TopProductDetailsDialog(
            topProduct = uiState.topProduct!!,
            sales = uiState.topProductSales,
            customers = uiState.customers,
            onDismiss = { showTopProductDetails = false },
            onViewRecipeDetails = { 
                showTopProductDetails = false
                onViewRecipeDetails(it) 
            }
        )
    }
}

@Composable
fun TopProductDetailsDialog(
    topProduct: com.example.amorhorneado.ui.dashboard.TopProduct,
    sales: List<SaleRecord>,
    customers: List<Customer>,
    onDismiss: () -> Unit,
    onViewRecipeDetails: (Int) -> Unit
) {
    val totalRevenue = sales.sumOf { it.totalAmount }
    val sdf = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Ventas: ${topProduct.title}", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray) }
                }

                Spacer(Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onViewRecipeDetails(topProduct.id) },
                    colors = CardDefaults.cardColors(containerColor = BakeryOrange.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Recaudación Total del Producto", color = BakeryOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("$${String.format(Locale.getDefault(), "%.2f", totalRevenue)}", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Black)
                        val totalRevenueBs = sales.sumOf { it.totalAmountBs }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "REF: ${String.format(Locale.getDefault(), "%.2f", totalRevenueBs)} Bs.",
                                color = Color(0xFFD32F2F),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("VER DETALLES DEL PRODUCTO", color = BakeryTextGold, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }

                Spacer(Modifier.height(20.dp))

                Text("Historial de Compradores", color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(sales) { sale ->
                        val customer = customers.find { it.id == sale.customerId }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = customer?.name ?: "Venta Directa",
                                    color = if (customer != null) Color.White else BakeryOrange,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(text = sdf.format(Date(sale.date)), color = Color.Gray, fontSize = 11.sp)
                                    Surface(
                                        color = Color.White.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "Tasa: ${String.format(Locale.getDefault(), "%.2f", sale.exchangeRate)}",
                                            color = Color.LightGray,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                PriceInBs(priceInUsd = sale.totalAmount, exchangeRate = sale.exchangeRate)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (sale.quantity > 0) "x${sale.quantity}" else "Pago",
                                    color = BakeryTextGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(text = "$${String.format(Locale.getDefault(), "%.2f", sale.totalAmount)}", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SalesIndicatorCard(title: String, amount: Double, goal: Double, percentage: Double, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = title, style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontSize = 11.sp)
                val isPositive = percentage >= 0
                Surface(color = if (isPositive) Color(0xFF2E7D32).copy(alpha = 0.8f) else CriticalRed.copy(alpha = 0.8f), shape = RoundedCornerShape(12.dp)) {
                    Text(text = "${if (isPositive) "+" else ""}${String.format(Locale.getDefault(), "%.1f", percentage)}%", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "$${String.format(Locale.getDefault(), "%,.2f", amount)}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Meta: $${String.format(Locale.getDefault(), "%,.2f", goal)}", style = MaterialTheme.typography.labelSmall, color = BakeryOrange.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(8.dp))
            val progress = if (goal > 0) (amount / goal).toFloat().coerceIn(0f, 1f) else 0f
            Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(Color.DarkGray.copy(alpha = 0.5f))) {
                Box(modifier = Modifier.fillMaxWidth(progress).fillMaxHeight().clip(CircleShape).background(if (progress >= 1f) Color.Green else BakeryOrange))
            }
        }
    }
}

@Composable
fun SalesLineChart(data: List<Float>, amounts: List<Double>, exchangeRates: List<Double>, modifier: Modifier = Modifier) {
    val lineColor = Color(0xFF4CAF50)
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
    val subLabelStyle = TextStyle(color = Color.LightGray, fontSize = 9.sp, fontWeight = FontWeight.Medium)
    var selectedPointIndex by remember { mutableStateOf(-1) }
    
    val maxIndex = data.indices.maxByOrNull { data[it] } ?: -1

    Canvas(modifier = modifier.pointerInput(Unit) {
        detectTapGestures { offset ->
            val width = size.width
            val spacing = width / (data.size - 1).coerceAtLeast(1)
            val threshold = 25.dp.toPx()
            var closestIndex = -1
            var minDistanceFound = Float.MAX_VALUE
            data.forEachIndexed { index, _ ->
                val pointX = index * spacing
                val distance = kotlin.math.abs(offset.x - pointX)
                if (distance < threshold && distance < minDistanceFound) {
                    minDistanceFound = distance
                    closestIndex = index
                }
            }
            selectedPointIndex = if (selectedPointIndex == closestIndex) -1 else closestIndex
        }
    }) {
        if (data.isEmpty()) return@Canvas
        val width = size.width
        val height = size.height
        val verticalPadding = 25.dp.toPx()
        val chartHeight = height - verticalPadding
        val spacing = width / (data.size - 1).coerceAtLeast(1)
        
        val points = data.mapIndexed { index, value -> 
            Offset(index * spacing, height - (value * (chartHeight - 10.dp.toPx())) - 5.dp.toPx()) 
        }

        for (i in 0..3) {
            val y = height * i / 3
            drawLine(Color.Gray.copy(alpha = 0.08f), Offset(0f, y), Offset(width, y), 1.dp.toPx())
        }

        if (points.size >= 2) {
            val cubicPath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 0 until points.size - 1) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val controlPoint1 = Offset(p0.x + (p1.x - p0.x) / 2f, p0.y)
                    val controlPoint2 = Offset(p0.x + (p1.x - p0.x) / 2f, p1.y)
                    cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
                }
            }
            val fillPath = Path().apply { addPath(cubicPath); lineTo(points.last().x, height); lineTo(points.first().x, height); close() }
            drawPath(fillPath, Brush.verticalGradient(listOf(lineColor.copy(alpha = 0.2f), Color.Transparent)))
            drawPath(cubicPath, lineColor, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
        
        points.forEachIndexed { index, point ->
            if (index == maxIndex && data[index] > 0f) {
                drawCircle(brush = Brush.radialGradient(colors = listOf(lineColor.copy(alpha = 0.4f), Color.Transparent), center = point, radius = 15.dp.toPx()), radius = 15.dp.toPx(), center = point)
                drawCircle(lineColor, radius = 5.dp.toPx(), center = point)
                drawCircle(Color.White, radius = 2.dp.toPx(), center = point)
            }
            if (index == selectedPointIndex) {
                drawLine(color = Color.White.copy(alpha = 0.2f), start = Offset(point.x, 0f), end = Offset(point.x, height), strokeWidth = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                drawCircle(Color.White, radius = 6.dp.toPx(), center = point)
                drawCircle(lineColor, radius = 4.dp.toPx(), center = point)
                
                val amountText = String.format(Locale.getDefault(), "$%.2f", amounts[index])
                val rateText = if (index < exchangeRates.size) "Tasa: ${String.format(Locale.getDefault(), "%.2f", exchangeRates[index])}" else ""
                
                val textLayoutResult = textMeasurer.measure(amountText, labelStyle)
                val subTextLayoutResult = textMeasurer.measure(rateText, subLabelStyle)
                
                val labelWidth = maxOf(textLayoutResult.size.width, subTextLayoutResult.size.width) + 16.dp.toPx()
                val labelHeight = textLayoutResult.size.height + subTextLayoutResult.size.height + 12.dp.toPx()
                
                var labelX = point.x - (labelWidth / 2)
                if (labelX < 0) labelX = 4.dp.toPx()
                if (labelX + labelWidth > width) labelX = width - labelWidth - 4.dp.toPx()
                val labelY = (point.y - labelHeight - 12.dp.toPx()).coerceAtLeast(4.dp.toPx())
                
                drawRoundRect(
                    color = BakeryOrange,
                    topLeft = Offset(labelX, labelY),
                    size = androidx.compose.ui.geometry.Size(labelWidth, labelHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                )
                
                drawText(
                    textMeasurer = textMeasurer,
                    text = amountText,
                    style = labelStyle,
                    topLeft = Offset(labelX + 8.dp.toPx(), labelY + 4.dp.toPx())
                )
                
                if (rateText.isNotEmpty()) {
                    drawText(
                        textMeasurer = textMeasurer,
                        text = rateText,
                        style = subLabelStyle,
                        topLeft = Offset(labelX + 8.dp.toPx(), labelY + textLayoutResult.size.height + 6.dp.toPx())
                    )
                }
            } else if (index != maxIndex) {
                drawCircle(lineColor, radius = 3.dp.toPx(), center = point)
                drawCircle(Color(0xFF2D2013), radius = 1.2.dp.toPx(), center = point)
            }
        }
    }
}

@Composable
fun StockAlertCard(name: String, status: String, onManage: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Inventory, contentDescription = null, tint = CriticalRed, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, fontWeight = FontWeight.Bold, color = Color.White)
                Text(text = status, style = MaterialTheme.typography.bodySmall, color = CriticalRed)
            }
            Button(onClick = onManage, colors = ButtonDefaults.buttonColors(containerColor = BakeryOrange.copy(alpha = 0.15f)), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp), shape = RoundedCornerShape(8.dp), modifier = Modifier.height(36.dp)) {
                Text("Gestionar", color = BakeryOrange, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
