package com.cse535.assignment3

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

//import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
//import com.patrykandpatrick.vico.core.cartesian.CartesianChart
//import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
//

class MainActivity2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity2)


        this.lifecycleScope.launch(Dispatchers.Main) {
            val acc_data = fetchAccelData(this@MainActivity2)
            // list of accelerometerData objects with id, timestamp, x, y, z
            Log.d("tag", acc_data.toString())
            val chartX: LineChart = findViewById(R.id.chart1)


            val entriesX = acc_data.mapIndexed { index, accelerometerData ->
                Entry(index.toFloat(), accelerometerData.x)
            }

            val entriesY = acc_data.mapIndexed { index, accelerometerData ->
                Entry(index.toFloat(), accelerometerData.y)
            }

            val entriesZ = acc_data.mapIndexed { index, accelerometerData ->
                Entry(index.toFloat(), accelerometerData.z)
            }

            val dataset = LineDataSet(entriesX, "in m/s^2")
            val lineData = LineData(dataset)

            chartX.data = lineData

            val description = Description()
            description.text = "X acceleration over time"
            chartX.description = description
            chartX.setDrawGridBackground(false)
            chartX.animateX(1500)
            chartX.invalidate()


            val chartY = findViewById<LineChart>(R.id.chart2)
            val dataset1 = LineDataSet(entriesY, "in m/s^2")
            val lineData1 = LineData(dataset1)

            chartY.data = lineData1

            val description1 = Description()
            description1.text = "Y acceleration over time"
            chartY.description = description1
            chartY.setDrawGridBackground(false)
            chartY.animateX(1500)
            chartY.invalidate()

            val chartZ = findViewById<LineChart>(R.id.chart3)

            val dataset2 = LineDataSet(entriesZ, "in m/s^2")
            val lineData2 = LineData(dataset2)

            chartZ.data = lineData2

            val description2 = Description()
            description2.text = "Z acceleration over time"
            chartZ.description = description2
            chartZ.setDrawGridBackground(false)
            chartZ.animateX(1500)
            chartZ.invalidate()


        }
    }
}

private suspend fun fetchAccelData(activity: MainActivity2): List<AccelerometerData> {
    val db = AppDatabase.getDatabase(activity)
    return db.accelerometerDataDao().getLast50()
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}