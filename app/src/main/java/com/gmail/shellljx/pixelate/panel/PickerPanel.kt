package com.gmail.shellljx.pixelate.panel

import android.content.Context
import android.view.*
import com.gmail.shellljx.pixelate.R
import com.gmail.shellljx.pixelate.view.OptionPickListener
import com.gmail.shellljx.pixelate.view.PickItem
import com.gmail.shellljx.pixelate.view.PickView
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.service.panel.AbsPanel
import com.gmail.shellljx.wrapper.service.panel.PanelConfig

/**
 * @Author: shell
 * @Email: shellljx@gmail.com
 * @Date: 2023/6/21
 * @Description:
 */
class PickerPanel(context: Context) : AbsPanel(context), OptionPickListener {

    override val tag: String
        get() = "PickerPanel"
    override val panelConfig: PanelConfig
        get() {
            val config = PanelConfig()
            config.exitAnim = R.anim.alpha_fade_out
            return config
        }

    private lateinit var mContainer: IContainer
    private lateinit var mPickView: PickView
    private var mPayload: PickPayload? = null
    override fun onBindVEContainer(container: IContainer) {
        mContainer = container
    }

    override fun getLayoutId(): Int {
        return R.layout.panel_settings_pick_layout
    }

    override fun onViewCreated(view: View?) {
        view ?: return
        mPickView = view.findViewById(R.id.pickview)
        mPickView.setOptionPickListener(this)
    }

    override fun onPayloadUpdate(any: Any) {
        mPayload = (any as? PickPayload)
        mPayload?.let {
            mPickView.setItems(it.data, it.selectPosition)
        }
    }

    override fun onPickOption(position: Int, byUser: Boolean) {
        if (byUser) {
            mPayload?.callback?.invoke(position)
            mContainer.getPanelService()?.hidePanel(mToken)
        }
    }

    data class PickPayload(val data: List<PickItem>, val selectPosition: Int, val callback: (Int) -> Unit)
}