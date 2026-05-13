package com.example.corebooster

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setContent replaces setContentView. This is where Compose starts!
        setContent {
            BoosterScreen()
        }
    }
}

// @Composable tells Android this is a UI piece, not standard logic
@Composable
fun BoosterScreen() {
    val context = LocalContext.current

    // 1. These are "State" variables. Whenever these numbers change,
    // the UI will automatically redraw itself to show the new numbers!
    var batteryLevel by remember { mutableFloatStateOf(0f) }
    var temperature by remember { mutableFloatStateOf(0f) }
    var isCharging by remember { mutableStateOf(false) }
    var health by remember { mutableStateOf("Scanning...") }

    // 2. DisposableEffect is a Compose tool that manages background tasks.
    // It starts listening when the screen opens, and stops when it closes.
    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {

                    // Update Battery Level
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    if (level != -1 && scale != -1) {
                        batteryLevel = level * 100 / scale.toFloat()
                    }

                    // Update Temperature (Divide by 10 for Celsius)
                    temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f

                    // Update Charging Status
                    val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                    isCharging = plugged == BatteryManager.BATTERY_PLUGGED_AC ||
                            plugged == BatteryManager.BATTERY_PLUGGED_USB ||
                            plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS

                    // Update Health
                    val healthInt = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
                    health = when (healthInt) {
                        BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "OVERHEATING"
                        BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                        BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
                        BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                        else -> "Unknown"
                    }
                }
            }
        }

        // Start listening
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)

        // onDispose runs automatically when the app is minimized to save battery
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    // 3. The UI Layout (This replaces the XML file entirely)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)) // Dark mode background
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "PROJECT BOOSTER",
            color = Color.Green,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = "Battery: ${batteryLevel.toInt()}%",
            color = Color.White,
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Notice the color logic here: it turns red if it gets over 38C
        Text(
            text = "Temp: $temperature °C",
            color = if (temperature > 38.0f) Color.Red else Color.White,
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = if (isCharging) "Status: Charging ⚡" else "Status: Discharging",
            color = Color.White,
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Health: $health",
            color = Color.White,
            fontSize = 20.sp
        )
    }
}