package com.cse535.assignment3

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
//import androidx.core.content.ContextCompat.getSystemService
import com.cse535.assignment3.ui.theme.Assignment3Theme
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.lifecycleScope

import android.hardware.SensorEventListener
import android.os.Environment
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AccelerometerApp(this)
        }
    }
}

@Composable
fun AccelerometerApp(activity: MainActivity) {
    val x = remember { mutableStateOf(0f) }
    val y = remember { mutableStateOf(0f) }
    val z = remember { mutableStateOf(0f) }
    // Retrieve the accelerometer sensor
    val sensorManager = activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    Log.d("tag", "here0")
    // Observe the accelerometer sensor and update the state
    LaunchedEffect(accelerometer) {
        var prevTime = System.currentTimeMillis()
        var inst = 0
        val interval = 100
        val freq = 1000/interval
        // for freq = 10Hz, read every instance
        // for
        val sensorEventListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val currTime = System.currentTimeMillis()
                if (currTime - prevTime < interval) {
                    return
                }
                activity.lifecycleScope.launch(Dispatchers.Main) {
                    Log.d("tag", "here1")
                    x.value = event.values[0]
                    y.value = event.values[1]
                    z.value = event.values[2]
                    prevTime = currTime
                    inst = (inst + 1)%3
                    storeData(activity, currTime, freq, x.value, y.value, z.value)

                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
        Log.d("tag", "here2")
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)


    }

    // Display the acceleration values
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Acceleration (m/s²)", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("X: ${String.format("%.4f", x.value)}", fontSize = 18.sp)
        Text("Y: ${String.format("%.4f", y.value)}", fontSize = 18.sp)
        Text("Z: ${String.format("%.4f", z.value)}", fontSize = 18.sp)
        Button (onClick = {
            // go to MainActivity2
            activity.lifecycleScope.launch(Dispatchers.Main) {
                storeDataCSV(activity)
            }
            val intent = Intent(activity, MainActivity2::class.java)
            activity.startActivity(intent)
        }){
            Text(text = "Store Data")
        }
    }
}

private suspend fun storeData(activity: MainActivity , timestamp: Long, freq: Int, x: Float, y: Float, z: Float) {
    val db = AppDatabase.getDatabase(activity)
    val accelerometerData = AccelerometerData(time = timestamp, freq = freq, x = x, y = y, z = z)
    activity.lifecycleScope.launch(Dispatchers.IO) {
        db.accelerometerDataDao().insert(accelerometerData)
    }
}

private suspend fun storeDataCSV(activity: MainActivity){
    val db = AppDatabase.getDatabase(activity)
    val data = db.accelerometerDataDao().getAll()
    val csv = data.joinToString("\n") { "${it.time},${it.freq},${it.x},${it.y},${it.z}" }
    val folderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
    Log.d("path", folderPath)
    Log.d("csv", csv)
    val file = java.io.File(folderPath, "accelerometer_data_freq.csv")
    file.writeText(csv, Charsets.UTF_8)
}