package com.example.ecowatch.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class SensorReader(context: Context) : SensorEventListener {

    private val mgr = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val temp = mgr.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
    private val hum  = mgr.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY)

    // Dernières valeurs lues (null si non dispo ou pas encore mesuré)
    @Volatile var temperature: Float? = null
        private set
    @Volatile var humidity: Float? = null
        private set

    fun start() {
        temp?.let { mgr.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        hum?.let  { mgr.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
    }

    fun stop() {
        mgr.unregisterListener(this)
    }

    override fun onSensorChanged(e: SensorEvent) {
        when (e.sensor.type) {
            Sensor.TYPE_AMBIENT_TEMPERATURE -> temperature = e.values.firstOrNull()
            Sensor.TYPE_RELATIVE_HUMIDITY   -> humidity    = e.values.firstOrNull()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
