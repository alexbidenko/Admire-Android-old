package social.admire.admire

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.AsyncTask
import android.support.annotation.Nullable
import android.support.v4.content.ContextCompat.startActivity
import android.transition.Transition
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.here.android.mpa.common.GeoCoordinate
import com.here.android.mpa.common.Image
import com.here.android.mpa.mapping.Map
import com.here.android.mpa.mapping.MapContainer
import com.here.android.mpa.mapping.MapMarker

import org.json.JSONArray
import social.admire.admire.FragmentController.activeFragment
import social.admire.admire.FragmentController.fm
import social.admire.admire.FragmentController.mainActivity
import social.admire.admire.FragmentController.openPlaceFragment
import social.admire.admire.ImagesData.now_places_length
import social.admire.admire.ImagesData.places_avatars
import social.admire.admire.ImagesData.places_data
import social.admire.admire.ImagesData.places_map_icons
import social.admire.admire.TasksController.addNewTask

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.HashMap

import javax.net.ssl.HttpsURLConnection

internal class GetDataByCoord(val latitude: Float, val longitude: Float) : AsyncTask<String, String, String>() {

    var activity: Activity? = null
    var context: Context? = null
    var linearLayout: LinearLayout? = null
    var last_latitude: Float = 0f
    var last_longitude: Float = 0f

    var places_container: MapContainer? = null

    var text_coord: String = ""

    fun prepareLayoutDraw(activity: Activity, linearLayout: LinearLayout, last_latitude: Float, last_longitude: Float): GetDataByCoord {
        this.activity = activity
        this.linearLayout = linearLayout
        this.last_latitude = last_latitude
        this.last_longitude = last_longitude

        return this
    }

    fun prepareMapDraw(context: Context, places_container: MapContainer): GetDataByCoord {
        this.context = context
        this.places_container = places_container

        return this
    }

    override fun doInBackground(vararg params: String): String {
        val url: URL
        var response = ""
        try {
            text_coord = if(latitude == 0f && longitude == 0f) {
                ""
            } else {
                "?latitude=$latitude&longitude=$longitude"
            }

            url = URL("https://admire.social/back/get-places-by-coord.php$text_coord")

            val conn = url.openConnection() as HttpURLConnection
            conn.readTimeout = 15000
            conn.connectTimeout = 15000
            conn.requestMethod = "GET"
            conn.doInput = true
            conn.doOutput = true


            val os = conn.outputStream
            val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))

