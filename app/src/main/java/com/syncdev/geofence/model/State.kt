package com.syncdev.geofence.model

sealed class State {

    object Arrived:State()

    object Left:State()
}