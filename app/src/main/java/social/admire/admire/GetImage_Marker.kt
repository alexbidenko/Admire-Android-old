package social.admire.admire

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.os.AsyncTask
import android.util.Log
import android.widget.ImageView
import com.here.android.mpa.common.GeoCoordinate
import com.here.android.mpa.common.Image
import com.here.android.mpa.mapping.MapContainer
import com.here.android.mpa.mapping.MapMarker
import social.admire.admire.ImagesData.user_avatar
import java.net.HttpURLConnection
import java.net.URL

class GetImage_Marker(val context: Context,
                      val url: String,
                      val map: MapContainer,
                      val geoCoordinate: GeoCoordinate,
                      val bitmap_by_key: MutableMap<String, Bitmap>,
                      var key: String) : AsyncTask<String, Void, Bitmap>() {

    private val paintDstIn = Paint()

    override fun doInBackground(vararg urls: String): Bitmap? {
        var mIcon11: Bitmap? = null

        try {
            val url = URL(url)
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
        if(result != null) {
            val cash_image = Bitmap.createScaledBitmap(result, 100, 100, true)

            var maskBitmap_1 = BitmapFactory.decodeResource(context.resources, R.drawable.round_mask)
            val ans = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
            val tempCanvas = Canvas(ans)

            maskBitmap_1 = Bitmap.createScaledBitmap(maskBitmap_1, 100, 100, true)
            paintDstIn.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)

            tempCanvas.drawBitmap(cash_image, 0f, 0f, null)
            tempCanvas.drawBitmap(maskBitmap_1, 0f, 0f, paintDstIn)


            val marker_img = Image()
            marker_img.bitmap = ans
            bitmap_by_key["marker_$key"] = ans
            val marker = MapMarker(geoCoordinate, marker_img)
            marker.title = key
            map.addMapObject(marker)
        }
        TasksController.sliceTask()
    }
}