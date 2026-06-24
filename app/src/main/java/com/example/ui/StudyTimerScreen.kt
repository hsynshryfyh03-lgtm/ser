package com.example.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import com.example.R
import java.util.Locale
import kotlin.random.Random
import kotlin.math.cos
import kotlin.math.sin

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)
val fontName = GoogleFont("Cairo")
val cairoFontFamily = FontFamily(
    Font(googleFont = fontName, fontProvider = provider)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyTimerScreen(viewModel: TimerViewModel = viewModel()) {
    val mode by viewModel.mode.collectAsStateWithLifecycle()
    val timeLeft by viewModel.timeLeft.collectAsStateWithLifecycle()
    val isRunning by viewModel.isRunning.collectAsStateWithLifecycle()
    val sessionGoal by viewModel.sessionGoal.collectAsStateWithLifecycle()
    val allSessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val totalStudyMinutes by viewModel.totalStudyMinutes.collectAsStateWithLifecycle()
    
    val streak = remember(allSessions) { viewModel.calculateStreak(allSessions) }

    val studyColor = Color(0xFFFF3300)
    val breakColor = Color(0xFF00FF66)

    val activeColor by animateColorAsState(
        if (mode == TimerMode.STUDY) studyColor else breakColor, 
        animationSpec = tween(1000), 
        label = "activeColor"
    )

    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Ambient Cinematic Background Glows
        val infiniteTransition = rememberInfiniteTransition(label = "infinite")
        val angle1 by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(15000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ), label = "angle1"
        )
        val angle2 by infiniteTransition.animateFloat(
            initialValue = 360f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(20000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ), label = "angle2"
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            
            val centerOffset1 = Offset(
                x = centerX + cos(Math.toRadians(angle1.toDouble())).toFloat() * size.width * 0.3f,
                y = centerY + sin(Math.toRadians(angle1.toDouble())).toFloat() * size.height * 0.3f
            )
            val centerOffset2 = Offset(
                x = centerX + cos(Math.toRadians(angle2.toDouble())).toFloat() * size.width * 0.3f,
                y = centerY + sin(Math.toRadians(angle2.toDouble())).toFloat() * size.height * 0.3f
            )
            val radius1 = size.width * 0.8f
            val radius2 = size.width * 0.9f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(activeColor.copy(alpha = 0.3f), Color.Transparent),
                    center = centerOffset1,
                    radius = radius1
                ),
                center = centerOffset1,
                radius = radius1
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(activeColor.copy(alpha = 0.15f), Color.Transparent),
                    center = centerOffset2,
                    radius = radius2
                ),
                center = centerOffset2,
                radius = radius2
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header stats (Glassmorphism)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .glassmorphism()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔥 الأيام المتتالية", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp, letterSpacing = 1.sp, fontFamily = cairoFontFamily)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("$streak يوم", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 20.sp, fontFamily = cairoFontFamily)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⏱️ إجمالي الإنجاز", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp, letterSpacing = 1.sp, fontFamily = cairoFontFamily)
                    Spacer(modifier = Modifier.height(6.dp))
                    val hours = (totalStudyMinutes ?: 0) / 60
                    val mins = (totalStudyMinutes ?: 0) % 60
                    Text("${hours}h ${mins}m", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 20.sp, fontFamily = cairoFontFamily)
                }
            }

            // Main Timer Area (Glass Card)
            Box(
                contentAlignment = Alignment.Center, 
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
            ) {
                TimerRing(
                    timeLeft = timeLeft,
                    totalTime = mode.durationSeconds,
                    color = activeColor
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedContent(
                        targetState = timeLeft,
                        transitionSpec = {
                            if (targetState < initialState) {
                                (slideInVertically { height -> height } + fadeIn() + scaleIn(initialScale = 0.8f)).togetherWith(
                                    slideOutVertically { height -> -height } + fadeOut() + scaleOut(targetScale = 1.2f)
                                )
                            } else {
                                (slideInVertically { height -> -height } + fadeIn() + scaleIn(initialScale = 1.2f)).togetherWith(
                                    slideOutVertically { height -> height } + fadeOut() + scaleOut(targetScale = 0.8f)
                                )
                            }
                        },
                        label = "timerAnimation"
                    ) { time ->
                        val minutes = time / 60
                        val seconds = time % 60
                        Text(
                            text = String.format(Locale.US, "%02d:%02d", minutes, seconds),
                            color = Color.White,
                            fontSize = 96.sp,
                            fontWeight = FontWeight.Thin,
                            letterSpacing = 2.sp,
                            fontFamily = cairoFontFamily
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (mode == TimerMode.STUDY) "جلسة تركيز عميق" else "وقت الاسترخاء",
                        color = activeColor.copy(alpha = 0.8f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 3.sp,
                        fontFamily = cairoFontFamily
                    )
                }
            }

            // Interactive Controls & Goal
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                if (mode == TimerMode.STUDY) {
                    OutlinedTextField(
                        value = sessionGoal,
                        onValueChange = { viewModel.setGoal(it) },
                        placeholder = { 
                            Text(
                                "هدف الجلسة (مثال: دراسة فصل 1)...", 
                                color = Color.White.copy(alpha = 0.3f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth(),
                                fontFamily = cairoFontFamily
                            ) 
                        },
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, color = Color.White, fontSize = 16.sp, fontFamily = cairoFontFamily),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = activeColor.copy(alpha = 0.5f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                            cursorColor = activeColor,
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassmorphism()
                    )
                } else {
                    BreakTipsWidget(activeColor)
                    Spacer(modifier = Modifier.height(16.dp))
                    AudioPlayerMock(activeColor)
                }

                Spacer(modifier = Modifier.height(48.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Reset Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.resetTimer()
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Timer",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(40.dp))

                    // Play/Pause Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(activeColor.copy(alpha = 0.1f))
                            .border(1.dp, activeColor.copy(alpha = 0.2f), CircleShape)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.toggleTimer()
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(activeColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Toggle Timer",
                                tint = Color.Black,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(96.dp)) // To balance the layout
                }
            }
        }
    }
}

