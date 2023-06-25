package com.gmail.shellljx.pixelate

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import androidx.fragment.app.Fragment
import com.gmail.shellljx.pixelate.extension.dp
import com.gmail.shellljx.pixelate.panel.EffectsPanel
import com.gmail.shellljx.pixelate.panel.ProgressPanel
import com.gmail.shellljx.pixelate.service.*
import com.gmail.shellljx.wrapper.Config
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.service.AbsDelegate

class PixelatorFragment : Fragment(), IImageDelegate {
    companion object {
        const val OPEN_GALLERY_REQUEST_CODE = 0
        const val KEY_IMAGE_DELEGATE = "image_delegate"
    }

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
        mContainer.getServiceManager().registerBusinessService(
            listOf(
                PixelatorCoreService::class.java,
                TransformService::class.java,
                MaskLockService::class.java,
                EffectService::class.java
            )
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return mContainer.onCreateView(container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mContainer.getDelegateService()?.putDelegate(KEY_IMAGE_DELEGATE, this)
        mContainer.onViewCreated(view, savedInstanceState)
        mContainer.getPanelService()?.showPanel(EffectsPanel::class.java)
        mCoreService = mContainer.getServiceManager().getService(PixelatorCoreService::class.java)

        mCoreService?.setBrushResource(R.mipmap.ic_brush_blur)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                OPEN_GALLERY_REQUEST_CODE -> {
                    val path = getRealPathFromURI(context, data?.data) ?: return
                    mCoreService?.loadImage(path)
                }
            }
        }
    }

    fun getRealPathFromURI(context: Context?, uri: Uri?): String? {
        context ?: return null
        uri ?: return null
        var filePath = ""
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        if (cursor != null) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            if (cursor.moveToFirst()) {
                filePath = cursor.getString(columnIndex)
            }
            cursor.close()
        }
        return filePath
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

    override fun openAlbum() {
        val intent = Intent(Intent.ACTION_PICK, null)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        activity?.startActivityForResult(intent, OPEN_GALLERY_REQUEST_CODE)
    }
}

interface IImageDelegate : AbsDelegate {
    fun openAlbum()
}