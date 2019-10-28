package social.admire.admire

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.TextInputLayout
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import social.admire.admire.FragmentController.mainActivity
import java.io.FileNotFoundException
import java.io.InputStream

class AddPlaceFragment : Fragment() {

    val tags_array = ArrayList<String>()
    var layout_view: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        layout_view = inflater.inflate(R.layout.fragment_add_place, container, false)

        val add_place_title_layout = layout_view!!.findViewById<TextInputLayout>(R.id.add_place_title_layout)
        val add_place_title_edittext = layout_view!!.findViewById<EditText>(R.id.add_place_title_edittext)
        add_place_title_edittext.setOnFocusChangeListener { v, hasFocus ->
            run {
                if (add_place_title_edittext.text.toString().isEmpty() && !hasFocus) {
                    add_place_title_layout.error = "Введите названия места"
                } else {
                    add_place_title_layout.error = ""
                }
            }
        }
        val add_place_description_layout = layout_view!!.findViewById<TextInputLayout>(R.id.add_place_description_layout)
        val add_place_description_edittext = layout_view!!.findViewById<EditText>(R.id.add_place_description_edittext)
        add_place_description_edittext.setOnFocusChangeListener { v, hasFocus ->
            run {
                if (add_place_description_edittext.text.toString().isEmpty() && !hasFocus) {
                    add_place_description_layout.error = "Введите описание места"
                } else {
                    add_place_description_layout.error = ""
                }
            }
        }
        layout_view!!.findViewById<RatingBar>(R.id.add_place_rating).setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            run {

            }
        }

        val add_place_tags_edittext = layout_view!!.findViewById<EditText>(R.id.add_place_tags_edittext)
        add_place_tags_edittext.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                for(i in 0..s.length - 1) {
                    if(s[i].toString() == " ") {
                        val new_tag = s.toString().replace(" ", "")
                        tags_array.add(new_tag)
                        updataTags()
                        add_place_tags_edittext.text.clear()
                    }
                }
            }
        })

        return layout_view
    }

    fun updataTags() {
        val add_place_tags_list = layout_view!!.findViewById<LinearLayout>(R.id.add_place_tags_list)
        add_place_tags_list.removeAllViews()
        for(i in 0 until tags_array.size) {
            val tag_block = layoutInflater.inflate(R.layout.activity_add_place_tag, null)
            val tag_text = tag_block.findViewById<TextView>(R.id.activity_add_place_tag_text)
            val tag_button = tag_block.findViewById<ImageButton>(R.id.activity_add_place_tag_button)
            tag_text.text = tags_array[i]
            tag_button.setOnClickListener {
                tags_array.remove(tags_array[i])
                updataTags()
            }
            add_place_tags_list.addView(tag_block)
        }
    }

    fun addImage(view: View) {
        val photoPickerIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(photoPickerIntent, 100)
    }

    fun addCoord(view: View) {
        Toast.makeText(mainActivity, "Функция находится в разработке", Toast.LENGTH_LONG).show()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            100 -> if (resultCode == Activity.RESULT_OK) {
                val selectedImage = data!!.data
                var imageStream: InputStream? = null
                try {
                    imageStream = mainActivity!!.getContentResolver().openInputStream(selectedImage!!)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }

                val rect = Rect()
                val options = BitmapFactory.Options()
                options.outHeight = 120
                options.outWidth = 120
                options.inSampleSize = 4
                val SelectedImage = BitmapFactory.decodeStream(imageStream, rect, options)
                val new_image = ImageView(mainActivity)
                val lp = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                lp.setMargins(16, 16, 16, 16)
                new_image.layoutParams = lp
                new_image.setImageBitmap(SelectedImage)
                layout_view!!.findViewById<LinearLayout>(R.id.add_place_images).addView(new_image)
            }
        }
    }
}
