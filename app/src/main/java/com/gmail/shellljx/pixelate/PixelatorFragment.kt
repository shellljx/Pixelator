package com.gmail.shellljx.pixelate

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.gmail.shellljx.pixelate.extension.dp
import com.gmail.shellljx.pixelate.panel.MediasPanel
import com.gmail.shellljx.pixelate.service.*
import com.gmail.shellljx.pixelate.utils.FileUtils.syncImageToGallery
import com.gmail.shellljx.pixelate.utils.PermissionUtils
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
    private var mEffectService: IEffectService? = null

    private val readFilePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            val intent = Intent(Intent.ACTION_PICK, null)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            activity?.startActivityForResult(intent, OPEN_GALLERY_REQUEST_CODE)
        } else {
            Toast.makeText(requireContext(), getString(R.string.request_permission_failed), Toast.LENGTH_SHORT).show()
        }
    }

    private val writeFilePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            mCoreService?.save()
        } else {
            Toast.makeText(requireContext(), getString(R.string.request_permission_failed), Toast.LENGTH_SHORT).show()
        }
    }

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
        mCoreService = mContainer.getServiceManager().getService(PixelatorCoreService::class.java)
        mEffectService = mContainer.getServiceManager().getService(EffectService::class.java)
        mEffectService?.showPanel()
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
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (PermissionUtils.permissionGranted(requireContext(), permission)) {
            mContainer.getPanelService()?.showPanel(MediasPanel::class.java)
        } else if (shouldShowRequestPermissionRationale(permission)) {
            showGotoSettings(R.string.request_read_file_permission_desc)
        } else {
            readFilePermissionLauncher.launch(permission)
        }
    }

    override fun saveImage() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
            if (PermissionUtils.permissionGranted(requireContext(), permission)) {
                mCoreService?.save()
            } else if (shouldShowRequestPermissionRationale(permission)) {
                showGotoSettings(R.string.request_read_file_permission_desc)
            } else {
                writeFilePermissionLauncher.launch(permission)
            }
        } else {
            mCoreService?.save()
        }
    }

    private fun showGotoSettings(@StringRes message: Int) {
        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle(getString(R.string.title_request_permission))
            .setMessage(getString(message))
            .setPositiveButton(getString(R.string.setting)) { _, _ ->
                PermissionUtils.goToApplicationDetail(requireContext())
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    override fun saveSuccess(path: String) {
        syncImageToGallery(requireContext(), path)
        //File(path).delete()
        Toast.makeText(requireContext(), getString(R.string.save_image_success), Toast.LENGTH_SHORT).show()
    }
}

interface IImageDelegate : AbsDelegate {
    fun openAlbum()

    fun saveImage()
    fun saveSuccess(path: String)
}