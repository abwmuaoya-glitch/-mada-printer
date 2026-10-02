
package com.madaprinter

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private val adapter: BluetoothAdapter? =
        BluetoothAdapter.getDefaultAdapter()

    private lateinit var status: TextView
    private lateinit var devices: LinearLayout
    private lateinit var trial: TextView

    private val bluetoothPermissionCode = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createInterface()
        updateTrial()
        requestBluetoothPermission()
    }

    private fun createInterface() {
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
            text = "جاري التحقق من البلوتوث..."
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 20, 0, 20)
        }

        trial = TextView(this).apply {
            textSize = 18f
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 12)
            setTextColor(Color.rgb(30, 120, 60))
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
            setOnClickListener {
                requestBluetoothPermission()
            }
        }

        val plansButton = Button(this).apply {
            text = "خطط التفعيل والاشتراك"
            setOnClickListener {
                showPlans()
            }
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
        layout.addView(plansButton)
        layout.addView(devices)

        setContentView(layout)
    }

    private fun requestBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            checkSelfPermission(
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                bluetoothPermissionCode
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
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == bluetoothPermissionCode) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                loadDevices()
            } else {
                status.text = "صلاحية البلوتوث مطلوبة"
            }
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

            val paired = adapter.bondedDevices

            if (paired.isEmpty()) {
                status.text = "لا توجد أجهزة مقترنة"
                return
            }

            status.text = "الأجهزة المقترنة"

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

    private fun showPlans() {
        val plans = arrayOf(
            "تفعيل يومي",
            "اشتراك شهري",
            "اشتراك سنوي",
            "تفعيل مدى الحياة"
        )

        AlertDialog.Builder(this)
            .setTitle("اختر خطة التفعيل")
            .setItems(plans) { _, which ->
                val selected = plans[which]

                AlertDialog.Builder(this)
                    .setTitle(selected)
                    .setMessage(
                        "سيتم توفير الدفع والتفعيل لهذه الخطة لاحقًا."
                    )
                    .setPositiveButton("حسنًا", null)
                    .show()
            }
            .setNegativeButton("إلغاء", null)
            .show()
    }

    override fun onResume() {
        super.onResume()

        if (::trial.isInitialized) {
            updateTrial()
        }
    }
}
