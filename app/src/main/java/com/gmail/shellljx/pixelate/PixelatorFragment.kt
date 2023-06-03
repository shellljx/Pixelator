package com.gmail.shellljx.pixelate

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.gmail.shellljx.pixelate.extension.dp
import com.gmail.shellljx.pixelate.panel.EffectsPanel
import com.gmail.shellljx.pixelate.panel.MiniScreenPanel
import com.gmail.shellljx.pixelate.service.*
import com.gmail.shellljx.wrapper.Config
import com.gmail.shellljx.wrapper.IContainer

class PixelatorFragment : Fragment() {

    private lateinit var mContainer: IContainer
    private var mCoreService: IPixelatorCoreService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!this::mContainer.isInitialized) {
            val config = Config()
            val controlConfig = Config.ControlContainerConfig()
            controlConfig.layoutRes = R.layout.layout_control_pixelator
            config.controlContainerConfig = controlConfig
            config.minPaintSize = 10.dp()
            config.maxPaintSize = 50.dp()
            mContainer = IContainer.Builder().setContext(requireContext()).setVEConfig(config).build()
        }
        mContainer.onCreate()
        mContainer.getServiceManager().registerBusinessService(listOf(PixelatorCoreService::class.java, TransformService::class.java))
        mContainer.getRenderService()?.updateViewPort(200.dp())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return mContainer.onCreateView(container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mContainer.onViewCreated(view, savedInstanceState)
        mContainer.getPanelService()?.showPanel(EffectsPanel::class.java)
        mCoreService = mContainer.getServiceManager().getService(PixelatorCoreService::class.java)

        mCoreService?.setBrushResource(R.mipmap.ic_brush_blur)
        mCoreService?.loadImage("/sdcard/aftereffect/ae/tt/resource/assets/a3.png")
    }

    override fun onResume() {
        super.onResume()
        mContainer.onResume()
    }

    override fun onPause() {
        super.onPause()
        mContainer.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mContainer.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        mContainer.onDestroy()
    }
}