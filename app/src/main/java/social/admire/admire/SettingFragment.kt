package social.admire.admire

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Switch
import social.admire.admire.FragmentController.mainActivity

class SettingFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val layout_view = inflater.inflate(R.layout.fragment_setting, container, false)
        val change_follow_location = layout_view.findViewById<Switch>(R.id.setting_change_follow_location)

        val sPref = mainActivity!!.getSharedPreferences("social.admire.admire", MODE_PRIVATE)
        change_follow_location.isChecked = sPref.getBoolean("follow_location", false)

        change_follow_location.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if((v as Switch).isChecked) {
                    sPref.edit().putBoolean("follow_location", true).apply()

                    val followLocationService = Intent(mainActivity!!, FollowLocation::class.java)
                    mainActivity!!.startService(followLocationService)
                } else {
                    sPref.edit().putBoolean("follow_location", false).apply()
                }
            }
        })

        return layout_view
    }
}
