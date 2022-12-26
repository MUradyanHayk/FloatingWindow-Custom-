package com.example.floatingwindow

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.AlertDialog
import android.app.PictureInPictureParams
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.core.widget.addTextChangedListener

class MainActivity : AppCompatActivity() {
    private lateinit var dialog: AlertDialog
    private lateinit var btn: Button
    private lateinit var editText: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn = findViewById(R.id.btn)
        editText = findViewById(R.id.edit_text)


        if (isServiceRunning()) {
            stopService(Intent(this@MainActivity, FloatingWindowAppService::class.java))
        }

        editText.setText(Common.currDes)
        editText.setSelection(editText.text.toString().length)

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Common.currDes = editText.text.toString()
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })


        btn.setOnClickListener {
            if (checkOverlayPermission()) {
                startService(Intent(this@MainActivity, FloatingWindowAppService::class.java))
                finish()
            } else {
                requestFloatingWindowPermission()
            }

//            val pictureInPictureParams = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                PictureInPictureParams.Builder().build()
//            } else {
//                TODO("VERSION.SDK_INT < O")
//            }
//            enterPictureInPictureMode(pictureInPictureParams)
        }
    }

    @SuppressLint("ServiceCast")
    private fun isServiceRunning(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (FloatingWindowAppService::class.java.name == service.service.className) {
                return true
            }
        }

        return false
    }

    private fun requestFloatingWindowPermission() {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
            .setTitle("Screen Overlay Permission Needed")
            .setMessage("Enable Display over the App from settings")
            .setPositiveButton("Open Settings") { dialog, witch ->
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${packageName}"))
                startActivityForResult(intent, RESULT_OK)
            }

        dialog = builder.create()
        dialog.show()
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            return true
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val pictureInPictureParams = PictureInPictureParams.Builder().build()
        enterPictureInPictureMode(pictureInPictureParams)

    }
}