package com.gmail.shellljx.pixelate

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.gmail.shellljx.pixelate.service.PixelatorCoreService
import com.gmail.shellljx.wrapper.Config
import com.gmail.shellljx.wrapper.IContainer

class PixelatorFragment : Fragment() {

    private lateinit var mContainer: IContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!this::mContainer.isInitialized) {
            val config = Config()
            val controlConfig = Config.ControlContainerConfig()
            controlConfig.layoutRes = R.layout.layout_edit_controller
            config.controlContainerConfig = controlConfig
            mContainer = IContainer.Builder().setContext(requireContext()).setVEConfig(config).build()
        }
        mContainer.onCreate()
        mContainer.getServiceManager().registerBusinessService(listOf(PixelatorCoreService::class.java))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return mContainer.onCreateView(container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mContainer.onViewCreated(view, savedInstanceState)
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