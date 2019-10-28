package social.admire.admire

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import social.admire.admire.FragmentController.activeFragment
import social.admire.admire.FragmentController.fm
import social.admire.admire.FragmentController.mainActivity
import social.admire.admire.FragmentController.myPageFragment
import social.admire.admire.ImagesData.user_avatar
import social.admire.admire.TasksController.addNewTask

class MyPageFragment : Fragment() {

    var layout_view: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        layout_view = inflater.inflate(R.layout.fragment_my_page, container, false)

        val sPref = mainActivity!!.getSharedPreferences("social.admire.admire", Context.MODE_PRIVATE)
        layout_view!!.findViewById<TextView>(R.id.my_page_login).text = sPref.getString("login", "")
        layout_view!!.findViewById<TextView>(R.id.my_page_first_name).text = sPref.getString("first_name", "")
        layout_view!!.findViewById<TextView>(R.id.my_page_last_name).text = sPref.getString("last_name", "")
        layout_view!!.findViewById<TextView>(R.id.my_page_email).text = sPref.getString("email", "")
        layout_view!!.findViewById<TextView>(R.id.my_page_city).text = sPref.getString("city", "")

        val avatar_image = layout_view!!.findViewById<ImageView>(R.id.my_page_avatar)

        var avatar = sPref.getString("avatar", "")
        if(user_avatar != null) {
            avatar_image.setImageBitmap(user_avatar)
        } else if(avatar != "") {
            avatar = "https://admire.social/avatars/$avatar"
        } else {
            avatar = if(sPref.getString("sex", "") == "male") {
                "https://i0.wp.com/www.winhelponline.com/blog/wp-content/uploads/2017/12/user.png?fit=256%2C256&quality=100&ssl=1"
            } else {
                "https://iconfree.net/uploads/icon/2017/7/5/avatar-user-profile-icon-3763-512x512.png"
            }
        }
        addNewTask("HIGH_PRIORITY_TASKS",
                GetImage_ImageView(avatar,
                        avatar_image,
                        null,
                        null))

        layout_view!!.findViewById<Button>(R.id.my_page_exit_account).setOnClickListener {
            ExitAccount()
        }

        return layout_view
    }

    fun ExitAccount() {
        val sPref = mainActivity!!.getSharedPreferences("social.admire.admire", Context.MODE_PRIVATE)
        sPref.edit()
                .remove("login")
                .remove("password")
                .apply()

        fm!!.beginTransaction().hide(activeFragment).show(myPageFragment).commit()
        activeFragment = myPageFragment
    }
}