@Composable
fun TimerRing(timeLeft: Int, totalTime: Int, color: Color) {
    val progress = if (totalTime > 0) timeLeft.toFloat() / totalTime.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessVeryLow
        ), label = "progress"
    )

    Canvas(modifier = Modifier.size(320.dp)) {
        // Subtle background ring
        drawArc(
            color = Color.White.copy(alpha = 0.05f),
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // Outer glow
        drawArc(
            color = color.copy(alpha = 0.25f),
            startAngle = -90f,
            sweepAngle = 360f * animatedProgress,
            useCenter = false,
            style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
        )

        // Mid glow
        drawArc(
            color = color.copy(alpha = 0.5f),
            startAngle = -90f,
            sweepAngle = 360f * animatedProgress,
            useCenter = false,
            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
        )

        // Inner solid ring
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * animatedProgress,
            useCenter = false,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
fun BreakTipsWidget(color: Color) {
    val tips = listOf(
        "اشرب كوباً من الماء 💧",
        "قم بتمارين إطالة خفيفة 🧘",
        "أرح عينيك وانظر لمسافة بعيدة 👀",
        "خذ نفساً عميقاً واسترخ 🌬️"
    )
    val randomTip = remember { tips[Random.nextInt(tips.size)] }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassmorphism()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(randomTip, color = color.copy(alpha = 0.9f), fontSize = 16.sp, fontWeight = FontWeight.Medium, fontFamily = cairoFontFamily)
    }
}

@Composable
fun AudioPlayerMock(activeColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassmorphism()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.3f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MusicNote, contentDescription = null, tint = activeColor)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Lofi Study Beats", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, fontFamily = cairoFontFamily)
                Text("تلاشي صوتي هادئ...", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp, fontFamily = cairoFontFamily)
            }
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Play Audio", tint = Color.White, modifier = Modifier.size(24.dp))
        }
    }
}

fun Modifier.glassmorphism() = this.then(
    Modifier
        .clip(RoundedCornerShape(24.dp))
        .background(
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.08f),
                    Color.White.copy(alpha = 0.02f)
                )
            )
        )
        .border(
            width = 1.dp, 
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.15f),
                    Color.White.copy(alpha = 0.02f)
                )
            ), 
            shape = RoundedCornerShape(24.dp)
        )
)