            writer.flush()
            writer.close()
            os.close()
            val responseCode = conn.responseCode

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                val br = BufferedReader(InputStreamReader(conn.inputStream))
                response = br.readText()
            } else {
                response = ""
            }

            if (response != "") {
                val all_places = JSONArray(response)

                now_places_length = all_places.length()

                for (i in 0 until all_places.length()) {
                    var find = false
                    for(j in 0 until places_data.size) {
                        if(places_data[j].getInt("id") == all_places.getJSONObject(i).getInt("id") &&
                                places_data[j].getInt("status") == all_places.getJSONObject(i).getInt("status")) {
                            find = true

                            Log.d("places_container", "find")
                        }
                    }

                    Log.d("places_container", "for")

                    if(!find) {
                        val current_data = all_places.getJSONObject(i)
                        places_data.add(current_data)

                        if(places_container != null) {
                            val latitude = current_data.getDouble("latitude")
                            val longitude = current_data.getDouble("longitude")

                            val avatar = current_data.getString("avatar_small")
                            if (places_map_icons.contains("marker_" + current_data.getInt("id"))) {
                                val marker_img = Image()
                                marker_img.bitmap = places_map_icons.get("marker_" + current_data.getInt("id"))
                                val marker = MapMarker(GeoCoordinate(latitude, longitude), marker_img)
                                marker.title = current_data.getInt("id").toString()
                                places_container!!.addMapObject(marker)
                            } else {
                                addNewTask("MID_PRIORITY_TASKS",
                                        GetImage_Marker(context!!,
                                                avatar,
                                                places_container!!,
                                                GeoCoordinate(latitude, longitude),
                                                places_map_icons,
                                                current_data.getInt("id").toString()))
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("writableDatabase", "error")
        }

        return ""
    }

    override fun onPostExecute(result: String) {
        if(linearLayout != null) {
            val sorted_places_data = places_data.filter { pl ->
                run {
                    val earth_r = 6371
                    val sin_1 = Math.sin(((pl.getDouble("latitude") - last_latitude) / 2) / 360 * 2 * Math.PI)
                    val sin_2 = Math.sin(((pl.getDouble("longitude") - last_longitude) / 2) / 360 * 2 * Math.PI)
                    val distance = 2 * earth_r * Math.asin(Math.sqrt(Math.abs(sin_1 * sin_2 + sin_1 * sin_2 *
                            Math.cos((last_latitude.toDouble()) / 360 * 2 * Math.PI) *
                            Math.cos(pl.getDouble("latitude") / 360 * 2 * Math.PI))))

                    return@filter distance < 5 || text_coord == ""
                }
            }

            val scaleTranslate = activity!!.resources.displayMetrics.density

            var count_places = 0
            for(i in 0 until sorted_places_data.size) {
                if(count_places < 5) {
                    val place_block = activity!!.layoutInflater.inflate(R.layout.activity_main_place_block, null)
                    val place_image = place_block.findViewById<ImageView>(R.id.main_block_place_image)
                    val place_title = place_block.findViewById<TextView>(R.id.main_block_place_title)
                    val place_description = place_block.findViewById<TextView>(R.id.main_block_place_description)
                    val place_to_location = place_block.findViewById<ImageButton>(R.id.main_block_place_to_location)

                    place_title.text = sorted_places_data[i].getString("title")
                    //all_titles.add(sorted_places_data[i].getString("title"))
                    place_description.text = sorted_places_data[i].getString("description")
                    //all_descriptions.add(sorted_places_data[i].getString("description"))

                    val more_info = object : View.OnClickListener {

                        override fun onClick(view: View) {
                            openPlaceFragment.onDraw(mainActivity!!, sorted_places_data[i])
                            fm!!.beginTransaction().hide(activeFragment).show(openPlaceFragment).commit()
                        }
                    }

                    /*if(places_avatars.size > count_places && places_avatars["key_" +
                                    sorted_places_data[count_places].getInt("id")] != null) {
                        place_image.setImageBitmap(places_avatars["key_" +
                                sorted_places_data[count_places].getInt("id")])
                    } else {
                    }*/

                    val url_image = if(sorted_places_data[i].getString("avatar").substring(0, 4) == "http") {
                        sorted_places_data[i].getString("avatar")
                    } else {
                        "https://admire.social/images/" + sorted_places_data[i].getString("avatar").substringAfterLast("/")
                    }

                    if(!activity!!.isDestroyed) {
                        Glide.with(activity!!)
                                .asBitmap()
                                .load(url_image)
                                .centerCrop()
                                .into(object : SimpleTarget<Bitmap>() {
                                    override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                                        places_avatars["key_" + sorted_places_data[count_places].getInt("id")] = resource

                                        place_image.setImageBitmap(resource)
                                    }
                                })
                    }

                    place_block.setOnClickListener(more_info)

                    place_to_location.setOnClickListener {
                        val to_map_intent = Intent(activity!! as Context, MapActivity::class.java)
                        to_map_intent.putExtra("to_location_latitude", sorted_places_data[i].getDouble("latitude"))
                        to_map_intent.putExtra("to_location_longitude", sorted_places_data[i].getDouble("longitude"))
                        activity!!.startActivity(to_map_intent)
                    }

                    linearLayout!!.addView(place_block)

                    count_places++
                }
            }
        }
        super.onPostExecute(result)
    }
}