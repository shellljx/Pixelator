package com.gmail.shellljx.pixelator

import android.view.Surface

/**
 * @Author: lijinxiang
 * @Email: lijinxiang@shizhuang-inc.com
 * @Date: 2023/7/15
 * @Description:
 */
class MiniScreen(private val id: Long) : IMiniScreen {

    override fun onSurfaceCreated(surface: Surface) {
        if (id != 0L) {
            onMiniScreenSurfaceCreate(id, surface)
        }
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        if (id != 0L) {
            onMiniScreenSurfaceChanged(id, width, height)
        }
    }

    override fun onSurfaceDestroy() {
        if (id != 0L) {
            onMiniScreenSurfaceDestroy(id)
        }
    }

    private external fun onMiniScreenSurfaceCreate(id: Long, surface: Surface)
    private external fun onMiniScreenSurfaceChanged(id: Long, width: Int, height: Int)
    private external fun onMiniScreenSurfaceDestroy(id: Long)
}