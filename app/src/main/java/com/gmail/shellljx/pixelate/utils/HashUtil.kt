package com.gmail.shellljx.pixelate.utils

import java.security.MessageDigest

/**
 * @Author: shell
 * @Email: shellljx@gmail.com
 * @Date: 2023/8/11
 * @Description:
 */
object HashUtil {
    fun generateUniqueId(input: String): String {
        val bytes = input.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }
}