package com.syncdev.geofence.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.syncdev.geofence.model.State

class GeofenceReceiver : BroadcastReceiver() {
    private val TAG = "GeofenceReceiver"

    override fun onReceive(context: Context, intent: Intent) {
            Log.i(TAG, "onReceive: ")
            val geofencingEvent = GeofencingEvent.fromIntent(intent)
            if (geofencingEvent!!.hasError()) {
                val errorCode = geofencingEvent.errorCode
                Log.e(TAG, "Geofence error: $errorCode")
                return
            }
            val geofenceTransition = geofencingEvent.geofenceTransition
            val notificationService = NotificationService(context)
            val locationAddress = intent.getStringExtra("address").toString()
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                notificationService.showNotification(locationAddress, State.Arrived)
            } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                notificationService.showNotification(locationAddress, State.Left)
            }

        }

}
