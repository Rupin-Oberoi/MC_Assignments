package com.cse535.assignment2_v3

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cse535.assignment2_v3.ui.theme.Assignment2_v3Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dbinstance = Room.databaseBuilder(
            applicationContext,
            TemperatureDatabase::class.java,
            "temperature_database"
        ).build()
        setContent {
            Assignment2_v3Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(dbinstance)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(dbinstance: TemperatureDatabase){
    var lat = 0.0; var long = 0.0
    val context = LocalContext.current
    var maxTemp by remember { mutableStateOf(0.0) }
    var minTemp by remember { mutableStateOf(0.0) }
    Column(modifier = Modifier.padding(10.dp)){
        var datePickerState = rememberDatePickerState()
        SelectDate(datePickerState)
        var selectedTimestamp = datePickerState.selectedDateMillis
        var selectedDate = ""
        if (selectedTimestamp != null) {
            selectedDate = getDateFromTimestamp(selectedTimestamp)
        }
        // check if internet is available


        val coroutineScope = rememberCoroutineScope()

        Button(onClick = {
            if (selectedTimestamp == null) {
                return@Button
            }
            val connectivity = checkConnectivity(context)
            val apiMode = connectivity

            val currTimestamp = System.currentTimeMillis()
            if (selectedTimestamp > currTimestamp){
                Log.d("tag1", "Selected date is in the future")
                maxTemp = Double.NaN
                minTemp = Double.NaN
                coroutineScope.launch {
                    val res = predictTemperature(dbinstance, lat, long, selectedDate, apiMode)
                    maxTemp = res.first
                    minTemp = res.second
                }
            }
            else{
                coroutineScope.launch {
                    val r1 = getTemperature(dbinstance, lat, long, selectedDate, apiMode)
                    maxTemp = r1.first
                    minTemp = r1.second
                    Log.d("tag2", "${r1.toString()}")
                    Log.d("tag2", "maxTemp: $maxTemp, minTemp: $minTemp")
                }
            }
        }) {
            Text("Get Temperature")
        }

    }
    Column(modifier = Modifier.padding(10.dp)) {
        Text("Max Temp: $maxTemp")
        Text("Min Temp: $minTemp")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectDate(datePickerState: DatePickerState){
    Column (Modifier.padding(10.dp)){
        //datePickerState = rememberDatePickerState()
        DatePicker(
            state = datePickerState,
            //modifier = Modifier.padding(16.dp)
        )
        Text("Selected date timestamp: ${datePickerState.selectedDateMillis ?: "no selection"}")
    }
}


suspend fun getTemperature(dbinstance: TemperatureDatabase, latitude: Double, longitude: Double, date: String, apiMode: Boolean): Pair<Double, Double> {
    if (apiMode == false){
        return getTemperaturesFromDB(dbinstance, date)
    }
    else {
        var maxTemp = 0.0; var minTemp = 0.0
//        val res = getTemperaturesFromAPI(dbinstance, latitude, longitude, date){
//                temperatures -> val(maxTemp1, minTemp1) = temperatures
//            maxTemp = maxTemp1
//            minTemp = minTemp1
//        }
//
//        Log.d("tag2.5", "maxTemp: $maxTemp, minTemp: $minTemp")
//        return Pair(maxTemp, minTemp)

        return withContext(Dispatchers.IO){
            suspendCoroutine {
                continuation ->
                getTemperaturesFromAPI(dbinstance, latitude, longitude, date){
                        temperatures -> val(maxTemp1, minTemp1) = temperatures
                    maxTemp = maxTemp1
                    minTemp = minTemp1
                    continuation.resume(Pair(maxTemp, minTemp))
                }
            }
        }


    }
    return Pair(Double.NaN, Double.NaN)
}


private suspend fun predictTemperature(dbinstance: TemperatureDatabase, latitude: Double, longitude: Double, date: String, apiMode: Boolean): Pair<Double, Double> {
    //val temperatureData = fetchTemperatureData(dbinstance, date)

    val currYear = Calendar.getInstance().get(Calendar.YEAR)
    // get data for the same date in the 10 previous years
    val maxTempList = mutableListOf<Double>()
    val minTempList = mutableListOf<Double>()
    for (i in 1..10) {
        val prevYear = currYear - i
        val prevDate = "$prevYear${date.substring(4)}"
//        if (apiMode == false) {
//            val temperatureData = getTemperaturesFromDB(dbinstance, prevDate)
//            maxTempList.add(temperatureData.second)
//            minTempList.add(temperatureData.first)
//        }
//
//        else{
//            getTemperaturesFromAPI(dbinstance, latitude, longitude, prevDate){
//                temperatures -> val(maxTemp, minTemp) = temperatures
//                maxTempList.add(maxTemp)
//                minTempList.add(minTemp)
//            }
//        }
        val temperatures = withContext(Dispatchers.IO){
            getTemperature(dbinstance, latitude, longitude, prevDate, apiMode)
        }
        maxTempList.add(temperatures.first)
        minTempList.add(temperatures.second)

    }
    // calculate the average of the max and min temperatures
    val maxTempAvg = maxTempList.average()
    val minTempAvg = minTempList.average()
    return Pair(maxTempAvg, minTempAvg)
}


fun getTemperaturesFromAPI(dbinstance: TemperatureDatabase,latitude: Double, longitude: Double, date: String, callback: (Pair<Double, Double>) -> Unit) {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://archive-api.open-meteo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService = retrofit.create(RetroInterface::class.java)
    val call = apiService.getForecast(latitude, longitude, startDate = date, endDate = date)
    val apiURL = call.request().url().toString()
    Log.d("tag1", apiURL)
    call.enqueue(object : Callback<Result> {
        override fun onResponse(call: Call<Result>, response: Response<Result>) {
            if (response.isSuccessful) {
                val forecastResponse = response.body()
                if (forecastResponse != null) {
                    Log.d("tag1", forecastResponse.toString())
                    val maxTemp = forecastResponse.daily.temperature_2m_max.firstOrNull() ?: Double.NaN
                    val minTemp = forecastResponse.daily.temperature_2m_min.firstOrNull() ?: Double.NaN
                    callback(Pair(maxTemp, minTemp))

                    val temperatureData = TemperatureData(
                        latitude = forecastResponse.latitude,
                        longitude = forecastResponse.longitude,
                        date = date,
                        maxTemperature = maxTemp,
                        minTemperature = minTemp
                    )
                    CoroutineScope(Dispatchers.IO).launch(){
                        storeTemperatureData(dbinstance, temperatureData)
                    }
                    //storeTemperatureData(dbinstance, temperatureData)
                    //val fetchedData = fetchTemperatureData(dbinstance, date)


                } else {
                    callback(Pair(Double.NaN, Double.NaN))
                }
            } else {
                callback(Pair(Double.NaN, Double.NaN))
            }
        }

        override fun onFailure(call: Call<Result>, t: Throwable) {
            Log.e("API_ERROR", "Error making API request: ${t.message}")
            callback(Pair(Double.NaN, Double.NaN))
        }
    })

}

private suspend fun storeTemperatureData(dbinstance: TemperatureDatabase, temperatureData: TemperatureData) {
    // Get an instance of the database

    val db = dbinstance

    // Use a coroutine to insert the data asynchronously
    CoroutineScope(Dispatchers.IO).launch {
        db.temperatureDataDao().insertTemperatureData(temperatureData)
    }
}

private suspend fun getTemperaturesFromDB(dbinstance: TemperatureDatabase, date: String): Pair<Double, Double> {
    Log.d("tag_fetch", "fetching data from db")
    var res = Pair(Double.NaN, Double.NaN)
    val temp = withContext(Dispatchers.IO) {
        dbinstance.temperatureDataDao().getTemperatureDataByDate(date)
    }
    temp?.let{
        Log.d("tag_fetch", temp.toString())
        res = Pair(temp.minTemperature, temp.maxTemperature)
    }
    return res
}


@SuppressLint("SimpleDateFormat")
fun getDateFromTimestamp(timestamp: Long): String {
    try{
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val dateEntered = java.util.Date(timestamp)
        return dateFormat.format(dateEntered)
    }
    catch (e: Exception){
        Log.d("myError", e.toString())
        return "Error"
    }
}


fun checkConnectivity(context: Context): Boolean{
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = cm.activeNetwork
    val capabilities = cm.getNetworkCapabilities(activeNetwork)
    if (capabilities != null) {
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    return false
}