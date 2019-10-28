package social.admire.admire

import android.content.Intent
import android.database.Cursor
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle

import com.here.android.mpa.common.GeoBoundingBox
import com.here.android.mpa.common.GeoCoordinate
import com.here.android.mpa.common.OnEngineInitListener
import com.here.android.mpa.common.ViewObject
import com.here.android.mpa.mapping.Map
import com.here.android.mpa.mapping.MapContainer
import com.here.android.mpa.mapping.MapGesture
import com.here.android.mpa.mapping.MapState
import com.here.android.mpa.mapping.SupportMapFragment
import com.here.android.mpa.mapping.MapRoute
import com.here.android.mpa.routing.RouteManager
import com.here.android.mpa.routing.RouteOptions
import com.here.android.mpa.routing.RoutePlan
import com.here.android.mpa.routing.RouteResult
import com.here.android.mpa.common.GeoPolyline
import com.here.android.mpa.common.Image
import com.here.android.mpa.common.ViewRect
import com.here.android.mpa.mapping.MapMarker
import com.here.android.mpa.mapping.MapPolyline

import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.Manifest
import android.app.Application
import android.app.Service
import android.content.Context
import android.graphics.*
import android.support.design.widget.BottomSheetBehavior
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import java.util.ArrayList
import java.util.Arrays
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.activity_map_coord_hint.*
import social.admire.admire.ImagesData.has_weather_data
import social.admire.admire.ImagesData.map_prepare
import social.admire.admire.ImagesData.now_places_length
import social.admire.admire.ImagesData.places_avatars
import social.admire.admire.ImagesData.places_data
import social.admire.admire.ImagesData.places_map_icons
import social.admire.admire.TasksController.addNewTask
import social.admire.admire.TasksController.clearTasks

/*import com.here.android.mpa.mapping.customization.CustomizableScheme
import com.here.android.mpa.mapping.customization.CustomizableVariables
import com.here.android.mpa.mapping.customization.ZoomRange*/

class MapActivity : AppCompatActivity() {

    internal var map: Map? = null

    private var mapFragment: SupportMapFragment? = null

    private var locationManager: LocationManager? = null

    internal var dp: JSONObject? = null

    internal var all_places_container = MapContainer()
    internal var places_container = MapContainer()

