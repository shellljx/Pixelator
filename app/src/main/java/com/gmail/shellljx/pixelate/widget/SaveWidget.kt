package com.gmail.shellljx.pixelate.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.widget.IWidget

class SaveWidget @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : androidx.appcompat.widget.AppCompatTextView(context, attrs), IWidget, OnClickListener {
    init {
        setOnClickListener(this)
    }

    override fun bindVEContainer(container: IContainer) {
    }

    override fun onWidgetActive() {
    }

    override fun onWidgetInactive() {
    }

    override fun onClick(v: View?) {

    }
}