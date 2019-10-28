package social.admire.admire

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import social.admire.admire.FragmentController.mainActivity
import java.util.ArrayList
import kotlin.random.Random

class HomeFragment : Fragment() {

    val all_place_blocks = ArrayList<View>()
    val all_titles = ArrayList<String>()
    val all_descriptions = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val layout_view = inflater.inflate(R.layout.fragment_home, container, false)
        val all_places = layout_view.findViewById<LinearLayout>(R.id.home_all_places)
        val main_scroll = layout_view.findViewById<ScrollView>(R.id.home_scroll)
        val main_search_text = layout_view.findViewById<EditText>(R.id.home_search_text)

        val main_app_name = layout_view.findViewById<TextView>(R.id.home_app_name)
        val main_fixed_title = layout_view.findViewById<LinearLayout>(R.id.home_fixed_title)

        val home_random_place_button = layout_view.findViewById<ImageButton>(R.id.home_random_place_button)
        val home_random_place_layout = layout_view.findViewById<RelativeLayout>(R.id.home_random_place_layout)
        val home_random_place_image = layout_view.findViewById<ImageView>(R.id.home_random_place_image)
        val home_random_place_title = layout_view.findViewById<TextView>(R.id.home_random_place_title)
        val home_random_place_description = layout_view.findViewById<TextView>(R.id.home_random_place_description)

        val scaleTranslate = resources.displayMetrics.density

        if(ImagesData.map_prepare.size == 0) {
            GetDataTask(mainActivity!!).execute()
        }

        val sPref = mainActivity!!.getSharedPreferences("social.admire.admire", Activity.MODE_PRIVATE)

        val last_latitude = sPref.getFloat("last_latitude", 0f)
        val last_longitude = sPref.getFloat("last_longitude", 0f)

        GetDataByCoord(last_latitude, last_longitude)
                .prepareLayoutDraw(mainActivity!!, all_places, last_latitude, last_longitude)
                .execute()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            main_scroll.setOnScrollChangeListener { view, scrollX, scrollY, oldScrollX, oldScrollY ->
                run {
                    ViewTitle(scrollY, scaleTranslate, main_app_name, main_fixed_title)
                }
            }
        }

        home_random_place_button.setOnClickListener {
            RandomPlace(home_random_place_layout, home_random_place_image, home_random_place_title, home_random_place_description)
        }

        main_search_text.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                val text = s.toString().toLowerCase()
                for(i in 0 until all_place_blocks.size) {
                    if(text.isEmpty()) {
                        all_place_blocks[i].visibility = View.VISIBLE
                    } else {
                        if (all_titles[i].toLowerCase().indexOf(text.toString()) > -1 ||
                                all_descriptions[i].toLowerCase().indexOf(text.toString()) > -1) {
                            all_place_blocks[i].visibility = View.VISIBLE
                        } else {
                            all_place_blocks[i].visibility = View.GONE
                        }
                    }
                }
            }
        })

        val login = sPref.getString("login", "")

        return layout_view
    }

    var random_latitude: Double = 0.0
    var random_longitude: Double = 0.0
    fun RandomPlace(
            random_place_layout: RelativeLayout,
            random_place_image: ImageView,
            random_place_title: TextView,
            random_place_description: TextView) {
        val random = Random.nextInt(ImagesData.places_data.size)

        random_place_title.text = ImagesData.places_data[random].getString("title")
        random_place_description.text = ImagesData.places_data[random].getString("description")
        TasksController.addNewTask("HIGH_PRIORITY_TASKS",
                GetImage_ImageView(ImagesData.places_data[random].getString("avatar_small"),
                        random_place_image, null, null))

        random_latitude = ImagesData.places_data[random].getDouble("latitude")
        random_longitude = ImagesData.places_data[random].getDouble("longitude")

        random_place_layout.visibility = View.VISIBLE
    }

    fun ToLocation(view: View) {
        val to_map_intent = Intent(mainActivity!!, MapActivity::class.java)
        to_map_intent.putExtra("to_location_latitude", random_latitude)
        to_map_intent.putExtra("to_location_longitude", random_longitude)
        startActivity(to_map_intent)
    }

    fun ViewTitle (scrollY: Int, scaleTranslate: Float, main_app_name: TextView, main_fixed_title: LinearLayout) {

        if (scrollY < 160) {
            main_app_name.textSize = 52f
            main_fixed_title.setPadding(0, Math.round((60 - scrollY / 2.6) * scaleTranslate).toInt(), 0, 0)
            main_fixed_title.setBackgroundColor(Color.TRANSPARENT)
        } else if(scrollY < 240) {
            main_fixed_title.setPadding(0, 0, 0, 0)
            main_app_name.textSize = (52 - ((scrollY - 160) / 3)) * 1f
            main_fixed_title.setBackgroundColor(Color.argb(Math.round((scrollY - 160) * 255f / 80f), 100, 100, 100))
        } else {
            main_fixed_title.setPadding(0, 0, 0, 0)
            main_app_name.textSize = 52 - 80 / 3f
            main_fixed_title.setBackgroundColor(Color.argb(255, 100, 100, 100))
        }
    }
}