    private val locationListener = object : LocationListener {

        override fun onLocationChanged(location: Location) {
            showLocation(location)
        }

        override fun onProviderDisabled(provider: String) {
            checkEnabled()
        }

        override fun onProviderEnabled(provider: String) {
            checkEnabled()
            if (ActivityCompat.checkSelfPermission(this@MapActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this@MapActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

    internal var me_marker: MapMarker? = null
    internal var is_start = true

    private val routeManagerListener = object : RouteManager.Listener {
        override fun onCalculateRouteFinished(errorCode: RouteManager.Error,
                                              result: List<RouteResult>) {

            if (errorCode == RouteManager.Error.NONE && result[0].route != null) {
                // create a map route object and place it on the map
                mapRoute = MapRoute(result[0].route)
                map!!.addMapObject(mapRoute)

                // Get the bounding box containing the route and zoom in (no animation)
                val gbb = result[0].route.boundingBox
                map!!.zoomTo(gbb, Map.Animation.NONE, Map.MOVE_PRESERVE_ORIENTATION)

                Log.d("create_route", String.format("Route calculated with %d maneuvers.",
                        result[0].route.maneuvers.size))
            } else {
                Log.d("create_route",
                        String.format("Route calculation failed: %s", errorCode.toString()))
            }
        }

        override fun onProgress(percentage: Int) {
            Log.d("create_route", String.format("... %d percent done ...", percentage))
        }
    }

    internal var is_route_regime = false
    internal var from_where: GeoCoordinate? = null

    val openPlaceFragment = OpenPlaceFragment()

    var fm: FragmentManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        checkPermissions()

        fm = supportFragmentManager

        fm!!.beginTransaction().add(R.id.map_open_place_container, openPlaceFragment, "1").hide(openPlaceFragment).commit()
    }

    override fun onPause() {
        super.onPause()
        locationManager!!.removeUpdates(locationListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        clearTasks ()
    }

    private fun initialize() {
        // Search for the map fragment to finish setup by calling init().
        mapFragment = supportFragmentManager.findFragmentById(R.id.mapfragment) as SupportMapFragment

        mapFragment!!.init { error ->
            if (error == OnEngineInitListener.Error.NONE) {
                // retrieve a reference of the map from the map fragment
                map = mapFragment!!.map
                // Set the map center to the Vancouver region (no animation)
                val sPref = getSharedPreferences("social.admire.admire", Context.MODE_PRIVATE)
                val last_map_lat = sPref.getFloat("last_map_latitude", 0f)
                val last_map_lng = sPref.getFloat("last_map_longitude", 0f)

                if(last_map_lat != 0f && last_map_lng != 0f) {
                    map!!.setCenter(GeoCoordinate(last_map_lat.toDouble(), last_map_lng.toDouble(), 0.0),
                            Map.Animation.NONE)
                } else {
                    map!!.setCenter(GeoCoordinate(44.7243900, 37.7675200, 0.0),
                            Map.Animation.NONE)
                }
                map!!.zoomLevel = 14.0

                map!!.addMapObject(all_places_container)
                map!!.addMapObject(places_container)

                for (i in places_data.indices) {
                    val coord = DoubleArray(2)
                    try {
                        coord[0] = places_data[i].getDouble("latitude")
                        coord[1] = places_data[i].getDouble("longitude")

                        val avatar = places_data[i].getString("avatar_small")
                        if(places_map_icons.contains("marker_" + places_data[i].getInt("id"))) {
                            val marker_img = Image()
                            marker_img.bitmap = places_map_icons.get("marker_" + places_data[i].getInt("id"))
                            val marker = MapMarker( GeoCoordinate(coord[0], coord[1]), marker_img)
                            marker.title = i.toString()
                            places_container.addMapObject(marker)
                        } else {
                            addNewTask("MID_PRIORITY_TASKS",
                                    GetImage_Marker(this,
                                            avatar,
                                            places_container,
                                            GeoCoordinate(coord[0], coord[1]),
                                            places_map_icons,
                                            places_data[i].getInt("id").toString()))
                        }
                    } catch (e: Exception) {
                    }

                }

                for (i in map_prepare.indices) {
                    val marker_img = Image()

                    val marker_bitmap = Bitmap.createBitmap(80, 80, Bitmap.Config.ARGB_8888)
                    val paint = Paint()
                    val canvas = Canvas(marker_bitmap)

                    paint.isAntiAlias = true
                    paint.color = Color.BLACK
                    paint.style = Paint.Style.FILL
                    canvas.drawCircle(40f, 40f, 40f, paint)

                    paint.color = Color.WHITE
                    paint.isAntiAlias = true
                    paint.textSize = 36f
                    val count = map_prepare[i].getInt("count")
                    val x = when {
                        count >= 1000 -> 0f
                        count >= 100 -> 12f
                        count >= 10 -> 16f
                        else -> 26f
                    }
                    canvas.drawText(count.toString(), x, 52f, paint)

                    marker_img.bitmap = marker_bitmap
                    val marker = MapMarker(GeoCoordinate(
                            map_prepare[i].getDouble("latitude"),
                            map_prepare[i].getDouble("longitude")), marker_img)
                    marker.title = "global_groupe_${count}"
                    Log.d("allPlacesData", "new")
                    all_places_container.addMapObject(marker)
                }

                val to_location_latitude = intent.getDoubleExtra("to_location_latitude", 0.0)
                val to_location_longitude = intent.getDoubleExtra("to_location_longitude", 0.0)
                if(to_location_latitude != 0.0 && to_location_longitude != 0.0) {
                    map!!.setCenter(GeoCoordinate(
                            to_location_latitude,
                            to_location_longitude, 0.0),
                            Map.Animation.LINEAR)
                }

                map!!.addTransformListener(object : Map.OnTransformListener {
                    override fun onMapTransformStart() {

                    }

                    override fun onMapTransformEnd(mapState: MapState) {
                        getWeather()

                        sPref.edit().putFloat("last_map_latitude", map!!.center.latitude.toFloat())
                                .putFloat("last_map_longitude", map!!.center.longitude.toFloat())
                                .apply()

                        if(map!!.zoomLevel > 10) {
                            places_container.isVisible = true
                            all_places_container.isVisible = false

                            var vis_places = 0
                            var invis_places = 0
                            for(i in 0 until places_container.allMapObjects.size) {
                                val it = places_container.allMapObjects[i] as MapMarker

                                if(map!!.zoomLevel > 18) {
                                    it.isVisible = true
                                } else {
                                    when {
                                        now_places_length > 300 -> {
                                            it.isVisible = invis_places / (vis_places + 1) > 10 / (map!!.zoomLevel - 9)

                                            if (it.isVisible) {
                                                vis_places++
                                            } else {
                                                invis_places++
                                            }
                                        }
                                        now_places_length > 240 -> {
                                            it.isVisible = invis_places / (vis_places + 1) > 8 / (map!!.zoomLevel - 9)

                                            if (it.isVisible) {
                                                vis_places++
                                            } else {
                                                invis_places++
                                            }
                                        }
                                        now_places_length > 150 -> {
                                            it.isVisible = invis_places / (vis_places + 1) > 5 / (map!!.zoomLevel - 9)

                                            if (it.isVisible) {
                                                vis_places++
                                            } else {
                                                invis_places++
                                            }
                                        }
                                        now_places_length > 90 -> {
                                            it.isVisible = invis_places / (vis_places + 1) > 2 / (map!!.zoomLevel - 9)

                                            if (it.isVisible) {
                                                vis_places++
                                            } else {
                                                invis_places++
                                            }
                                        }
                                        else -> it.isVisible = true
                                    }
                                }
                            }
                        } else {
                            places_container.isVisible = false
                            all_places_container.isVisible = true

                            all_places_container.allMapObjects.forEach {
                                val count = (it as MapMarker).title.substring(14).toInt()
                                when {
                                    map!!.zoomLevel > 9 -> it.isVisible = count > 1
                                    map!!.zoomLevel > 8 -> it.isVisible = count > 3
                                    map!!.zoomLevel > 7 -> it.isVisible = count > 6
                                    map!!.zoomLevel > 6 -> it.isVisible = count > 10
                                    map!!.zoomLevel > 5 -> it.isVisible = count > 20
                                    map!!.zoomLevel > 4 -> it.isVisible = count > 40
                                    map!!.zoomLevel > 3 -> it.isVisible = count > 70
                                    map!!.zoomLevel > 2 -> it.isVisible = count > 80
                                    map!!.zoomLevel > 1 -> it.isVisible = count > 100
                                    else -> it.isVisible = count > 400
                                }
                            }

                            clearTasks("MID_PRIORITY_TASKS")
                        }
                    }
                })

                getWeather()

                if (ActivityCompat.checkSelfPermission(this@MapActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this@MapActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.e("geo_search_error", "Не запустилось")
                } else {
                    Log.e("geo_search_error", "Запустилось")
                    locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            (1000 * 10).toLong(), 10f, locationListener)
                    locationManager!!.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER, (1000 * 10).toLong(), 10f,
                            locationListener)
                    checkEnabled()
                }

                mapFragment!!.mapGesture.addOnGestureListener(object : MapGesture.OnGestureListener {
                    override fun onPanStart() {

                    }

                    override fun onPanEnd() {

                    }

                    override fun onMultiFingerManipulationStart() {

                    }

                    override fun onMultiFingerManipulationEnd() {

                    }

                    override fun onMapObjectsSelected(list: List<ViewObject>): Boolean {
                        if(!is_route_regime)
                            try {
                                val index = Integer.parseInt((list[list.size - 1] as MapMarker).title)

                                for(i in 0 until places_data.size) {
                                    if(index == places_data[i].getInt("id")) {
                                        dp = places_data[i]
                                    }
                                }

                                openPlaceFragment.onDraw(this@MapActivity, dp!!)
                                fm!!.beginTransaction().show(openPlaceFragment).commit()

                            } catch (e: Exception) {}
                        return false
                    }

                    override fun onTapEvent(pointF: PointF?): Boolean {
                        if (is_route_regime && pointF != null) {
                            from_where = map!!.pixelToGeo(pointF)

                            CreateRoute()
                        }

                        return false
                    }

                    override fun onDoubleTapEvent(pointF: PointF): Boolean {
                        return false
                    }

                    override fun onPinchLocked() {

                    }

                    override fun onPinchZoomEvent(v: Float, pointF: PointF): Boolean {
                        return false
                    }

                    override fun onRotateLocked() {

                    }

                    override fun onRotateEvent(v: Float): Boolean {
                        return false
                    }

                    override fun onTiltEvent(v: Float): Boolean {
                        return false
                    }

                    override fun onLongPressEvent(pointF: PointF): Boolean {
                        return false
                    }

                    override fun onLongPressRelease() {

                    }

                    override fun onTwoFingerTapEvent(pointF: PointF): Boolean {
                        return false
                    }
                })

                /*val toolsNavLayout = getLayoutInflater().inflate(R.layout.activity_map_nav_layout, null);
                tools_nav_view.addHeaderView(toolsNavLayout);
                toolsNavLayout.map_normal.setOnClickListener {

                    val m_colorSchemeName = "colorScheme"
                    if (map != null && map.getCustomizableScheme(m_colorSchemeName) == null) {
                        map.createCustomizableScheme(m_colorSchemeName, Map.Scheme.NORMAL_DAY)
                        val m_colorScheme = map.getCustomizableScheme(m_colorSchemeName)
                        val range = ZoomRange(0.0, 20.0)
                        m_colorScheme.setVariableValue(CustomizableVariables.AirportArea.COLOR, Color.RED, range)
                        map!!.setMapScheme(m_colorScheme)
                    }
                }*/
            } else {
                println("ERROR: Cannot initialize Map Fragment")
            }
        }
    }

    fun OpenCoordHintPanel(view: View) {
        val coord_hint_card_behavior = BottomSheetBehavior.from(coord_hint_card);

        GetWeather(this@MapActivity).execute("https://admire.social/back/get-weather.php",
                "https://weather.api.here.com/weather/1.0/report.json?app_id=UdRH6PlISTlADYsW6mzl&app_code=lfrrTheP9nBedeJyy1NtIA&product=forecast_7days_simple&latitude=${map!!.center.latitude}&longitude=${map!!.center.longitude}&language=russian")

        val filtred_places = places_data.filter { pl ->
            run {
                val earth_r = 6371
                val pl_sin_1 = Math.sin(((pl.getDouble("latitude") - map!!.center.latitude) / 2) / 360 * 2 * Math.PI)
                val pl_sin_2 = Math.sin(((pl.getDouble("longitude") - map!!.center.longitude) / 2) / 360 * 2 * Math.PI)
                val pl_distance = 2 * earth_r * Math.asin(Math.sqrt(Math.abs(pl_sin_1 * pl_sin_2 + pl_sin_1 * pl_sin_2 *
                        Math.cos(map!!.center.latitude / 360 * 2 * Math.PI) * Math.cos(pl.getDouble("latitude") / 360 * 2 * Math.PI))))

                return@filter pl_distance < 300
            }
        }

        places_hint_scroll_view.removeAllViews()
        for(i in 0 until filtred_places.size) {
            if(i < 10) {
                val place_hint_layout = layoutInflater.inflate(R.layout.activity_map_hint_place, null)
                if (places_avatars.contains("key_" + filtred_places[i].getInt("id"))) {
                    place_hint_layout.findViewById<ImageView>(R.id.map_hint_place_image).setImageBitmap(
                            places_avatars["key_" + filtred_places[i].getInt("id")])
                } else {
                    Glide.with(this)
                            .asBitmap()
                            .load(filtred_places[i].getString("avatar_small"))
                            .centerCrop()
                            .into(object : SimpleTarget<Bitmap>() {
                                override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
                                    places_avatars["key_" + filtred_places[i].getInt("id")] = resource

                                    place_hint_layout.findViewById<ImageView>(R.id.map_hint_place_image).setImageBitmap(resource)
                                }
                            })
                }
                place_hint_layout.findViewById<TextView>(R.id.map_hint_place_title).text = filtred_places.get(i).getString("title")
                place_hint_layout.findViewById<TextView>(R.id.map_hint_place_description).text =
                        filtred_places.get(i).getString("description")

                place_hint_layout.setOnClickListener {
                    val bottomSheetBehavior = BottomSheetBehavior.from(coord_hint_card)
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

                    map!!.setCenter(GeoCoordinate(
                            filtred_places[i].getDouble("latitude"),
                            filtred_places[i].getDouble("longitude"), 0.0),
                            Map.Animation.LINEAR)
                }

                places_hint_scroll_view.addView(place_hint_layout)
            }
        }

        coord_hint_card_behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    var last_weather_lat = 0.0
    var last_weather_lng = 0.0
    fun getWeather() {
        val earth_r = 6371
        val sin_1 = Math.sin(((last_weather_lat - map!!.center.latitude) / 2) / 360 * 2 * Math.PI)
        val sin_2 = Math.sin(((last_weather_lng - map!!.center.longitude) / 2) / 360 * 2 * Math.PI)
        val distance = 2 * earth_r * Math.asin(Math.sqrt(Math.abs(sin_1 * sin_2 + sin_1 * sin_2 *
                Math.cos(map!!.center.latitude / 360 * 2 * Math.PI) * Math.cos(last_weather_lat / 360 * 2 * Math.PI))))

        Log.d("getWeather", distance.toString())

        if(distance > 5 && map!!.zoomLevel > 10) {
            has_weather_data = false
            last_weather_lat = map!!.center.latitude
            last_weather_lng = map!!.center.longitude

            clearTasks("MID_PRIORITY_TASKS")

            GetDataByCoord(last_weather_lat.toFloat(), last_weather_lng.toFloat())
                    .prepareMapDraw(this, places_container)
                    .execute()
        }
    }

    private fun checkPermissions() {
        val missingPermissions = ArrayList<String>()
        // check all required dynamic permissions
        for (permission in REQUIRED_SDK_PERMISSIONS) {
            val result = ContextCompat.checkSelfPermission(this, permission)
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission)
            }
        }
        if (missingPermissions.isNotEmpty()) {
            // request all missing permissions
            val permissions = missingPermissions
                    .toTypedArray()
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS)
        } else {
            val grantResults = IntArray(REQUIRED_SDK_PERMISSIONS.size)
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED)
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                    grantResults)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CODE_ASK_PERMISSIONS -> {
                for (index in permissions.indices.reversed()) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(this, "Required permission '" + permissions[index]
                                + "' not granted, exiting", Toast.LENGTH_LONG).show()
                        finish()
                        return
                    }
                }
                // all permissions were granted
                initialize()
            }
        }
    }

    fun ToMain(view: View) {
        val to_map_intent = Intent(this, MainActivity::class.java)
        startActivity(to_map_intent)
    }

    private fun showLocation(location: Location?) {
        if (location == null) {
            Log.e("geo_search_error", "Не отцентровалось")
            return
        }
        if (location.provider == LocationManager.GPS_PROVIDER || location.provider == LocationManager.NETWORK_PROVIDER) {
            Log.e("geo_search_error", "Отцентровалось")

            if (me_marker == null) {
                val me_image = Image()
                me_image.bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources,
                        R.drawable.i_icon), 100, 100, true)
                me_marker = MapMarker(GeoCoordinate(location.latitude, location.longitude), me_image)
                map!!.addMapObject(me_marker)
            } else {
                me_marker!!.coordinate = GeoCoordinate(location.latitude, location.longitude)
            }
            if (is_start && intent.getStringExtra("to_location") == null) {
                map!!.setCenter(GeoCoordinate(location.latitude, location.longitude), Map.Animation.LINEAR)
                is_start = false
            }

            val sPref = getSharedPreferences("social.admire.admire", Service.MODE_PRIVATE)
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

    fun CreateRoute() {
        if (map != null && mapRoute != null) {
            map!!.removeMapObject(mapRoute)
            mapRoute = null
        }

        setRouteRegime(false)

        // 2. Initialize RouteManager
        val routeManager = RouteManager()

        // 3. Select routing options
        val routePlan = RoutePlan()

        val routeOptions = RouteOptions()
        routeOptions.transportMode = RouteOptions.TransportMode.CAR
        routeOptions.routeType = RouteOptions.Type.FASTEST
        routePlan.routeOptions = routeOptions

        routePlan.addWaypoint(from_where)

        try {
            routePlan.addWaypoint(GeoCoordinate(JSONArray(dp!!.getString("route")).getJSONArray(0).getDouble(0),
                    JSONArray(dp!!.getString("route")).getJSONArray(0).getDouble(1)))
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val error = routeManager.calculateRoute(routePlan, routeManagerListener)
        if (error != RouteManager.Error.NONE) {
            Toast.makeText(applicationContext,
                    "Route calculation failed with: " + error.toString(), Toast.LENGTH_SHORT)
                    .show()
        }
    }

    fun setRouteRegime(new_val: Boolean) {
        is_route_regime = new_val
        if (new_val) {
            route_regime_panele.visibility = View.VISIBLE
        } else {
            route_regime_panele.visibility = View.GONE
        }
    }

    fun CanselRoute(view: View) {
        if (map != null && mapRoute != null) {
            map!!.removeMapObject(mapRoute)
            mapRoute = null
        }
        setRouteRegime(false)
    }

    companion object {

        // MapRoute for this activity
        private var mapRoute: MapRoute? = null

        private val REQUEST_CODE_ASK_PERMISSIONS = 1

        private val REQUIRED_SDK_PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }
}