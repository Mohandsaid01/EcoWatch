package com.example.ecowatch.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.ecowatch.R
import com.example.ecowatch.data.model.Species

private const val CHANNEL_ID = "eco_alerts"

object Notifier {

    private fun ensureChannel(ctx: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = ctx.getString(R.string.channel_name)
            val channel = NotificationChannel(
                CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT
            )
            (ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    fun notifyThreshold(ctx: Context, species: Species, message: String) {
        ensureChannel(ctx)

        val title = ctx.getString(R.string.notif_title)
        val text = if (species.address != null)
            ctx.getString(R.string.notif_template_with_addr, species.name, species.address)
        else
            ctx.getString(R.string.notif_template_no_addr, species.name)

        val notif = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText("$text – $message")
            .setStyle(NotificationCompat.BigTextStyle().bigText("$text\n$message"))
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(ctx).notify(species.id.toInt(), notif)
    }
}
