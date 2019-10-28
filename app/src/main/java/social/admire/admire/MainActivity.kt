package social.admire.admire

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import social.admire.admire.FragmentController.activeFragment
import social.admire.admire.FragmentController.addPlaceFragment
import social.admire.admire.FragmentController.fm
import social.admire.admire.FragmentController.homeFragment
import social.admire.admire.FragmentController.mainActivity
import social.admire.admire.FragmentController.myPageFragment
import social.admire.admire.FragmentController.openPlaceFragment
import social.admire.admire.FragmentController.registFragment
import social.admire.admire.FragmentController.settingFragment
import social.admire.admire.ImagesData.places_avatars
import social.admire.admire.ImagesData.places_data
import social.admire.admire.TasksController.addNewTask
import social.admire.admire.TasksController.clearTasks
import java.util.ArrayList
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    var login: String? = ""

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()

        mainActivity = this

        fm = supportFragmentManager

        fm!!.fragments.forEach {
            fm!!.beginTransaction().remove(it).commit()
        }

        fm!!.beginTransaction().add(R.id.main_container, homeFragment, "1")
                .add(R.id.main_container, registFragment, "2").hide(registFragment)
                .add(R.id.main_container, myPageFragment, "3").hide(myPageFragment)
                .add(R.id.main_container, addPlaceFragment, "4").hide(addPlaceFragment)
                .add(R.id.main_container, settingFragment, "5").hide(settingFragment)
                .add(R.id.main_container, openPlaceFragment, "6").hide(openPlaceFragment).commit()

        main_navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        val sPref = getSharedPreferences("social.admire.admire", Activity.MODE_PRIVATE)
        if(sPref.getBoolean("follow_location", false)) {
            val followLocationService = Intent(this, FollowLocation::class.java)
            startService(followLocationService)
        }

        activeFragment = homeFragment

        login = sPref.getString("login", "")
    }

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                fm!!.beginTransaction().hide(activeFragment).show(homeFragment).commit()
                activeFragment = homeFragment
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_map -> {
                val intent = Intent(this, MapActivity::class.java)
                startActivity(intent)
                finish()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_my_page -> {
                if(login == "") {
                    fm!!.beginTransaction().hide(activeFragment).show(registFragment).commit()
                    activeFragment = registFragment
                } else {
                    fm!!.beginTransaction().hide(activeFragment).show(myPageFragment).commit()
                    activeFragment = myPageFragment
                }
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_add_place -> {
                fm!!.beginTransaction().hide(activeFragment).show(addPlaceFragment).commit()
                activeFragment = addPlaceFragment
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_settings -> {
                fm!!.beginTransaction().hide(activeFragment).show(settingFragment).commit()
                activeFragment = settingFragment
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }
}
