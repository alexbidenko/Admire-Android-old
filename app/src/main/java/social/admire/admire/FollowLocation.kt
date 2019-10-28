package social.admire.admire

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import org.json.JSONArray
import android.R
import android.app.*
import android.content.Context.NOTIFICATION_SERVICE
import android.content.SharedPreferences
import android.support.v4.content.ContextCompat.getSystemService
import android.support.v4.app.NotificationCompat
import java.util.*
import android.os.Build
import android.support.v4.app.TaskStackBuilder
import social.admire.admire.ImagesData.places_data


class FollowLocation: Service() {

    var context: Context? = null
    var locationManager: LocationManager? = null

    private val locationListener = object : LocationListener {

        override fun onLocationChanged(location: Location) {
            showLocation(location)
        }

        override fun onProviderDisabled(provider: String) {
            checkEnabled()
        }

        override fun onProviderEnabled(provider: String) {
            checkEnabled()
            if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e("geo_search_error", "Не отобразилось")
            } else {
                Log.e("geo_search_error", "Запустилось")
            }
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            if (provider == LocationManager.GPS_PROVIDER) {
                //tvStatusGPS.setText("Status: " + String.valueOf(status));
            } else if (provider == LocationManager.NETWORK_PROVIDER) {
                //tvStatusNet.setText("Status: " + String.valueOf(status));
            }
        }
    }

    val triggered_notifications = ArrayList<Int>()

    private fun showLocation(location: Location?) {
        if (location == null) {
            Log.e("geo_search_error", "Не отцентровалось")
            return
        }
        if (location.provider == LocationManager.GPS_PROVIDER || location.provider == LocationManager.NETWORK_PROVIDER) {
            Log.e("geo_search_error", "Я слежу за тобой!")

            for(i in 0..all_coords.size - 1) {
                val earth_r = 6371
                val sin_1 = Math.sin(((all_coords[i].getDouble(0) - location.latitude) / 2) / 360 * 2 * Math.PI)
                val sin_2 = Math.sin(((all_coords[i].getDouble(1) - location.longitude) / 2) / 360 * 2 * Math.PI)
                val distance = 2 * earth_r * Math.asin(Math.sqrt(Math.abs(sin_1 * sin_2 + sin_1 * sin_2 *
                        Math.cos((location.latitude) / 360 * 2 * Math.PI) *
                        Math.cos((all_coords[i].getDouble(0)) / 360 * 2 * Math.PI))))

                if(distance < 0.5 && !triggered_notifications.contains(all_web_id[i])) {
                    Log.e("geo_search_error", "Место " + distance.toString())

                    triggered_notifications.add(all_web_id[i])

                    val intent = Intent(this, LocationNearNotify::class.java)
                    intent.putExtra("title", all_places_data[i])
                    intent.putExtra("route", all_route[i])

                    val pending_intent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

                    val alarmForNextEvent = getSystemService(ALARM_SERVICE) as AlarmManager;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmForNextEvent.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 100, pending_intent);
                    } else {
                        alarmForNextEvent.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 100, pending_intent);
                    }
                }
            }

            val sPref = getSharedPreferences("social.admire.admire", MODE_PRIVATE)
            sPref.edit()
                    .putFloat("last_latitude", location.latitude.toFloat())
                    .putFloat("last_longitude", location.longitude.toFloat())
                    .apply()
        }
    }

    private fun checkEnabled() {
        Log.e("geo_search_error", "Enabled: " + locationManager!!
                .isProviderEnabled(LocationManager.GPS_PROVIDER))
        Log.e("geo_search_error", "Enabled: " + locationManager!!
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        /*startActivity(new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));*/
    }

    val all_places_data = ArrayList<String>()
    val all_route = ArrayList<String>()
    val all_coords = ArrayList<JSONArray>()
    val all_web_id = ArrayList<Int>()

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mNotificationManager1 = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channel1 = NotificationChannel("my_channel_01",
                    "Отслеживание геолокации",
                    NotificationManager.IMPORTANCE_DEFAULT)
            mNotificationManager1.createNotificationChannel(channel1)
        }

        /*val dbHelperPlacesTable = DBHelperPlacesTable(this)
        val db = dbHelperPlacesTable.writableDatabase
        val c = db.query("seapl_place_table", null, null, null, null, null, null)
        if (c.moveToFirst()) {
            while (c.moveToNext()) {
                val route = c.getString(c.getColumnIndex("route"))
                all_route.add(route)
                val coord = JSONArray(route).getJSONArray(0)
                all_coords.add(coord)
                all_places_data.add(c.getString(c.getColumnIndex("title")))
                all_web_id.add(c.getInt(c.getColumnIndex("web_id")))
            }
        }
        c.close()*/
        for(i in 0 until places_data.size) {
            /*all_route.add(route)
            all_coords.add(coord)*/
            all_places_data.add(places_data[i].getString("title"))
            all_web_id.add(places_data[i].getInt("id"))
        }

        this.context = this

        locationManager = getSystemService(AppCompatActivity.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("geo_search_error", "Не запустилось")
        } else {
            Log.e("geo_search_error", "Запустилось")
            locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    (1000 * 120).toLong(), 10f, locationListener)
            locationManager!!.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, (1000 * 120).toLong(), 10f,
                    locationListener)
            checkEnabled()
        }

        return START_STICKY
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}