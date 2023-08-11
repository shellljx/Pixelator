package com.gmail.shellljx.pixelate

enum class STATUS {
    NotDownload,
    Downloading,
    Downloaded
}

data class EffectItem(
        val id: Int,
        val type: Int,
        val cover: String,
        val url: String
) {
    var path: String? = null
    var status = STATUS.NotDownload
}