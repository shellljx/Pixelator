package com.gmail.shellljx.wrapper.service.render

import android.content.Context
import android.view.*
import androidx.annotation.Keep
import androidx.lifecycle.*
import com.gmail.shellljx.wrapper.*
import com.gmail.shellljx.wrapper.service.AbsService

@Keep
class RenderContainerService(container: IContainer) : AbsService(container), IRenderContainerService, LifecycleObserver {
    private var mRenderContext: IRenderContext? = null
    private var mRenderContainer: RenderContainer? = null
    private var mVideoRenderLayer: IVideoRenderLayer? = null
    private val mCustomRenderLayers = arrayListOf<IRenderLayer>()
    private var marginBottom = 0

    override fun onStart() {
        lifecycle.addObserver(this)
    }

    override fun createView(context: Context): ViewGroup {
        val renderContainer = RenderContainer(context)
        mRenderContainer = renderContainer
        val renderLayer = initRenderLayer()
        mRenderContainer?.bindVideoRenderLayer(renderLayer, marginBottom)
        return renderContainer
    }

    override fun bindVEContainer(container: IContainer) {
    }

    override fun bindRenderContext(renderContext: IRenderContext) {
        mRenderContext = renderContext
        initRenderLayer()
    }

    override fun addRenderLayer(layer: IRenderLayer) {
        if (!mCustomRenderLayers.contains(layer)) {
            mRenderContainer?.bindCustomRenderLayer(layer, marginBottom)
            mCustomRenderLayers.add(layer)
        }
    }

    override fun removeRenderLayer(layer: IRenderLayer) {
        mRenderContainer?.removeView(layer.view())
        mCustomRenderLayers.remove(layer)
    }

    private fun initRenderLayer(): IRenderLayer {
        val renderLayer = mVideoRenderLayer ?: SurfaceVideoRenderLayer(container.getContext())
        if (mVideoRenderLayer == null) {
            mVideoRenderLayer = renderLayer
        }
        mRenderContext?.let {
            renderLayer.bindRenderContext(it)
        }
        return renderLayer
    }

    override fun getRenderHeight(): Int {
        return mVideoRenderLayer?.view()?.height ?: 0
    }

    override fun getRenderWidth(): Int {
        return mVideoRenderLayer?.view()?.width ?: 0
    }

    override fun updateViewPort(offset: Int) {
        marginBottom = offset
    }

    override fun onStop() {
        lifecycle.removeObserver(this)
    }
}

interface IRenderContainerService : IService {
    /**
     * 返回渲染视图容器
     */
    fun createView(context: Context): ViewGroup

    /**
     * 录制或者编辑模块绑定渲染上下文
     * @param renderContext 渲染上下文
     */
    fun bindRenderContext(renderContext: IRenderContext)

    fun addRenderLayer(layer: IRenderLayer)

    fun removeRenderLayer(layer: IRenderLayer)

    /**
     * 返回视频渲染层宽度
     */
    fun getRenderWidth(): Int

    /**
     * 返回视频渲染层高度
     */
    fun getRenderHeight(): Int
    fun updateViewPort(offset: Int)
}

interface IRenderLayer {
    /**
     * 视图view
     */
    fun view(): View

    /**
     * 视图层级
     * level可以不是连续的，会根据相对大小顺序添加层级
     */
    fun level(): Int
}

interface IVideoRenderLayer : IRenderLayer {
    fun bindRenderContext(renderContext: IRenderContext)
}
