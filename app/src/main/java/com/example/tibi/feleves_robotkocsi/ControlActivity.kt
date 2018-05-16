package com.example.tibi.feleves_robotkocsi

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.control_layout.*
import java.io.IOException
import java.util.*

/**
 * Created by Tibi on 2018. 05. 13..
 */
class ControlActivity: AppCompatActivity(), SensorEventListener {

    lateinit var sensorManager: SensorManager

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }
    var strt: Boolean = false
    var stp: Boolean = false
    var xa: Int = 0
    var ya: Int = 0
    override fun onSensorChanged(event: SensorEvent?) {
        accelerometer_data.text="x=${event!!.values[0]}\n\n" +
                                "y = ${event!!.values[1]}"


            xa = (event!!.values[0]).toInt()
            ya = (event!!.values[1]).toInt()

        // "z = ${event!!.values[2]}"
    if(strt==true) {
        sendCommand("${xa}")
        sendCommand("${ya}")
    }
        if(stp==true)
        {
            sendCommand("100")
            sendCommand("100")
        }
    }

    companion object {
        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket: BluetoothSocket? = null
        lateinit var m_progress: ProgressDialog
        lateinit var m_bluetoothAdapter: BluetoothAdapter
        var m_isConnected: Boolean = false
        lateinit var m_address: String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.control_layout)
        m_address = intent.getStringExtra(SelectDeviceActivity.EXTRA_ADDRESS)

        ConnectToDevice(this).execute()

        control_led_disconnect.setOnClickListener { disconnect() }
        on.setOnClickListener {strt=true ; stp=false}
        off.setOnClickListener {stp=true ; strt=false}


        //sendCommand("a")


    sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL)
}

    private fun sendCommand(input: String) {
        if (m_bluetoothSocket != null) {
            try{
                m_bluetoothSocket!!.outputStream.write(input.toInt()/*toByteArray()*/)
            } catch(e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun disconnect() {
        if (m_bluetoothSocket != null) {
            try {
                m_bluetoothSocket!!.close()
                m_bluetoothSocket = null
                m_isConnected = false
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        finish()
    }

    private class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>() {
        private var connectSuccess: Boolean = true
        private val context: Context

        init {
            this.context = c
        }

        override fun onPreExecute() {
            super.onPreExecute()
            m_progress = ProgressDialog.show(context, "Connecting...", "please wait")
        }

        override fun doInBackground(vararg p0: Void?): String? {
            try {
                if (m_bluetoothSocket == null || !m_isConnected) {
                    m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = m_bluetoothAdapter.getRemoteDevice(m_address)
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    m_bluetoothSocket!!.connect()
                }
            } catch (e: IOException) {
                connectSuccess = false
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (!connectSuccess) {
                Log.i("data", "couldn't connect")
            } else {
                m_isConnected = true
            }
            m_progress.dismiss()
        }
    }
}