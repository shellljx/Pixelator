package com.gmail.shellljx.wrapper

import android.view.View
import androidx.annotation.LayoutRes

class Config {
    var controlContainerConfig: ControlContainerConfig? = null
    var minPaintSize = 0 //画笔最小尺寸
    var maxPaintSize = 0 //画笔最大尺寸

    class ControlContainerConfig {
        @LayoutRes
        var layoutRes: Int = 0
        var instance: View? = null
    }
}
