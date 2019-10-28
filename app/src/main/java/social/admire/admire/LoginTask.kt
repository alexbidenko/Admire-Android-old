package social.admire.admire

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import android.widget.TextView
import org.apache.http.client.entity.UrlEncodedFormEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.BasicResponseHandler
import org.apache.http.message.BasicNameValuePair
import org.apache.http.impl.client.DefaultHttpClient
import org.json.JSONObject
import android.view.View
import social.admire.admire.FragmentController.mainActivity


internal class LoginTask(val error_text: TextView) : AsyncTask<String, String, String>() {

    override fun doInBackground(vararg params: String?): String {
        Log.d("LoginTask", "start")
        val httpclient = DefaultHttpClient()
        if(params.size <= 2) {
            val http = HttpPost("https://admire.social/back/get-user.php")
            val nameValuePairs = ArrayList<BasicNameValuePair>()
            nameValuePairs.add(BasicNameValuePair("login",
                    String(params[0]!!.toByteArray(charset("UTF-8")), charset("ISO-8859-1"))))
            nameValuePairs.add(BasicNameValuePair("password",
                    String(params[1]!!.toByteArray(charset("UTF-8")), charset("ISO-8859-1"))))
            http.entity = UrlEncodedFormEntity(nameValuePairs)
            val response = httpclient.execute(http, BasicResponseHandler()) as String

            Log.d("LoginTask", response)
            if (response != "no") {
                val sPref = mainActivity!!.getSharedPreferences("social.admire.admire", Service.MODE_PRIVATE)

                val json_data = JSONObject(response)

                sPref.edit()
                        .putString("login", params[0])
                        .putString("password", params[1])
                        .putString("first_name", json_data.getString("first_name"))
                        .putString("last_name", json_data.getString("last_name"))
                        .putString("email", json_data.getString("email"))
                        .putString("city", json_data.getString("city"))
                        .putString("sex", json_data.getString("sex"))
                        .putString("avatar", json_data.getString("avatar"))
                        .apply()

                FragmentController.fm!!.beginTransaction().hide(FragmentController.activeFragment)
                        .show(FragmentController.myPageFragment).commit()
                FragmentController.activeFragment = FragmentController.myPageFragment
            } else {
                this.publishProgress("Неверный логин или пароль")
                //Toast.makeText(context, "Неверный логин или пароль", Toast.LENGTH_LONG).show()
            }
        } else {
            val http = HttpPost("https://admire.social/back/regist.php")
            val nameValuePairs = ArrayList<BasicNameValuePair>()
            nameValuePairs.add(BasicNameValuePair("login",
                    String(params[0]!!.toByteArray(charset("UTF-8")), charset("ISO-8859-1"))))
            nameValuePairs.add(BasicNameValuePair("first_name",
                    String(params[1]!!.toByteArray(charset("UTF-8")), charset("ISO-8859-1"))))
            nameValuePairs.add(BasicNameValuePair("last_name",
                    String(params[2]!!.toByteArray(charset("UTF-8")), charset("ISO-8859-1"))))
            nameValuePairs.add(BasicNameValuePair("email",
                    String(params[3]!!.toByteArray(charset("UTF-8")), charset("ISO-8859-1"))))
            nameValuePairs.add(BasicNameValuePair("city",
                    String(params[4]!!.toByteArray(charset("UTF-8")), charset("ISO-8859-1"))))
            nameValuePairs.add(BasicNameValuePair("sex", params[5]))
            nameValuePairs.add(BasicNameValuePair("password",
                    String(params[6]!!.toByteArray(charset("UTF-8")), charset("ISO-8859-1"))))
            nameValuePairs.add(BasicNameValuePair("pass_double",
                    String(params[7]!!.toByteArray(charset("UTF-8")), charset("ISO-8859-1"))))
            http.setEntity(UrlEncodedFormEntity(nameValuePairs))
            val response = httpclient.execute(http, BasicResponseHandler()) as String

            Log.d("LoginTask", response)
            if (response == "no_pass") {
                this.publishProgress("Пароли не совпадают")
            } else if (response == "no_login") {
                this.publishProgress("Логин уже занят")
            } else if (response == "no_email") {
                this.publishProgress("Адрес почты уже занят")
            } else {
                val sPref = mainActivity!!.getSharedPreferences("social.admire.admire", Service.MODE_PRIVATE)

                sPref.edit()
                        .putString("login", params[0])
                        .putString("first_name", params[1])
                        .putString("last_name", params[2])
                        .putString("email", params[3])
                        .putString("city", params[4])
                        .putString("sex", params[5])
                        .putString("password", params[6])
                        .putString("avatar", "")
                        .apply()

                FragmentController.fm!!.beginTransaction().hide(FragmentController.activeFragment)
                        .show(FragmentController.myPageFragment).commit()
                FragmentController.activeFragment = FragmentController.myPageFragment
            }
        }

        return ""
    }

    override fun onProgressUpdate(vararg values: String) {
        super.onProgressUpdate(*values)
        error_text.text = values[0]
        error_text.visibility = View.VISIBLE
    }
}