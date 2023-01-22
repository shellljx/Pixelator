package com.gmail.shellljx.pixelate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.HandlerThread
import android.os.Message
import com.gmail.shellljx.pixelator.Pixelator

class MainActivity : AppCompatActivity() {
    val pixelator = Pixelator()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val msg = Message.obtain()
    }
}
