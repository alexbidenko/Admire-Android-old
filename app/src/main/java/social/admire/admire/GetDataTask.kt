package social.admire.admire

import android.content.ContentValues
import android.content.Context
import android.os.AsyncTask
import android.util.Log

import org.json.JSONArray
import social.admire.admire.ImagesData.map_prepare

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.HashMap

import javax.net.ssl.HttpsURLConnection

internal class GetDataTask(var context: Context) : AsyncTask<String, String, String>() {

    override fun onPreExecute() {
        super.onPreExecute()
    }

    override fun doInBackground(vararg params: String): String {
        val url: URL
        var response = ""
        //val postDataParams: HashMap<String, String>
        try {
            url = URL("https://admire.social/back/map_prepare.json")

            val conn = url.openConnection() as HttpURLConnection
            conn.readTimeout = 15000
            conn.connectTimeout = 15000
            conn.requestMethod = "GET"
            conn.doInput = true
            conn.doOutput = true


            val os = conn.outputStream
            val writer = BufferedWriter(
                    OutputStreamWriter(os, "UTF-8"))

            writer.flush()
            writer.close()
            os.close()
            val responseCode = conn.responseCode

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                var line: String
                val br = BufferedReader(InputStreamReader(conn.inputStream))
                response = br.readText()
            } else {
                response = ""
            }

            if (response != "") {
                val all_places = JSONArray(response)

                for (i in 0 until all_places.length()) {
                    map_prepare.add(all_places.getJSONObject(i))
                }
                /*val dbHelperMapPrepare = DBHelperMapPrepare(context)
                val db = dbHelperMapPrepare.writableDatabase
                val cv = ContentValues()

                val c = db.query("map_prepare", null, null, null, null, null, null)
                if (c.count != all_places.length()) {
                    val clearCount = db.delete("map_prepare", null, null)
                    //context.deleteDatabase("map_prepare")

                    val new_dbHelperMapPrepare = DBHelperMapPrepare(context)
                    val new_db = new_dbHelperMapPrepare.writableDatabase

                    for (i in 0 until all_places.length()) {
                        cv.put("count", all_places.getJSONObject(i).getInt("count"))
                        cv.put("latitude", all_places.getJSONObject(i).getDouble("latitude"))
                        cv.put("longitude", all_places.getJSONObject(i).getDouble("longitude"))
                        // вставляем запись и получаем ее ID
                        val rowID = new_db.insert("map_prepare", null, cv)
                    }
                }

                c.close()*/
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("writableDatabase", "error")
        }

        return ""
    }

    override fun onPostExecute(result: String) {
        super.onPostExecute(result)
    }
}
