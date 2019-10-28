package social.admire.admire

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder

class LocationNearNotify: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        } else {
            null
        }

        val mBuilder = NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentTitle("Admire советы")
                .setContentText(intent!!.getStringExtra("title") + " рядом с вами!")
                /*.setStyle(NotificationCompat.BigTextStyle()
                        .bigText(textOfNotification)
                        .setBigContentTitle(titleOfNotification)
                        .setSummaryText("Напоминает"))*/
                .setChannelId("my_channel_01")
                .setAutoCancel(true)
                .setVibrate(longArrayOf(1500, 1000, 500))
                .setLights(-0xb5eb74, 3000, 5000)
                .setTicker("Admire напоминает!")
                .setWhen(System.currentTimeMillis())

        val resultIntent = Intent(context, MapActivity::class.java)
        resultIntent.putExtra("to_location", intent.getStringExtra("route"))

        val stackBuilder = TaskStackBuilder.create(context!!)
        stackBuilder.addParentStack(MapActivity::class.java)
        stackBuilder.addNextIntent(resultIntent)
        val resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        mBuilder.setContentIntent(resultPendingIntent)

        notificationManager!!.notify(1, mBuilder.build())
    }
}