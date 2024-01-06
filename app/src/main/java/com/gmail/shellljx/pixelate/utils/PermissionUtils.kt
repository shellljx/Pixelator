package com.gmail.shellljx.pixelate.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.gmail.shellljx.pixelate.R

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

    fun showGotoSettings(context: Context, @StringRes message: Int) {
        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle(context.getString(R.string.title_request_permission))
            .setMessage(context.getString(message))
            .setPositiveButton(context.getString(R.string.setting)) { _, _ ->
                goToApplicationDetail(context)
            }
            .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
}