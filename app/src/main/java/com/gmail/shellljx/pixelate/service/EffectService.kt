package com.gmail.shellljx.pixelate.service

import com.gmail.shellljx.pixelate.EffectItem
import com.gmail.shellljx.pixelator.EffectType
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.IService

class EffectService : IEffectService {
    private lateinit var mContainer: IContainer
    private val effectList = arrayListOf<EffectItem>()
    private val effectChangedObservers = arrayListOf<EffectChangedObserver>()
    override fun onStart() {
        effectList.add(EffectItem(0, EffectType.TypeMosaic, "/sdcard/PixelatorResources/0.jpg", ""))
        effectList.add(EffectItem(1, EffectType.TypeImage, "/sdcard/PixelatorResources/1.jpg", "/sdcard/PixelatorResources/1.jpg"))
        effectList.add(EffectItem(2, EffectType.TypeImage, "/sdcard/PixelatorResources/2.png", "/sdcard/PixelatorResources/2.png"))
        effectList.add(EffectItem(3, EffectType.TypeImage, "/sdcard/PixelatorResources/3.png", "/sdcard/PixelatorResources/3.png"))
        effectList.add(EffectItem(4, EffectType.TypeImage, "/sdcard/PixelatorResources/4.png", "/sdcard/PixelatorResources/4.png"))
        effectList.add(EffectItem(5, EffectType.TypeImage, "/sdcard/PixelatorResources/5.png", "/sdcard/PixelatorResources/5.png"))
        effectList.add(EffectItem(6, EffectType.TypeImage, "/sdcard/PixelatorResources/6.png", "/sdcard/PixelatorResources/6.png"))
        effectList.add(EffectItem(7, EffectType.TypeImage, "/sdcard/PixelatorResources/7.png", "/sdcard/PixelatorResources/7.png"))
        effectList.add(EffectItem(8, EffectType.TypeImage, "/sdcard/PixelatorResources/8.png", "/sdcard/PixelatorResources/8.png"))
        effectList.add(EffectItem(9, EffectType.TypeImage, "/sdcard/PixelatorResources/9.png", "/sdcard/PixelatorResources/9.png"))
        effectList.add(EffectItem(10, EffectType.TypeImage, "/sdcard/PixelatorResources/10.png", "/sdcard/PixelatorResources/10.png"))
    }

    override fun bindVEContainer(container: IContainer) {
        mContainer = container
    }

    override fun onStop() {

    }

    override fun getEffects(): List<EffectItem> {
        return effectList
    }

    override fun addEffectChangedObserver(observer: EffectChangedObserver) {
        effectChangedObservers.add(observer)
    }
}

interface IEffectService : IService {
    fun getEffects(): List<EffectItem>
    fun addEffectChangedObserver(observer: EffectChangedObserver)
}

interface EffectChangedObserver {
    fun onEffectChanged(effects: List<EffectItem>)
}