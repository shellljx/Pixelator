package com.gmail.shellljx.pixelate.panel

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.gmail.shellljx.pixelate.R
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.service.panel.AbsPanel

/**
 * @Author: shell
 * @Email: shellljx@gmail.com
 * @Date: 2023/6/24
 * @Description:
 */
@Keep
class ProgressPanel(context: Context) : AbsPanel(context) {

    private lateinit var mMessageView: TextView
    private var message: String? = null

    override val tag: String
        get() = "ProgressPanel"

    override fun onBindVEContainer(container: IContainer) {
    }

    override fun getLayoutId(): Int {
        return R.layout.panel_progress_layout
    }

    override fun onViewCreated(view: View?) {
        view ?: return
        mMessageView = view.findViewById(R.id.messageView)
    }

    override fun onPayloadUpdate(any: Any) {
        message = any as? String
    }

    override fun onAttach() {
        mMessageView.isVisible = message != null
        mMessageView.text = message ?: ""
    }

    override fun onDetach() {
        super.onDetach()
    }
}