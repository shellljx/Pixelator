package com.gmail.shellljx.wrapper.service.panel

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import com.gmail.shellljx.wrapper.IContainer
import java.lang.Exception

class PanelContainer : FrameLayout {
    private lateinit var mVEContianer: IContainer
    private val mPanelElementMap = hashMapOf<AbsPanel, PanelElement>()

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)
    constructor(context: Context, attributeSet: AttributeSet?, def: Int) : super(context, attributeSet, def)

    fun bindVEContainer(veContainer: IContainer) {
        mVEContianer = veContainer
    }

    fun showPanel(panel: AbsPanel) {
        var element = mPanelElementMap[panel]
        if (element != null) {
            if (!element.isAttached(this)) {
                element.attachContainer(this)
            }
        } else {
            element = PanelElement(mVEContianer, panel)
            mPanelElementMap[panel] = element
            element.attachContainer(this)
        }
    }

    fun hidePanel(panel: AbsPanel, immediately: Boolean) {
        val element = mPanelElementMap[panel] ?: return
        if (element.isAttached(this)) {
            element.detachContaienr(this, immediately)
        }
    }

    fun release() {
        val iterator = mPanelElementMap.values.iterator()
        while (iterator.hasNext()) {
            iterator.next().release()
        }
    }

    private class PanelElement(private val container: IContainer, val panel: AbsPanel) {
        private var backGround: View? = null
        private var isEnterAnimating = false
        private var isExitAnimating = false

        fun attachContainer(parent: ViewGroup) {
            if (isEnterAnimating || isExitAnimating) return
            if (isAttached(parent)) {
                parent.removeView(panel.getView())
                backGround?.let {
                    parent.removeView(backGround)
                }
            }
            val view = panel.createView(parent)
            backGround?.let {
                parent.addView(it, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
            }
            parent.addView(view)
            panel.attach()
            panel.resume()
            if (panel.panelConfig.enterAnim == 0) {
                return
            }
            panel.getView()?.viewTreeObserver?.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    panel.getView()?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                    val enterAnim = panel.panelConfig.enterAnim
                    if (enterAnim != 0) {
                        try {
                            AnimationUtils.loadAnimation(container.getContext(), enterAnim)?.let {
                                it.setAnimationListener(object : Animation.AnimationListener {
                                    override fun onAnimationStart(animation: Animation?) {
                                        isEnterAnimating = true
                                        panel.onEnterAnimationStart()
                                    }

                                    override fun onAnimationEnd(animation: Animation?) {
                                        isEnterAnimating = false
                                    }

                                    override fun onAnimationRepeat(animation: Animation?) {
                                    }
                                })
                                panel.getView()?.startAnimation(it)
                            }
                        } catch (e: Exception) {
                        }
                    }
                }
            })
        }

        fun detachContaienr(parent: ViewGroup, immediately: Boolean) {
            if (isExitAnimating) return
            panel.getView()?.animation?.cancel()
            panel.getView()?.clearAnimation()
            val animation = try {
                if (panel.panelConfig.exitAnim != 0) {
                    AnimationUtils.loadAnimation(container.getContext(), panel.panelConfig.exitAnim)
                } else null
            } catch (e: Exception) {
                null
            }
            if (animation == null || immediately) {
                panel.pause()
                panel.detach()
                parent.removeView(panel.getView())
                backGround?.let {
                    parent.removeView(it)
                }
                return
            }

            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    isExitAnimating = true
                    panel.onExitAnimationStart()
                }

                override fun onAnimationEnd(animation: Animation?) {
                    isExitAnimating = false
                    panel.pause()
                    panel.detach()
                    parent.removeView(panel.getView())
                    backGround?.let {
                        parent.removeView(it)
                    }
                }

                override fun onAnimationRepeat(animation: Animation?) {
                }
            })
            panel.getView()?.startAnimation(animation)
        }

        fun isAttached(parent: ViewGroup): Boolean {
            return parent.indexOfChild(panel.getView()) >= 0
        }

        fun release() {
            panel.getView()?.animation?.cancel()
            panel.getView()?.clearAnimation()
        }
    }
}
