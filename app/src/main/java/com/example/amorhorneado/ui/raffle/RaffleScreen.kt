package com.example.amorhorneado.ui.raffle

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.airbnb.lottie.compose.*
import com.example.amorhorneado.R
import com.example.amorhorneado.ui.theme.BakeryOrange
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaffleScreen(
    viewModel: RaffleViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSettings by remember { mutableStateOf(false) }
    
    val activeParticipants = uiState.participants.filter { it.isSelected }

    // 1. Cargar el archivo JSON
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.ruleta_anim))
    
    // 2. Estado que controla la animación
    var isPlaying by remember { mutableStateOf(false) }
    
    val progress by animateLottieCompositionAsState(
        composition = composition,
        isPlaying = isPlaying,
        restartOnPlay = true,
        iterations = 1, // Una ejecución por sorteo
        speed = 1f
    )

    // 3. Detectar cuando termina la animación
    LaunchedEffect(progress) {
        if (progress == 1f && isPlaying) {
            isPlaying = false
            viewModel.onAnimationFinished() // Muestra el confeti y el ganador
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Sorteo Amorhorneado", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { if (!isPlaying) onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = BakeryOrange)
                    }
                },
                actions = {
                    IconButton(onClick = { if (!isPlaying) showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = BakeryOrange)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {
            
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (activeParticipants.isNotEmpty()) {
                    
                    Box(contentAlignment = Alignment.Center) {
                        // La Ruleta de Lottie
                        LottieAnimation(
                            composition = composition,
                            progress = { progress },
                            modifier = Modifier.size(400.dp)
                        )

                        // Capa de Texto (Compose)
                        val textMeasurer = rememberTextMeasurer()
                        Canvas(modifier = Modifier.size(350.dp).graphicsLayer {
                            // Sincronizamos la rotación del texto con el progreso del Lottie
                            // 1080 grados son 3 vueltas completas
                            val sweepAngle = 360f / activeParticipants.size
                            val targetRotation = if (uiState.targetWinnerIndex != -1) {
                                -(uiState.targetWinnerIndex * sweepAngle)
                            } else 0f
                            
                            rotationZ = (progress * 1080f) + targetRotation
                        }) {
                            val sweepAngle = 360f / activeParticipants.size
                            val radius = size.minDimension / 2
                            val center = Offset(size.width / 2, size.height / 2)

                            activeParticipants.forEachIndexed { index, participant ->
                                val startAngle = index * sweepAngle - 90f
                                rotate(startAngle + sweepAngle / 2 + 90f) {
                                    val textLayout = textMeasurer.measure(
                                        participant.name.take(10),
                                        style = TextStyle(
                                            color = Color.White, 
                                            fontWeight = FontWeight.Bold, 
                                            fontSize = 14.sp,
                                            shadow = Shadow(Color.Black, Offset(2f, 2f), 4f)
                                        )
                                    )
                                    drawText(
                                        textLayout,
                                        topLeft = Offset(
                                            center.x - textLayout.size.width / 2,
                                            center.y - radius + 40.dp.toPx()
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(50.dp))

                    // Botón de inicio
                    if (!isPlaying && uiState.winner == null) {
                        Button(
                            onClick = {
                                viewModel.requestSpin()
                                isPlaying = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BakeryOrange),
                            enabled = activeParticipants.size >= 2,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .height(60.dp)
                                .width(240.dp)
                                .shadow(12.dp, RoundedCornerShape(16.dp))
                        ) {
                            Icon(Icons.Default.Casino, contentDescription = null)
                            Spacer(Modifier.width(12.dp))
                            Text("¡GIRAR RULETA!", fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }
                    }
                } else {
                    Text("Configura los participantes en la tuerca", color = Color.Gray)
                }
            }

            // Overlay de Ganador (Tu código de Konfetti)
            if (uiState.winner != null) {
                WinnerCelebration(
                    winnerName = uiState.winner!!.name, 
                    onDismiss = { viewModel.clearWinner() }
                )
            }
        }
    }

    if (showSettings) {
        RaffleSettingsDialog(
            participants = uiState.participants,
            onToggle = { viewModel.toggleParticipantSelection(it) },
            onDismiss = { showSettings = false }
        )
    }
}

@Composable
fun WinnerCelebration(winnerName: String, onDismiss: () -> Unit) {
    val party = remember {
        Party(
            speed = 12f,
            maxSpeed = 45f,
            damping = 0.9f,
            spread = 360,
            colors = listOf(0xFFFFA726.toInt(), 0xFFFFFFFF.toInt()),
            emitter = Emitter(duration = 2, TimeUnit.SECONDS).perSecond(60),
            position = Position.Relative(0.5, 0.3)
        )
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)).clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        KonfettiView(modifier = Modifier.fillMaxSize(), parties = listOf(party))
        
        Card(
            modifier = Modifier.padding(24.dp).shadow(30.dp, RoundedCornerShape(32.dp)),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = BakeryOrange)
        ) {
            Column(modifier = Modifier.padding(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color.White, modifier = Modifier.size(100.dp))
                Spacer(modifier = Modifier.height(20.dp))
                Text("¡TENEMOS UN GANADOR!", color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(winnerName.uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 34.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))
                Text("Toca para cerrar", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun RaffleSettingsDialog(
    participants: List<RaffleParticipant>,
    onToggle: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Participantes", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray) }
                }
                Spacer(Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(participants) { participant ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(12.dp)).clickable { onToggle(participant.id) },
                            color = if (participant.isSelected) BakeryOrange.copy(alpha = 0.15f) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = participant.isSelected, onCheckedChange = { onToggle(participant.id) }, colors = CheckboxDefaults.colors(checkedColor = BakeryOrange))
                                Text(participant.name, modifier = Modifier.padding(start = 12.dp), color = Color.White)
                            }
                        }
                    }
                }
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(54.dp).padding(top = 16.dp), colors = ButtonDefaults.buttonColors(containerColor = BakeryOrange), shape = RoundedCornerShape(16.dp)) {
                    Text("LISTO", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
