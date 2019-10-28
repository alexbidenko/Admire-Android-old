package social.admire.admire

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_regist.view.*
import social.admire.admire.FragmentController.mainActivity

class RegistFragment : Fragment() {

    var layout_view: View? = null
    var new_error_text: TextView? = null
    var error_text: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        layout_view = inflater.inflate(R.layout.fragment_regist, container, false)

        layout_view!!.findViewById<TextView>(R.id.regist_want_enter_button).setOnClickListener {
            wantEnter()
        }
        layout_view!!.findViewById<TextView>(R.id.regist_want_regist_button).setOnClickListener {
            wantRegist()
        }
        layout_view!!.findViewById<TextView>(R.id.regist_enter_button).setOnClickListener {
            Enter()
        }
        layout_view!!.findViewById<TextView>(R.id.regist_regist_button).setOnClickListener {
            Regist()
        }

        new_error_text = layout_view!!.findViewById<TextView>(R.id.regist_new_error)
        error_text = layout_view!!.findViewById<TextView>(R.id.regist_error)

        return layout_view
    }

    fun wantEnter() {
        error_text!!.visibility = View.GONE
        layout_view!!.findViewById<LinearLayout>(R.id.regist_want_regist).setVisibility(View.GONE)
        layout_view!!.findViewById<LinearLayout>(R.id.regist_want_enter).setVisibility(View.VISIBLE)
    }

    fun wantRegist() {
        new_error_text!!.visibility = View.GONE
        layout_view!!.findViewById<LinearLayout>(R.id.regist_want_enter).setVisibility(View.GONE)
        layout_view!!.findViewById<LinearLayout>(R.id.regist_want_regist).setVisibility(View.VISIBLE)
    }

    fun Enter() {
        error_text!!.visibility = View.GONE

        LoginTask(error_text!!).execute(
                layout_view!!.findViewById<EditText>(R.id.regist_login).text.toString(),
                layout_view!!.findViewById<EditText>(R.id.regist_pass).text.toString());
    }

    fun Regist() {
        new_error_text!!.visibility = View.GONE

        var sex: String? = "male"

        if(layout_view!!.findViewById<RadioButton>(R.id.regist_new_sex_male).isChecked) {
            sex = "male"
        } else if(layout_view!!.findViewById<RadioButton>(R.id.regist_new_sex_famale).isChecked) {
            sex = "famale"
        }

        LoginTask(new_error_text!!).execute(
                layout_view!!.findViewById<EditText>(R.id.regist_new_login).text.toString(),
                layout_view!!.findViewById<EditText>(R.id.regist_new_first_name).text.toString(),
                layout_view!!.findViewById<EditText>(R.id.regist_new_last_name).text.toString(),
                layout_view!!.findViewById<EditText>(R.id.regist_new_email).text.toString(),
                layout_view!!.findViewById<EditText>(R.id.regist_new_city).text.toString(),
                sex,
                layout_view!!.findViewById<EditText>(R.id.regist_new_pass).text.toString(),
                layout_view!!.findViewById<EditText>(R.id.regist_new_repeat_pass).text.toString());
    }
}
