package com.gmail.shellljx.pixelate

import android.app.Activity
import android.content.Context
import com.gmail.shellljx.pixelate.panels.EffectsPanel
import com.gmail.shellljx.pixelate.panels.MiniScreenPanel
import com.gmail.shellljx.pixelate.service.EffectService

class ServiceManager(val context: Context) {

    val effectService by lazy { EffectService(this) }
    val effectPanel by lazy { EffectsPanel(context as Activity) }
    val miniScreenPanel by lazy { MiniScreenPanel(context as Activity) }

    fun start() {
        effectService.onStart()
    }
}