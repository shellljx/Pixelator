package com.gmail.shellljx.pixelate.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat

/**
 * @Author: shell
 * @Email: shell@gmail.com
 * @Date: 2023/7/30
 * @Description:
 */
object PermissionUtils {

    fun goToApplicationDetail(context: Context) {
        val packageURI = Uri.parse("package:" + "com.gmail.shellljx.pixelate")
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI)
        context.startActivity(intent)
        return
    }

    fun permissionGranted(context: Context, permissions: Array<String>): Boolean {
        return permissions.all {
            permissionGranted(context = context, permission = it)
        }
    }

    fun permissionGranted(context: Context, permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}