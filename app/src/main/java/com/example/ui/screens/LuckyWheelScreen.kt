package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.ui.theme.PositiveGreen
import com.example.ui.viewmodel.InvestmentViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LuckyWheelScreen(
    viewModel: InvestmentViewModel,
    user: User
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // 8 Wedges on the Lucky Wheel
    val rewards = listOf(
        500.0,
        1000.0,
        2000.0,
        1500.0,
        5000.0,
        2500.0,
        3000.0,
        10000.0
    )
    
    val colors = listOf(
        Color(0xFF3F51B5),
        Color(0xFFE91E63),
        Color(0xFF9C27B0),
        Color(0xFF00BCD4),
        Color(0xFF4CAF50),
        Color(0xFFFF9800),
        Color(0xFFFFC107),
        Color(0xFF9E9E9E)
    )

    // Verification of Eligibility: Can spin once a calendar month
    val hasSpunThisMonth = remember(user.lastSpinTimestamp) {
        if (user.lastSpinTimestamp == 0L) {
            false
        } else {
            val lastDate = Calendar.getInstance().apply { timeInMillis = user.lastSpinTimestamp }
            val currentDate = Calendar.getInstance()
            val lastMonth = lastDate.get(Calendar.MONTH)
            val lastYear = lastDate.get(Calendar.YEAR)
            val currentMonth = currentDate.get(Calendar.MONTH)
            val currentYear = currentDate.get(Calendar.YEAR)
            lastMonth == currentMonth && lastYear == currentYear
        }
    }

    var isSpinning by remember { mutableStateOf(false) }
    var rotationAngle by remember { mutableStateOf(0f) }
    var selectedRewardIndex by remember { mutableStateOf<Int?>(null) }
    var rewardEarnedText by remember { mutableStateOf<String?>(null) }
    var showCongratulatoryDialog by remember { mutableStateOf(false) }

    // Spin animation configuration
    val animatedRotation by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = tween(
            durationMillis = 4000,
            easing = CubicBezierEasing(0.1f, 0.8f, 0.2f, 1.0f)
        ),
        finishedListener = {
            isSpinning = false
            selectedRewardIndex?.let { index ->
                val wonReward = rewards[index]
                rewardEarnedText = "${String.format(Locale.US, "%,.0f", wonReward)} FCFA"
                showCongratulatoryDialog = true
                viewModel.spinLuckyWheel(wonReward)
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen identity header
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Invexa Lucky Wheel Pin",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Test your financial fortune! Spin the interactive bonus layout for monthly rewards.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // Eligibility Banner Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (hasSpunThisMonth) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Eligibility",
                    tint = if (hasSpunThisMonth) MaterialTheme.colorScheme.outline
                    else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (hasSpunThisMonth) "Already Spun This Month" else "Lucky Spin Available!",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (hasSpunThisMonth) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (hasSpunThisMonth) {
                            val nextAllowedMonth = Calendar.getInstance().apply { 
                                timeInMillis = user.lastSpinTimestamp
                                add(Calendar.MONTH, 1)
                                set(Calendar.DAY_OF_MONTH, 1)
                            }
                            val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                            "You earned rewards this month. Next spin unlocks on ${sdf.format(nextAllowedMonth.time)}."
                        } else {
                            "You are fully eligible! Spin the wheel below to unlock an instant credit payout directly to your wallet account."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Spin Dial Frame Container
        Box(
            modifier = Modifier
                .size(290.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surface
                        )
                    ),
                    shape = CircleShape
                )
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            // Interactive Rotating Canvas drawing the wheel wedges
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationZ = animatedRotation }
                    .testTag("interactive_spin_wheel_canvas")
            ) {
                val canvasSize = size
                val center = Offset(canvasSize.width / 2, canvasSize.height / 2)
                val radius = canvasSize.width / 2
                val sweepAngle = 360f / rewards.size

                for (i in rewards.indices) {
                    val startAngle = i * sweepAngle
                    // Draw slice
                    drawArc(
                        color = colors[i],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        size = Size(canvasSize.width, canvasSize.height)
                    )

                    // Draw text representation inside sector (drawn geometrically using trigonometry)
                    val textAngleRad = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
                    val textRadius = radius * 0.65f
                    val x = center.x + textRadius * cos(textAngleRad).toFloat()
                    val y = center.y + textRadius * sin(textAngleRad).toFloat()

                    // Optional radial divider lines
                    val endX = center.x + radius * cos(Math.toRadians(startAngle.toDouble())).toFloat()
                    val endY = center.y + radius * sin(Math.toRadians(startAngle.toDouble())).toFloat()
                    drawLine(
                        color = Color.White.copy(alpha = 0.4f),
                        start = center,
                        end = Offset(endX, endY),
                        strokeWidth = 2.dp.toPx()
                    )
                }

                // Inner central ring
                drawCircle(
                    color = Color.White,
                    radius = 28.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = Color.DarkGray,
                    radius = 24.dp.toPx(),
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Stationary Pointer arrow indicating selected wedge (Wedge pointing is top: 270f offset)
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    modifier = Modifier
                        .size(width = 24.dp, height = 32.dp)
                        .graphicsLayer { translationY = -14f },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(bottomStartPercent = 80, bottomEndPercent = 80)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("▼", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Spin CTA Core button
            Button(
                onClick = {
                    if (!isSpinning && !hasSpunThisMonth) {
                        isSpinning = true
                        // Select random sector
                        val targetIndex = (rewards.indices).random()
                        selectedRewardIndex = targetIndex
                        
                        // Sector angle math: pointer is at top (270 degrees). 
                        // To align pointer with winning sector center (targetIndex * (360/8) + 22.5):
                        val sectorCenter = (targetIndex * (360f / rewards.size)) + ((360f / rewards.size) / 2)
                        // Target rotation turns full circles plus aligns pointer
                        val desiredRotation = 360f * 4 + (270f - sectorCenter)
                        rotationAngle = desiredRotation
                    }
                },
                modifier = Modifier
                    .size(64.dp)
                    .testTag("spin_action_cta_button"),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasSpunThisMonth) MaterialTheme.colorScheme.outline
                    else MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(0.dp),
                enabled = !isSpinning && !hasSpunThisMonth
            ) {
                Text(
                    text = "SPIN",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Payout wedges guide chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = "Casino Items",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Fortune Pin Wheel Sector Rewards",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                rewards.distinct().sorted().forEach { reward ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isJackpot = reward == 10000.0
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = if (isJackpot) PositiveGreen else MaterialTheme.colorScheme.outline,
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isJackpot) "🟢 Mega Payout Jackpot!" else "Standard Bonus Wedge",
                                fontSize = 11.sp,
                                color = if (isJackpot) PositiveGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isJackpot) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        Text(
                            text = "${String.format(Locale.US, "%,.0f", reward)} FCFA",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isJackpot) PositiveGreen else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }

    // Congratulatory success popup modal
    if (showCongratulatoryDialog) {
        AlertDialog(
            onDismissRequest = { showCongratulatoryDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.LocalAtm,
                    contentDescription = "Jackpot reward",
                    tint = PositiveGreen,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Congratulations!",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "You successfully spun the Invexa Fortune Wheel and secured a free deposit reward of:",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = rewardEarnedText ?: "",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = PositiveGreen
                        ),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "The amount was instantly credited to your wallet balance. Enjoy your complementary bonus!",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCongratulatoryDialog = false },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = PositiveGreen)
                ) {
                    Text("Awesome, Thank you!")
                }
            }
        )
    }
}
