package com.gmail.shellljx.pixelate.service

import com.gmail.shellljx.pixelate.EffectItem
import com.gmail.shellljx.pixelate.ServiceManager

class EffectService(private val serviceManager: ServiceManager) {
    private val effectList = arrayListOf<EffectItem>()
    init {
        effectList.add(EffectItem(1,"",""))
        effectList.add(EffectItem(2,"",""))
        effectList.add(EffectItem(3,"",""))

    }
    fun onStart() {
        serviceManager.effectPanel.setEffectItems(effectList)
    }
}