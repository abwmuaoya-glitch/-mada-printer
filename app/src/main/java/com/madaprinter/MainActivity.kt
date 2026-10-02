
package com.madaprinter

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.*
import android.graphics.Color

class MainActivity : Activity() {

    private val adapter: BluetoothAdapter? =
        BluetoothAdapter.getDefaultAdapter()

    private lateinit var status: TextView
    private lateinit var devices: LinearLayout
    private lateinit var trial: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 35, 28, 20)
            setBackgroundColor(Color.WHITE)
        }

        val title = TextView(this).apply {
            text = "Mada Printer"
            textSize = 27f
            setTextColor(Color.rgb(20, 80, 150))
            gravity = Gravity.CENTER
        }

        status = TextView(this).apply {
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 20)
        }

        trial = TextView(this).apply {
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 12)
        }

        val pairButton = Button(this).apply {
            text = "إعدادات البلوتوث"
            setOnClickListener {
                startActivity(
                    Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                )
            }
        }

        val printSettings = Button(this).apply {
            text = "إعدادات خدمة الطباعة"
            setOnClickListener {
                startActivity(
                    Intent(Settings.ACTION_PRINT_SETTINGS)
                )
            }
        }

        val refresh = Button(this).apply {
            text = "تحديث الطابعات"
            setOnClickListener { loadDevices() }
        }

        devices = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        layout.addView(title)
        layout.addView(status)
        layout.addView(trial)
        layout.addView(pairButton)
        layout.addView(printSettings)
        layout.addView(refresh)
        layout.addView(devices)

        setContentView(ScrollView(this).apply {
            addView(layout)
        })

        requestBluetoothPermission()
        updateTrial()
    }

    private fun requestBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= 31 &&
            checkSelfPermission(
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                100
            )
        } else {
            loadDevices()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode, permissions, grantResults
        )

        if (requestCode == 100 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            loadDevices()
        }
    }

    private fun loadDevices() {
        devices.removeAllViews()

        try {
            if (adapter == null) {
                status.text = "البلوتوث غير مدعوم"
                return
            }

            if (!adapter.isEnabled) {
                status.text = "يرجى تشغيل البلوتوث"
                return
            }

            status.text = "الأجهزة المقترنة"

            val paired = adapter.bondedDevices

            if (paired.isEmpty()) {
                status.text = "لا توجد طابعات مقترنة"
                return
            }

            paired.forEach { device ->
                val button = Button(this).apply {
                    text = "${device.name ?: "جهاز"}\n${device.address}"

                    setOnClickListener {
                        getSharedPreferences(
                            "mada_printer",
                            MODE_PRIVATE
                        ).edit()
                            .putString(
                                "printer_mac",
                                device.address
                            )
                            .apply()

                        Toast.makeText(
                            this@MainActivity,
                            "تم اختيار الجهاز",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                devices.addView(button)
            }

        } catch (e: SecurityException) {
            status.text = "يرجى منح صلاحية البلوتوث"
        }
    }

    private fun updateTrial() {
        val used = TrialManager(this).used()
        val remaining = (10 - used).coerceAtLeast(0)

        trial.text = if (remaining > 0) {
            "المهام المجانية المتبقية: $remaining من 10"
        } else {
            "انتهت التجربة. اختر خطة تفعيل."
        }
    }

    override fun onResume() {
        super.onResume()
        if (::trial.isInitialized) updateTrial()
    }
}
