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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.AppTheme
import com.example.ui.viewmodel.NihongoViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun CoverScreen(viewModel: NihongoViewModel) {
    val currentTheme by viewModel.currentTheme.collectAsState()
    
    // Theme-compatible color configurations for custom vector drawing
    val (primaryColor, accentColor, bgColor, sunColor, toriiColor) = when (currentTheme) {
        AppTheme.SAKURA_PINK -> listOf(
            Color(0xFFFF8DA1), // Primary Pink
            Color(0xFFFFB7C5), // Light Sakura
            Color(0xFFFFF0F2), // BG White-Pink
            Color(0xFFFF5271), // Sun Deep
            Color(0xFF8B263E)  // Dark torii red
        )
        AppTheme.ZEN_MATCHA -> listOf(
            Color(0xFF4CAF50), // Matcha Green
            Color(0xFF81C784), // Light Sage
            Color(0xFFF1F8E9), // BG Off-white green
            Color(0xFFFFB74D), // Sun Golden
            Color(0xFF2E7D32)  // Dark pine green
        )
        AppTheme.SAMURAI_DARK -> listOf(
            Color(0xFFE50914), // Samurai Red
            Color(0xFF303030), // Charcoal Gray
            Color(0xFF121212), // Deep Charcoal BG
            Color(0xFF9E0B0B), // Blood Moon Sun
            Color(0xFFF5F5F5)  // Light structural Torii
        )
        AppTheme.CLASSIC_INDIGO -> listOf(
            Color(0xFF3F51B5), // Indigo
            Color(0xFF7986CB), // Lavender Blue
            Color(0xFFF5F6FA), // Silver Blue BG
            Color(0xFFFF5722), // Deep Sunset Sun
            Color(0xFF1E272C)  // Dark Iron
        )
    }

    // Animation states for welcoming zoom & fade effects
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(bgColor, bgColor.copy(alpha = 0.85f))
                )
            )
            .testTag("cover_screen")
    ) {
        // 1. Dynamic Vector background representing MT FUJI, SUN, and TORII GATE
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .align(Alignment.TopCenter)
        ) {
            val width = size.width
            val height = size.height

            // A. Draw Rising Sun
            drawCircle(
                color = sunColor.copy(alpha = 0.15f),
                radius = 160.dp.toPx(),
                center = Offset(width / 2, height * 0.45f)
            )
            drawCircle(
                color = sunColor.copy(alpha = 0.8f),
                radius = 70.dp.toPx(),
                center = Offset(width / 2, height * 0.45f)
            )

            // B. Draw Mt. Fuji silhouette
            val fujiPath = Path().apply {
                moveTo(0f, height)
                lineTo(width * 0.15f, height)
                cubicTo(
                    width * 0.35f, height * 0.9f,
                    width * 0.4f, height * 0.55f,
                    width * 0.5f, height * 0.55f
                )
                cubicTo(
                    width * 0.6f, height * 0.55f,
                    width * 0.65f, height * 0.9f,
                    width * 0.85f, height)
                lineTo(width, height)
                close()
            }
            drawPath(
                path = fujiPath,
                color = accentColor.copy(alpha = 0.25f)
            )

            // C. Draw snow-cap of Mt. Fuji
            val snowCapPath = Path().apply {
                moveTo(width * 0.46f, height * 0.63f)
                cubicTo(
                    width * 0.48f, height * 0.56f,
                    width * 0.49f, height * 0.55f,
                    width * 0.5f, height * 0.55f
                )
                cubicTo(
                    width * 0.51f, height * 0.55f,
                    width * 0.52f, height * 0.56f,
                    width * 0.54f, height * 0.63f
                )
                lineTo(width * 0.53f, height * 0.68f)
                lineTo(width * 0.515f, height * 0.65f)
                lineTo(width * 0.5f, height * 0.69f)
                lineTo(width * 0.485f, height * 0.65f)
                lineTo(width * 0.47f, height * 0.68f)
                close()
            }
            drawPath(
                path = snowCapPath,
                color = Color.White.copy(alpha = 0.85f)
            )

            // D. Draw Japanese Torii Gate
            val toriiLeft = width * 0.35f
            val toriiRight = width * 0.65f
            val toriiWidth = toriiRight - toriiLeft
            val groundY = height * 0.92f
            val headerY1 = height * 0.73f
            val headerY2 = height * 0.77f

            // Ground Line
            drawLine(
                color = toriiColor.copy(alpha = 0.8f),
                start = Offset(width * 0.25f, groundY),
                end = Offset(width * 0.75f, groundY),
                strokeWidth = 4.dp.toPx()
            )

            // Main Columns (left / right)
            drawRect(
                color = toriiColor,
                topLeft = Offset(toriiLeft + toriiWidth * 0.15f, headerY2),
                size = Size(toriiWidth * 0.08f, groundY - headerY2)
            )
            drawRect(
                color = toriiColor,
                topLeft = Offset(toriiRight - toriiWidth * 0.23f, headerY2),
                size = Size(toriiWidth * 0.08f, groundY - headerY2)
            )

            // Inner support beam
            drawRect(
                color = toriiColor,
                topLeft = Offset(toriiLeft + toriiWidth * 0.05f, headerY2 + 10.dp.toPx()),
                size = Size(toriiWidth * 0.9f, toriiWidth * 0.05f)
            )

            // Top curved header board (Kasagi)
            val kasagiPath = Path().apply {
                moveTo(toriiLeft - 10.dp.toPx(), headerY1)
                quadraticTo(width / 2, headerY1 + 5.dp.toPx(), toriiRight + 10.dp.toPx(), headerY1)
                lineTo(toriiRight + 12.dp.toPx(), headerY1 - 8.dp.toPx())
                quadraticTo(width / 2, headerY1 - 2.dp.toPx(), toriiLeft - 12.dp.toPx(), headerY1 - 8.dp.toPx())
                close()
            }
            drawPath(
                path = kasagiPath,
                color = toriiColor
            )
        }

        // 2. Beautiful styled card layout with scrolling support
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 280.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Elegant Japanese Label
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = 0.15f))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "日本語 • KANA MASTER",
                    color = primaryColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main App Display Title
            Text(
                text = "Nihongo Kana\n& Kotoba",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                lineHeight = 38.sp,
                color = if (currentTheme == AppTheme.SAMURAI_DARK) Color.White else Color(0xFF1A1A24)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Belajar Hiragana, Katakana, dan Kosakata Bahasa Jepang dengan mudah, terstruktur & menyenangkan.",
                fontSize = 14.sp,
                color = if (currentTheme == AppTheme.SAMURAI_DARK) Color.LightGray else Color(0xFF555566),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Stat badge list
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (currentTheme == AppTheme.SAMURAI_DARK) Color(0xFF1E1E1E) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "19",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                        Text(
                            text = "Level",
                            fontSize = 12.sp,
                            color = if (currentTheme == AppTheme.SAMURAI_DARK) Color.Gray else Color.Gray
                        )
                    }
                    Divider(
                        modifier = Modifier
                            .height(30.dp)
                            .width(1.dp),
                        color = Color.LightGray.copy(alpha = 0.5f)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "100%",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                        Text(
                            text = "Pilihan Ganda",
                            fontSize = 12.sp,
                            color = if (currentTheme == AppTheme.SAMURAI_DARK) Color.Gray else Color.Gray
                        )
                    }
                    Divider(
                        modifier = Modifier
                            .height(30.dp)
                            .width(1.dp),
                        color = Color.LightGray.copy(alpha = 0.5f)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "N5+",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                        Text(
                            text = "Kotoba",
                            fontSize = 12.sp,
                            color = if (currentTheme == AppTheme.SAMURAI_DARK) Color.Gray else Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Stunning Action Call Button
            Button(
                onClick = { viewModel.navigateTo(Screen.Dashboard) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = Color.White
                ),
                shape = CircleShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .offset(y = bounceOffset.dp)
                    .testTag("start_learning_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Mulai",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MULAI BELAJAR SEKARANG",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Metode belajar efektif disesuaikan berdasarkan baris Gojuon.",
                fontSize = 11.sp,
                color = Color.Gray,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
        }
    }
}
