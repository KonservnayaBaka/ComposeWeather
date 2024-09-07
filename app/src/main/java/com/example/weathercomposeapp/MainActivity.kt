package com.example.weathercomposeapp

import android.app.DownloadManager
import android.content.Context
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.webkit.WebSettings.TextSize
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.example.weathercomposeapp.ui.theme.WeatherComposeAppTheme
import androidx.compose.ui.res.colorResource
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.text.font.FontWeight
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            window.statusBarColor = getColor(R.color.statusBar)
            window.navigationBarColor = getColor(R.color.navBar)
            WeatherScreen()
        }
    }
}

@Composable
fun WeatherScreen() {
    var temp by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var country by remember { mutableStateOf("") }
    var weatherHourList by remember { mutableStateOf(mutableListOf<WeatherHourModel>()) }
    var weatherDaysList by remember { mutableStateOf(mutableListOf<WeatherDaysModel>()) }
    var context = LocalContext.current
    val borderBgColor: Color = colorResource(R.color.borderBG)

    LaunchedEffect(Unit) {
        getWeather("Moscow", context) { newTemp, newCity, newCountry, newWeatherHourList, newWeatherDaysList  ->
            temp = newTemp
            city = newCity
            country = newCountry
            weatherHourList = newWeatherHourList.toMutableList()
            weatherDaysList = newWeatherDaysList.toMutableList()
        }
    }

    Image(
        painterResource(id = R.drawable.background),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 10.dp)
        .padding(top = 20.dp, bottom = 10.dp),
    ){
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f)
                .background(color = borderBgColor, shape = RoundedCornerShape(10.dp))
                .border(width = 1.dp, color = Color.White, shape = RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text="$city", fontSize = 32.sp, color = Color.White, modifier = Modifier.padding(bottom = 0.3.dp))
                Text(text="$country", fontSize = 16.sp, color = Color.White)
                Text(text = "$temp°", fontSize = 86.sp, color = Color.White)
            }
        }

            LazyRow(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)) {
                items(weatherHourList.size) { index ->
                    WeatherHourItem(weatherHourList[index])
                }
            }
            LazyColumn(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)) {
                items(weatherDaysList.size) { index ->
                    WeatherDayItem(weatherDaysList[index])
                }
            }
    }
}

@Composable
fun WeatherHourItem(weatherHour: WeatherHourModel) {
    Column(
        modifier = Modifier
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Text(text = weatherHour.hour, fontSize = 20.sp, color = Color.White)
        Image(painter = rememberAsyncImagePainter(model = "https:${weatherHour.image}"), contentDescription = null, modifier = Modifier.size(50.dp))
        Text(text = "${weatherHour.temp}°", fontSize = 20.sp, color = Color.White)
    }
}

@Composable
fun WeatherDayItem(weatherDay: WeatherDaysModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth().padding(bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = weatherDay.date,Modifier.width(50.dp),fontSize = 24.sp, color = Color.White)
        Spacer(modifier = Modifier.width(16.dp))
        Image(
            painter = rememberAsyncImagePainter(model = "https:${weatherDay.image}"),
            contentDescription = null,
            modifier = Modifier.size(50.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = "Max: ${weatherDay.maxtemp_c}°", fontSize = 24.sp, color = Color.White)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = "Min: ${weatherDay.mintemp_c}°", fontSize = 24.sp, color = Color.White)
    }
}

private fun getWeather (city: String, context: Context, onWeaTherUpdate: (String, String, String, List<WeatherHourModel>, List<WeatherDaysModel>) -> Unit) {
    var api_key = "634b11d5452e4086921121137242208"
    var url = "https://api.weatherapi.com/v1/forecast.json?key=$api_key&q=$city&days=7&aqi=no&alerts=no"

    val queue = Volley.newRequestQueue(context)
    val jsonObjectRequest = JsonObjectRequest(
        Request.Method.GET, url, null,
        { response ->
            val current = response.getJSONObject("current")
            val location = response.getJSONObject("location")
            val forecast = response.getJSONObject("forecast")
            val forecastday = forecast.getJSONArray("forecastday")
            val temp = current.getString("temp_c").toDouble().toInt().toString()
            var city = location.getString("name")
            var country = location.getString("country")
            var weatherHourList = mutableListOf<WeatherHourModel>()
            var weatherDaysList = mutableListOf<WeatherDaysModel>()

            for(i in 0 until forecastday.length()) {
                val day = forecastday.getJSONObject(i)
                val date = day.getString("date")
                val maxTemp = day.getJSONObject("day").getString("maxtemp_c")
                val minTemp = day.getJSONObject("day").getString("mintemp_c")
                val condition = day.getJSONObject("day").getJSONObject("condition").getString("icon")

                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("EE", Locale.getDefault())
                val parsedDate = inputFormat.parse(date)
                val dayOfWeek = outputFormat.format(parsedDate)

                weatherDaysList.add(WeatherDaysModel(dayOfWeek, condition, maxTemp, minTemp))

                val hourArray = day.getJSONArray("hour")
                for(j in 0 until hourArray.length()) {
                    val hour = hourArray.getJSONObject(j)
                    val hourTime = hour.getString("time")
                    val hourTemp = hour.getString("temp_c").toDouble().toInt().toString()
                    val hourImage = hour.getJSONObject("condition").getString("icon")

                    val inputTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    val outputTimeFormat = SimpleDateFormat("HH", Locale.getDefault())
                    val parsedTime = inputTimeFormat.parse(hourTime)
                    val formattedTime = outputTimeFormat.format(parsedTime)

                    weatherHourList.add(WeatherHourModel(formattedTime, hourImage, hourTemp))
                }
            }

            onWeaTherUpdate(temp, city, country, weatherHourList, weatherDaysList)
        },
        { error ->
            Log.e("API", "Error: ${error.toString()}")
        }
    )

    queue.add(jsonObjectRequest)
}