package com.syncdev.geofence.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.syncdev.geofence.ui.MainActivity
import com.syncdev.geofence.R
import com.syncdev.geofence.model.State

class NotificationService(private val context: Context) {
    companion object{
        const val GEOFENCE_CHANNEL_ID = "geofence_channel"
        const val GEOFENCE_CHANNEL_NAME = "Geofence"
        const val IMPORTANCE = NotificationManager.IMPORTANCE_DEFAULT
        const val DESCRIPTION = "Used display location alert"
    }

    private val notificationManager=context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


    fun showNotification(location:String,state:State){
        val activityPendingIntent= PendingIntent.getActivity(
            context,
            1,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        var contextText=""
        when(state){
            State.Arrived->contextText="You are arrived at $location"
            State.Left->contextText="You are left $location"
        }
        val notification= NotificationCompat.Builder(context, GEOFENCE_CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_my_location_24)
            .setContentTitle("Location geofence destination")
            .setContentText(contextText)
            .setContentIntent(activityPendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1,notification)
    }
}