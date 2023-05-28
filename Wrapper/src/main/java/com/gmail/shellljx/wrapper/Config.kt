package com.gmail.shellljx.wrapper

import android.view.View
import androidx.annotation.LayoutRes

class Config {
    var controlContainerConfig: ControlContainerConfig? = null

    class ControlContainerConfig {
        @LayoutRes
        var layoutRes: Int = 0
        var instance: View? = null
    }
}
