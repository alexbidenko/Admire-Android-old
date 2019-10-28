package social.admire.admire

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import android.widget.ImageView
import social.admire.admire.TasksController.sliceTask
import java.net.HttpURLConnection
import java.net.URL

class GetImage_ImageView(url: String, image: ImageView, bitmap_by_key: MutableMap<String, Bitmap>?, key: String?) : AsyncTask<String, Void, Bitmap>() {
    internal val image = image
    internal val url = url
    internal val key = key
    internal val bitmap_by_key = bitmap_by_key

    override fun doInBackground(vararg urls: String): Bitmap? {
        val urldisplay = url
        var mIcon11: Bitmap? = null
        try {
            val url = URL(urldisplay)
            val connection = url.openConnection() as HttpURLConnection

            val `is` = connection.inputStream
            //InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(`is`)
        } catch (e: Exception) {
            Log.e("Ошибка", e.message)
            e.printStackTrace()
        }

        return mIcon11
    }

    override fun onPostExecute(result: Bitmap?) {
        if (result != null) {
            image.setImageBitmap(result)
            Log.e("Ошибка", "Картинка")

            if(bitmap_by_key != null) {
                if (!bitmap_by_key.contains(key))
                    bitmap_by_key[key!!] = result
            }
        }
        sliceTask()
    }
}