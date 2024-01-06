package com.gmail.shellljx.pixelate.panel

import android.content.Context
import android.view.View
import com.gmail.shellljx.pixelate.R
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.service.panel.AbsPanel

class ImageEffectPanel(context: Context) : AbsPanel(context) {

    override val tag: String
        get() = ImageEffectPanel::class.java.simpleName

    private lateinit var mMosaicLayout: View

    override fun onBindVEContainer(container: IContainer) {
    }

    override fun getLayoutId() = R.layout.panel_image_effects_layout

    override fun onViewCreated(view: View?) {
        view ?: return
        mMosaicLayout = view.findViewById(R.id.mosaic_layout)
        mMosaicLayout.setOnClickListener {
            mContainer.getPanelService()?.showPanel(MosaicPanel::class.java)
        }
    }
}