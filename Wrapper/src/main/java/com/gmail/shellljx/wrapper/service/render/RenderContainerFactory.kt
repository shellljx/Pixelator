package com.gmail.shellljx.wrapper.service.render

import android.content.Context

interface IRenderLayerFactory<in C : Context, out T : IVideoRenderLayer> {
    fun create(c: C): T
}

/**
 * @Date: 2023/2/9
 * @Description: 视频Render
 */
class VideoRenderContainerFactory : IRenderLayerFactory<Context, IVideoRenderLayer> {
    override fun create(c: Context): IVideoRenderLayer {
        return SurfaceVideoRenderLayer(c)
    }
}





