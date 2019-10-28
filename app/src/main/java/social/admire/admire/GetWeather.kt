package social.admire.admire

import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_map_coord_hint.*
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.BasicResponseHandler
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.message.BasicNameValuePair
import org.json.JSONObject
import social.admire.admire.ImagesData.has_weather_data
import social.admire.admire.ImagesData.weather_icons
import social.admire.admire.TasksController.addNewTask

class GetWeather(activity: AppCompatActivity) : AsyncTask<String, Void, String>() {

    val activity = activity

    override fun doInBackground(vararg data: String): String? {
        if(!has_weather_data) {
            Log.d("getWeather", "start_updata")
            val httpclient = DefaultHttpClient()
            val http = HttpPost(data[0])
            val nameValuePairs = ArrayList<BasicNameValuePair>()
            nameValuePairs.add(BasicNameValuePair("url", data[1]))
            http.setEntity(UrlEncodedFormEntity(nameValuePairs))
            return httpclient.execute(http, BasicResponseHandler()) as String
        } else return ""
    }

    override fun onPostExecute(result: String?) {
        if(!has_weather_data) {
            try {
                val weather_data = JSONObject(result)

                activity.weather_scroll_view.removeAllViews()

                val dayly_data = weather_data.getJSONObject("dailyForecasts").getJSONObject("forecastLocation").getJSONArray("forecast")
                for (i in 0..dayly_data.length() - 1) {
                    val day_weather_layout = activity.layoutInflater.inflate(R.layout.activity_map_day_weather_block, null)
                    day_weather_layout.findViewById<TextView>(R.id.weather_block_day_of_weak).text =
                            dayly_data.getJSONObject(i).getString("weekday")
                    if (weather_icons.contains(dayly_data.getJSONObject(i).getString("iconName"))) {
                        day_weather_layout.findViewById<ImageView>(R.id.weather_block_icon).setImageBitmap(
                                weather_icons[dayly_data.getJSONObject(i).getString("iconName")])
                    } else {
                        addNewTask("HIGH_PRIORITY_TASKS",
                                GetImage_ImageView(dayly_data.getJSONObject(i).getString("iconLink"),
                                        day_weather_layout.findViewById<ImageView>(R.id.weather_block_icon),
                                        weather_icons, dayly_data.getJSONObject(i).getString("iconName")))
                    }
                    day_weather_layout.findViewById<TextView>(R.id.weather_block_description).text =
                            dayly_data.getJSONObject(i).getString("description")

                    day_weather_layout.findViewById<TextView>(R.id.weather_block_lowTemperature).text =
                            dayly_data.getJSONObject(i).getString("lowTemperature")
                    day_weather_layout.findViewById<TextView>(R.id.weather_block_highTemperature).text =
                            dayly_data.getJSONObject(i).getString("highTemperature")

                    activity.weather_scroll_view.addView(day_weather_layout)
                }
            } catch (e :Exception) {}
        }
    }
}