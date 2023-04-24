package com.gmail.shellljx.pixelate

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LaunchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        findViewById<TextView>(R.id.jump).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}