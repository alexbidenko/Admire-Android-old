package social.admire.admire

import android.annotation.SuppressLint
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity

@SuppressLint("StaticFieldLeak")
object FragmentController {

    var mainActivity: MainActivity? = null

    var fm: FragmentManager? = null

    val homeFragment = HomeFragment()
    val registFragment = RegistFragment()
    val myPageFragment = MyPageFragment()
    val addPlaceFragment = AddPlaceFragment()
    val settingFragment = SettingFragment()

    val openPlaceFragment = OpenPlaceFragment()

    var activeFragment: Fragment = homeFragment
}