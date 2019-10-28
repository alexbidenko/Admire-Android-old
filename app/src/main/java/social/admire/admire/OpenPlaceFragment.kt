package social.admire.admire

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import social.admire.admire.FragmentController.fm
import social.admire.admire.FragmentController.homeFragment
import org.apmem.tools.layouts.FlowLayout


class OpenPlaceFragment : Fragment() {

    var layout_view: View? = null

    var place_title: TextView? = null
    var tags_list: FlowLayout? = null
    var place_description: TextView? = null
    var place_rating: RatingBar? = null
    var place_images_group: LinearLayout? = null
    var to_place_create_route: ImageButton? = null
    var place_share: ImageButton? = null
    var close: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        layout_view = inflater.inflate(R.layout.fragment_open_place, container, false)

        place_title = layout_view!!.findViewById<TextView>(R.id.open_place_place_title)
        tags_list = layout_view!!.findViewById<FlowLayout>(R.id.open_place_tags_list)
        place_description = layout_view!!.findViewById<TextView>(R.id.open_place_place_description)
        place_images_group = layout_view!!.findViewById<LinearLayout>(R.id.open_place_images_group)
        to_place_create_route = layout_view!!.findViewById<ImageButton>(R.id.open_place_to_place_create_route)
        place_share = layout_view!!.findViewById<ImageButton>(R.id.open_place_place_share)
        close = layout_view!!.findViewById<Button>(R.id.open_place_close_alert_dialog)
        place_rating = layout_view!!.findViewById<RatingBar>(R.id.open_place_place_rating)

        return layout_view
    }

    fun onDraw(mapActivity: MapActivity, dp: JSONObject) {
        drawLayout(mapActivity, dp)

        to_place_create_route!!.setOnClickListener {
            mapActivity.fm!!.beginTransaction().hide(this).commit()
            mapActivity.setRouteRegime(true)
        }
        close!!.setOnClickListener {
            mapActivity.fm!!.beginTransaction().hide(this).commit()
        }
    }

    fun onDraw(mainActivity: MainActivity, dp: JSONObject) {
        drawLayout(mainActivity, dp)

        to_place_create_route!!.visibility = View.GONE
        close!!.setOnClickListener {
            fm!!.beginTransaction().hide(this).show(homeFragment).commit()
        }
    }

    fun drawLayout(context: Context, dp: JSONObject) {
        place_title!!.text = dp.getString("title")
        place_description!!.text = dp.getString("description")
        place_rating!!.rating = dp.getDouble("rating").toFloat() / 200000

        for(i in 0 until JSONArray(dp.getString("tags")).length()) {
            val tag_layout = layoutInflater.inflate(R.layout.tag_item, null) as LinearLayout
            (tag_layout.getChildAt(0) as TextView).text = JSONArray(dp.getString("tags")).getString(i)
            tags_list!!.addView(tag_layout)
        }

        val images = JSONArray(dp.getString("images"))
        place_images_group!!.removeAllViews()
        for (i in 0 until images.length()) {
            val image = ImageView(context)
            val lp = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 800)
            lp.setMargins(0, 0, 0, 8)
            image.layoutParams = lp
            image.scaleType = ImageView.ScaleType.CENTER_CROP

            place_images_group!!.addView(image)

            val url_image = if(images.get(i).toString().contains("http")) {
                images.get(i).toString()
            } else {
                "https://admire.social/images/" + images.get(i).toString().substringAfterLast("/")
            }

            Glide.with(layout_view!!)
                    .asBitmap()
                    .load(url_image)
                    .centerCrop()
                    .into(image)
        }
        place_share!!.setOnClickListener {
            try {
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(Intent.EXTRA_TEXT, "admire.social/link.php?p=${dp.getInt("id")}&s=${dp.getInt("status")}")
                sendIntent.type = "text/plain"
                startActivity(Intent.createChooser(sendIntent, "Поделиться местом"))
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }
}
