package com.ingenieriajhr.controlsocketjhr

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

interface InterfaceAcelemetro{
    fun getData(x:Float,y: Float,z:Float)
}

class Acelerometro(private val context: Context) {

    lateinit var interfaceAcelemetro: InterfaceAcelemetro

    private val sensorManager: SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val sensorEventListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            // Obtiene las coordenadas X, Y y Z del acelerómetro
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Llama a la función onNewData con las coordenadas
            interfaceAcelemetro.getData(x,y,z)
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            // No se implementa en este ejemplo
        }
    }

    fun startListening() {
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun stopListening() {
        sensorManager.unregisterListener(sensorEventListener)
    }



    fun initAcelerometroRetur(interfaceAcelemetro: InterfaceAcelemetro){
        this.interfaceAcelemetro = interfaceAcelemetro
    }


}