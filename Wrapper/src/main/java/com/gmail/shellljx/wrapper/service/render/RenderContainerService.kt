package com.gmail.shellljx.wrapper.service.render

import android.animation.ValueAnimator
import android.content.Context
import android.view.*
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.lifecycle.*
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.IService
import com.gmail.shellljx.wrapper.core.IRenderContext

class RenderContainerService : IRenderContainerService, LifecycleObserver {
    private lateinit var mVEContainer: IContainer
    private var mRenderContext: IRenderContext? = null
    private var mRenderContainer: RenderContainer? = null
    private var mVideoRenderLayer: IVideoRenderLayer? = null
    private var mVideoRatio = AspectRatio.RATIO_9_16_INSIDE
    private var mCustomVideoRatio = 0f
    private var mRenderMarginTop = 0
    private var mWindowDisplayWidth = 0
    private var mWindowDisplayHeight = 0
    private var layoutAnimator: ValueAnimator? = null
    private var mChildrenLayers = arrayListOf<IRenderLayer>()
    private var mCustomLayers = arrayListOf<IRenderLayer>()
    private var middleLineHeight = 0
    override fun onStart() {
        mVEContainer.getLifeCycleService()?.addObserver(this)
    }

    override fun createView(context: Context): ViewGroup {
        val renderContainer = RenderContainer(context)
        mRenderContainer = renderContainer
        mVideoRenderLayer?.let {
            attachPreviewLayer(it)
        }
        return renderContainer
    }

