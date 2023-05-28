package com.gmail.shellljx.wrapper.service.render

import android.content.Context
import android.view.*
import androidx.lifecycle.*
import com.gmail.shellljx.wrapper.*

class RenderContainerService : IRenderContainerService, LifecycleObserver {
    private lateinit var mContainer: IContainer
    private var mRenderContext: IRenderContext? = null
    private var mRenderContainer: RenderContainer? = null
    private var mVideoRenderLayer: IRenderLayer? = null
    private var marginBottom = 0

    override fun onStart() {
        mContainer.getLifeCycleService()?.addObserver(this)
    }

    override fun createView(context: Context): ViewGroup {
        val renderContainer = RenderContainer(context)
        mRenderContainer = renderContainer
        val videoRenderLayer = SurfaceVideoRenderLayer(mContainer.getContext())
        mVideoRenderLayer = videoRenderLayer
        mRenderContainer?.bindVideoRenderLayer(videoRenderLayer, marginBottom)
        return renderContainer
    }

    override fun bindVEContainer(veContainer: IContainer) {
        mContainer = veContainer
    }

    override fun bindRenderContext(renderContext: IRenderContext) {
        mRenderContext = renderContext
        mVideoRenderLayer?.bindRenderContext(renderContext)
    }

    override fun getVideoHeight(): Int {
        return mVideoRenderLayer?.view()?.height ?: 0
    }

    override fun getVideoWidth(): Int {
        return mVideoRenderLayer?.view()?.width ?: 0
    }

    override fun setMarginBootom(margin: Int) {
        marginBottom = margin
    }

    override fun onStop() {
        mContainer.getLifeCycleService()?.removeObserver(this)
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

    /**
     * 返回视频渲染层宽度
     */
    fun getVideoWidth(): Int

    /**
     * 返回视频渲染层高度
     */
    fun getVideoHeight(): Int

    fun setMarginBootom(margin: Int)
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

    fun bindRenderContext(renderContext: IRenderContext)
}
