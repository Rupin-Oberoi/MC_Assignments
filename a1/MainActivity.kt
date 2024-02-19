package com.cse535.demoapp1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // for testing purposes can create any number of stops and distances are generated randomly
//        val numStops = 25
//        val stops = generateItems(numStops)
//        val ran = Random
//        val dists = (1..<numStops).map{ran.nextDouble()}
        // comment one of the 2 sets of hardcoded lists
        val stops = listOf("Source", "Stop 1", "Stop 2", "Stop 3", "Stop 4", "Stop 5", "Stop 6", "Destination")
        val dists = listOf(12.0, 14.0, 20.2, 34.53, 3.49, 7.5034, 10.5)
//
//        val stops = listOf("Source", "Stop 1", "Stop 2", "Stop 3", "Stop 4", "Stop 5", "Stop 6", "Stop 7", "Stop 8", "Stop 9", "Stop 10", "Destination")
//        val dists = listOf(12.0, 14.0, 20.2, 34.53, 3.49, 7.5034, 10.5, 24.30, 3.1234, 2.402, 10.0)

        setContent {
            JourneyApp(stops, dists)
        }
    }
}

@Composable
fun JourneyApp(stops: List<String>, dists: List<Double>) {
    var currInd by remember {mutableIntStateOf(0)}
    var inKm by remember { mutableStateOf(true) }

    assert(dists.size == stops.size-1)

    var totalDistCovered by remember{mutableDoubleStateOf(0.0)}
    var totalDistLeft by remember{ mutableDoubleStateOf(dists.sum()) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Current Stop is: ${stops[currInd]}" +
                "${if (currInd == stops.size - 1) " (Destination)" else ""}", fontSize = 16.sp)
        Text("Next Stop is: ${if (currInd < stops.size - 1) stops[currInd + 1] else "-"}")
        Text("Distance to next stop: ${if (currInd < stops.size - 1) formatDistance(dists[currInd], inKm) else "0 ${if (inKm) "km" else "mi"}"}")

        Button(onClick = {
            inKm = !inKm
        }) {
            Text(if (inKm) "Use Miles" else "Use Kilometers")
        }

        Button(onClick = {
            if (currInd < stops.size - 1){
                totalDistCovered += dists[currInd]
                totalDistLeft -= dists[currInd]
                // to avoid -0.00 left bug
                totalDistLeft = abs(totalDistLeft)
                currInd++ } })
        {
            Text("Next Stop")
        }
        Box(modifier = Modifier
            .fillMaxWidth()
            //.weight(1f)
            .border(width = 3.dp, color = Color.Black)
            .padding(all = 5.dp)
            .height(104.dp)
            //.clip(RoundedCornerShape(40.dp))

        )
        {
            if (stops.size > 10){
                //LazyLoadingList(stops, dists, currentStopIndex, inKm)

                LazyColumn(modifier = Modifier
                    .height(104.dp), rememberLazyListState()){items(1){
                    stops.forEachIndexed{index, item -> Text(text = item + getStopStatus(dists, currInd, index, inKm))}}
                }
            }
            else{
                Column(modifier = Modifier
                    .verticalScroll(rememberScrollState())){
                stops.forEachIndexed{index, item -> Text(text = item + getStopStatus(dists, currInd, index, inKm))}
            }
            }
        }
        Text("Total distance covered: ${formatDistance(totalDistCovered, inKm)}", fontSize = 16.sp)
        Text("Total distance left: ${formatDistance(totalDistLeft, inKm)}", fontSize = 16.sp)
        JourneyProgress(totalDistCovered, totalDistCovered+totalDistLeft)
    }
}


fun formatDistance(dist: Double, inKm: Boolean): String{
    var res = ""
    if (inKm){res = String.format("%.2f km", dist)}
    else{res = String.format("%.2f mi", convertKmToMiles(dist))}
    return res
}

fun getStopStatus(dists: List<Double>, currInd: Int, endInd: Int, inKm:Boolean): String{
    if (currInd >= endInd){
        return " : Already reached"
    }
    else{
        var d = 0.0
        for (i in (currInd..<endInd)){
            d += dists[i]
        }
        return " : ${formatDistance(d, inKm)} away"
    }
}

fun convertKmToMiles(distInKm: Double): Double{ return distInKm*0.6214 }


@Composable
fun JourneyProgress(distCovered: Double, distTotal: Double) {
    val completionPerc: Float = (distCovered/distTotal).toFloat()
    LinearProgressIndicator(progress = completionPerc,
        modifier = Modifier
            .fillMaxWidth(),
        color = Color.Green
    )
}

fun generateItems(count: Int): List<String>{
    return List(count) {"Stop ${it + 1}"}
}