    private val mContainerLayoutObserver = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            mRenderContainer?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
            mVideoRenderLayer?.view()?.layoutParams = generateLayoutParamsByRatio()
        }
    }

    override fun bindVEContainer(veContainer: IContainer) {
        mVEContainer = veContainer
    }

    override fun bindRenderContext(renderContext: IRenderContext) {
        mRenderContext = renderContext

        var renderLayerFactory = mVEContainer.getConfig().customRenderLayerFactory
        if (renderLayerFactory == null) {
            renderLayerFactory = VideoRenderContainerFactory()
        }
        val videoRenderLayer = renderLayerFactory.create(mVEContainer.getContext())
        renderContext.setDisplayView(videoRenderLayer.view())
        mChildrenLayers.forEach {
            videoRenderLayer.addAlignLayer(it)
        }
        mCustomLayers.forEach {
            videoRenderLayer.addAlignLayer(it)
        }
        mVideoRenderLayer = videoRenderLayer
        attachPreviewLayer(videoRenderLayer)
    }

    private fun attachPreviewLayer(layer: IVideoRenderLayer) {
        mRenderContainer?.bindVideoRenderLayer(layer)
        mRenderContainer?.viewTreeObserver?.addOnGlobalLayoutListener(mContainerLayoutObserver)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onLifecycleResume() {
        mVideoRenderLayer?.view()?.visibility = View.VISIBLE
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onLifecyclePause() {
        mVideoRenderLayer?.view()?.visibility = View.GONE
    }

    override fun addRenderLayer(layer: IRenderLayer, attachRenderContainer: Boolean) {
        if (attachRenderContainer) {
            val level = layer.level()
            var insertIndex = -1
            for (index in 0 until mChildrenLayers.size) {
                if (mChildrenLayers[index].level() > level) {
                    insertIndex = index
                }
            }
            if (insertIndex < 0) {
                mChildrenLayers.add(layer)
            } else {
                mChildrenLayers.add(insertIndex, layer)
            }
            mRenderContainer?.addView(layer.view(), insertIndex)
        } else {
            mCustomLayers.add(layer)
        }
        mVideoRenderLayer?.addAlignLayer(layer)
    }

    override fun removeRenderLayer(layer: IRenderLayer) {
        if (mChildrenLayers.contains(layer)) {
            mChildrenLayers.remove(layer)
            mRenderContainer?.removeView(layer.view())
        } else if (mCustomLayers.contains(layer)) {
            mCustomLayers.remove(layer)
        }
        mVideoRenderLayer?.removeAlignLayer(layer)
    }

    override fun updateViewPort(offset: Int) {
        mRenderContainer?.let {
            if (offset < 0 || offset > it.height) return
            val scale = (it.height - offset) / it.height.toFloat()
            it.pivotX = it.width / 2f
            it.pivotY = 0f
            it.scaleX = scale
            it.scaleY = scale
        }
    }

    override fun setVideoRatio(ratio: AspectRatio, marginTop: Int) {
        mVideoRatio = ratio
        mRenderMarginTop = if (mVideoRatio != AspectRatio.RATIO_1_1_INSIDE) 0 else middleLineHeight - mWindowDisplayWidth / 2
        layoutChangeAnimator()
    }

    override fun getVideoRatio(): AspectRatio {
        return mVideoRatio
    }

    override fun setCustomVideoRatio(ratio: Float, marginTop: Int) {
        mCustomVideoRatio = ratio
        mRenderMarginTop = marginTop
        mVideoRenderLayer?.view()?.layoutParams = generateLayoutParamsByRatio()
    }

    private fun layoutChangeAnimator() {
        mVideoRenderLayer?.view()?.let {
            layoutAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = 200
            }
            val animator = layoutAnimator ?: return
            val originHeight = it.layoutParams?.height ?: return
            val targetHeight = generateLayoutParamsByRatio().height
            val targetTopMargin = generateLayoutParamsByRatio().topMargin
            val targetBottomMargin = generateLayoutParamsByRatio().bottomMargin
            val originTopMargin = (it.layoutParams as? FrameLayout.LayoutParams)?.topMargin ?: return
            val originBottomMargin = (it.layoutParams as? FrameLayout.LayoutParams)?.bottomMargin ?: return
            animator.addUpdateListener { a1 ->
                val value = a1.animatedValue as? Float ?: return@addUpdateListener
                it.layoutParams?.height = (originHeight + (targetHeight - originHeight) * value).toInt()
                (it.layoutParams as? FrameLayout.LayoutParams)?.topMargin = (originTopMargin + (targetTopMargin - originTopMargin) * value).toInt()
                (it.layoutParams as? FrameLayout.LayoutParams)?.bottomMargin = (originBottomMargin + (targetBottomMargin - originBottomMargin) * value).toInt()
                it.requestLayout()
            }
            animator.start()
        }

    }

    private fun generateLayoutParamsByRatio(): FrameLayout.LayoutParams {
        val windowWidth = mRenderContainer?.measuredWidth ?: 0
        val windowHeight = mRenderContainer?.measuredHeight ?: 0
        val topPadding = mRenderContainer?.paddingTop ?: 0

        if (windowWidth == 0 || windowHeight == 0) {
            return FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        }
        mWindowDisplayWidth = windowWidth
        mWindowDisplayHeight = windowHeight
        val windowDisplayRatio = mWindowDisplayWidth / mWindowDisplayHeight.toFloat()
        val videoDisplayRatio = when (mCustomVideoRatio) {
            -1f -> {
                windowDisplayRatio
            }
            0f -> {
                getVideoRatioFloatValue()
            }
            else -> {
                mCustomVideoRatio
            }
        }
        var height = -1
        var width = -1
        if (windowDisplayRatio > videoDisplayRatio) {
            height = mWindowDisplayHeight
            width = (height * videoDisplayRatio).toInt()
        } else if (windowDisplayRatio < videoDisplayRatio) {
            width = mWindowDisplayWidth
            height = (width / videoDisplayRatio).toInt()
        }
        if (mVideoRatio == AspectRatio.RATIO_3_4_INSIDE) {
            middleLineHeight = height / 2
        }
        return if (height < 0 || width < 0) {
            FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
        } else {
            val lp = FrameLayout.LayoutParams(width, height)
            lp.marginStart = ((windowWidth - width) / 2f).toInt()
            lp.topMargin = if (mRenderMarginTop < 0) ((windowHeight - height - topPadding) / 2f).toInt() else mRenderMarginTop
            lp
        }
    }

    private fun getVideoRatioFloatValue(): Float {
        return when (mVideoRatio) {
            AspectRatio.RATIO_9_16_INSIDE -> 9 / 16f
            AspectRatio.RATIO_3_4_INSIDE -> 3 / 4f
            AspectRatio.RATIO_1_1_INSIDE -> 1f
        }
    }

    override fun getRenderContainer(): RenderContainer? {
        return mRenderContainer
    }

    override fun getVideoHeight(): Int {
        return mVideoRenderLayer?.view()?.height ?: 0
    }

    override fun getVideoWidth(): Int {
        return mVideoRenderLayer?.view()?.width ?: 0
    }

    override fun onStop() {
        mVEContainer.getLifeCycleService()?.removeObserver(this)
        mRenderContainer?.viewTreeObserver?.removeOnGlobalLayoutListener(mContainerLayoutObserver)
        layoutAnimator?.let { animator ->
            animator.cancel()
            animator.removeAllUpdateListeners()
            animator.removeAllListeners()
        }
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
     * 在视频画面上添加一个可以跟随视频画面缩放位移的子视图
     * @param attachRenderContainer 是否要添加到渲染容器里
     */
    fun addRenderLayer(layer: IRenderLayer, attachRenderContainer: Boolean = true)

    /**
     * 移除一个渲染视图
     */
    fun removeRenderLayer(layer: IRenderLayer)

    /**
     * 更新画面显示区域
     * @param offset 底部向上偏移量
     */
    fun updateViewPort(offset: Int)

    /**
     * 改变视图的长宽比
     * @param ratio 视图长宽比
     * @param marginTop 渲染 layer 上边距，-1 居中显示
     */
    fun setVideoRatio(ratio: AspectRatio, marginTop: Int = -1)

    /**
     * 获取设置的视图长宽比
     */
    fun getVideoRatio(): AspectRatio

    /**
     * 自定义视图长宽比，当 setVideoRatio(AspectRatio.RATIO_CUSTOM)
     * 可调用该方法设置自定义当长宽比
     * @param ratio 自定义长宽比 (-1： 则默认宽高容器撑满， 0： 则取默认视频比例 videoRatio)
     * @param marginTop 渲染 layer 上边距，-1 居中显示
     */
    fun setCustomVideoRatio(ratio: Float, marginTop: Int = -1)

    /**
     * 返回视频渲染层宽度
     */
    fun getVideoWidth(): Int

    /**
     * 返回视频渲染层高度
     */
    fun getVideoHeight(): Int

    /**
     * 返回渲染层容器
     */
    fun getRenderContainer(): RenderContainer?
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
    fun addAlignLayer(layer: IRenderLayer)
    fun removeAlignLayer(layer: IRenderLayer)
}
