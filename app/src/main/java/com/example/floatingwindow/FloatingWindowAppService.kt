package com.example.floatingwindow

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.Button
import android.widget.EditText

class FloatingWindowAppService : Service() {
    lateinit var floatView: ViewGroup
    lateinit var floatWindowLayoutParams: WindowManager.LayoutParams
    private var LAYOUT_TYPE: Int? = null
    private lateinit var windowManager: WindowManager
    private lateinit var editText: EditText
    private lateinit var btn: Button

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        val metrics = applicationContext.resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val inflater = baseContext.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater

        floatView = inflater.inflate(R.layout.activity_main, null) as ViewGroup
        btn = floatView.findViewById(R.id.btn)
        editText = floatView.findViewById(R.id.edit_text)

        editText.setText(Common.currDes)
        editText.setSelection(editText.text.toString().length)
        editText.isCursorVisible = false

        LAYOUT_TYPE = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_TOAST
        }

        floatWindowLayoutParams = WindowManager.LayoutParams((width * 0.55f).toInt(), (height * 0.55f).toInt(), LAYOUT_TYPE!!, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT)

        floatWindowLayoutParams.gravity = Gravity.CENTER
        floatWindowLayoutParams.x = 0
        floatWindowLayoutParams.y = 0

        windowManager.addView(floatView, floatWindowLayoutParams)

        btn.setOnClickListener {
            stopSelf()
            windowManager.removeView(floatView)
            val back = Intent(this@FloatingWindowAppService, MainActivity::class.java)
            back.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(back)

            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    Common.currDes = editText.text.toString()
                }

                override fun afterTextChanged(p0: Editable?) {
                }
            })

            floatView.setOnTouchListener(object : View.OnTouchListener {
                val updatedFloatWindowLayoutParams = floatWindowLayoutParams
                var x = 0.0
                var y = 0.0
                var px = 0.0
                var py = 0.0

                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    when (event!!.action) {
                        MotionEvent.ACTION_DOWN -> {
                            x = updatedFloatWindowLayoutParams.x.toDouble()
                            y = updatedFloatWindowLayoutParams.y.toDouble()
                            px = event.rawX.toDouble()
                            py = event.rawY.toDouble()
                        }
                        MotionEvent.ACTION_MOVE -> {
                            updatedFloatWindowLayoutParams.x = (x + event.rawX - px).toInt()
                            updatedFloatWindowLayoutParams.y = (y + event.rawY - py).toInt()

                            windowManager.updateViewLayout(floatView, updatedFloatWindowLayoutParams)
                        }
                    }
                    return false
                }
            })

            editText.setOnTouchListener(object :View.OnTouchListener{
                override fun onTouch(p0: View?, p1: MotionEvent?): Boolean {
                    editText.isCursorVisible = true
                    val updatedFloatParamsFlag = floatWindowLayoutParams
                    updatedFloatParamsFlag.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN

                    windowManager.updateViewLayout(floatView, updatedFloatParamsFlag)
                    return false
                }

            })

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        stopSelf()
        windowManager.removeView(floatView)
    }
}