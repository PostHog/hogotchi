package com.posthog.hogotchi

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import com.posthog.PostHog
import com.posthog.hogotchi.ui.theme.HogotchiTheme
import com.posthog.hogotchi.ui.theme.PostHogOrange
import com.posthog.hogotchi.ui.theme.PostHogYellow

class MainActivity : ComponentActivity() {

    private val viewModel: HogViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notifications enabled! Your hog can now remind you to care for it.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Without notifications, you might forget to feed your hog!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()
        askNotificationPermission()
        logFcmToken()

        intent.getStringExtra("action")?.let { action ->
            viewModel.handleNotificationAction(action)
        }

        setContent {
            HogotchiTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HogotchiScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        intent.getStringExtra("action")?.let { action ->
            viewModel.handleNotificationAction(action)
        }
    }

    private fun createNotificationChannel() {
        val channelId = getString(R.string.default_notification_channel_id)
        val channelName = "Hogotchi Alerts"
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.createNotificationChannel(
            NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications about your virtual hog's needs"
            }
        )
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun logFcmToken() {
        Firebase.messaging.token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM token failed", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d(TAG, "FCM Token: $token")
            PostHog.setFcmToken(token)
            Log.d(TAG, "FCM Token sent to PostHog")
        }
    }

    companion object {
        private const val TAG = "HogotchiMain"
    }
}

@Composable
fun HogotchiScreen(viewModel: HogViewModel) {
    val hogState by viewModel.hogState.collectAsState()
    val showSurprise by viewModel.showSurprise.collectAsState()
    val lastAction by viewModel.lastAction.collectAsState()

    val scale by animateFloatAsState(
        targetValue = if (showSurprise) 1.1f else 1f,
        animationSpec = tween(150),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Hog-otchi",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = PostHogOrange
        )
        Text(
            text = "Level ${hogState.level} ${hogState.name}",
            fontSize = 18.sp,
            color = PostHogYellow
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Hog Display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(
                        id = if (showSurprise) R.drawable.hog_surprised else hogState.hogImage
                    ),
                    contentDescription = "Your hog",
                    modifier = Modifier
                        .size(280.dp)
                        .scale(scale)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            viewModel.onHogTapped()
                        }
                )

                // Mood indicator
                Text(
                    text = when (hogState.mood) {
                        HogMood.HAPPY -> "😊"
                        HogMood.HUNGRY -> "🍎"
                        HogMood.SLEEPY -> "💤"
                        HogMood.PLAYFUL -> "🎾"
                        HogMood.CRITICAL -> "⚠️"
                        HogMood.IDLE -> "👋"
                    },
                    fontSize = 48.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Last action feedback
        AnimatedVisibility(
            visible = lastAction != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            lastAction?.let {
                Text(
                    text = it,
                    fontSize = 16.sp,
                    color = PostHogYellow,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        // Stats
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                StatBar(label = "Happiness", value = hogState.happiness, color = PostHogYellow)
                Spacer(modifier = Modifier.height(8.dp))
                StatBar(label = "Hunger", value = hogState.hunger, color = PostHogOrange)
                Spacer(modifier = Modifier.height(8.dp))
                StatBar(label = "Energy", value = hogState.energy, color = Color(0xFF4CAF50))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActionButton(
                emoji = "🍎",
                label = "Feed",
                onClick = { viewModel.onAction(HogAction.Feed) }
            )
            ActionButton(
                emoji = "🎾",
                label = "Play",
                onClick = { viewModel.onAction(HogAction.Play) }
            )
            ActionButton(
                emoji = "💤",
                label = "Sleep",
                onClick = { viewModel.onAction(HogAction.Sleep) }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tap your hog to pet it!",
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun StatBar(label: String, value: Int, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 14.sp, color = Color.White)
            Text(text = "$value%", fontSize = 14.sp, color = color)
        }
        LinearProgressIndicator(
            progress = { value / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = Color.DarkGray
        )
    }
}

@Composable
fun ActionButton(emoji: String, label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = PostHogOrange
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.width(100.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Text(text = label, fontSize = 12.sp)
        }
    }
}
