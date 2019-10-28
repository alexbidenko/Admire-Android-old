package social.admire.admire

import android.graphics.Bitmap
import org.json.JSONObject
import java.util.ArrayList

object ImagesData {
    val places_avatars = mutableMapOf<String, Bitmap>()
    var user_avatar: Bitmap? = null
    val places_map_icons = mutableMapOf<String, Bitmap>()
    val weather_icons = mutableMapOf<String, Bitmap>()

    var has_weather_data = false

    var map_prepare = ArrayList<JSONObject>()
    var places_data = ArrayList<JSONObject>()

    var now_places_length = 0
}