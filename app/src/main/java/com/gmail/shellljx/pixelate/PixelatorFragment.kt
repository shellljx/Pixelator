package com.gmail.shellljx.pixelate

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelStoreOwner
import com.gmail.shellljx.pixelate.extension.dp
import com.gmail.shellljx.pixelate.panel.MediasPanel
import com.gmail.shellljx.pixelate.panel.SaveResultPanel
import com.gmail.shellljx.pixelate.service.*
import com.gmail.shellljx.pixelate.utils.FileUtils.syncImageToGallery
import com.gmail.shellljx.pixelate.utils.PermissionUtils
import com.gmail.shellljx.pixelate.viewmodel.MainViewModel
import com.gmail.shellljx.wrapper.ActivityViewModelStoreProvider
import com.gmail.shellljx.wrapper.Config
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.extension.activityViewModels
import com.gmail.shellljx.wrapper.extension.safeAppCompatActivity

class PixelatorFragment : Fragment(), ActivityViewModelStoreProvider {

    private lateinit var mContainer: IContainer
    private val mainViewModel: MainViewModel by activityViewModels()
    private var mCoreService: IPixelatorCoreService? = null
    private var mEffectService: IEffectService? = null

    private val readFilePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            mContainer.getPanelService()?.showPanel(MediasPanel::class.java)
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
        mContainer.onViewCreated(view, savedInstanceState)
        mCoreService = mContainer.getServiceManager().getService(PixelatorCoreService::class.java)
        mEffectService = mContainer.getServiceManager().getService(EffectService::class.java)
        mEffectService?.showPanel()
        mCoreService?.setBrushResource(R.mipmap.ic_brush_blur)
        mainViewModel.openAlbumLiveData.observe(viewLifecycleOwner) {
            openAlbum()
        }
        mainViewModel.saveImageLiveData.observe(viewLifecycleOwner) {
            saveImage()
        }
        mainViewModel.savedImageLiveData.observe(viewLifecycleOwner) {
            saveSuccess(it)
        }
    }

    override fun getActivityViewModelStoreOwner(): ViewModelStoreOwner {
        return checkNotNull(safeAppCompatActivity(requireContext()))
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

    private fun openAlbum() {
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

    private fun saveImage() {
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

    private fun saveSuccess(path: String) {
        syncImageToGallery(requireContext(), path)
        mContainer.getPanelService()?.showPanel(SaveResultPanel::class.java, path)
        Toast.makeText(requireContext(), getString(R.string.save_image_success), Toast.LENGTH_SHORT).show()
    }
}