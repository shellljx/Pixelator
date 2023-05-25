package com.gmail.shellljx.pixelate

import android.app.Activity
import android.content.Context
import com.gmail.shellljx.pixelate.panels.EffectsPanel
import com.gmail.shellljx.pixelate.services.EffectService

class ServiceManager(val context: Context) {

    val effectService by lazy { EffectService(this) }
    val effectPanel by lazy { EffectsPanel(context as Activity) }

    fun start() {
        effectService.onStart()
    }
}