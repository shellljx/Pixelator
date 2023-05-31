package com.gmail.shellljx.wrapper.service.panel

import android.content.Context
import android.view.MotionEvent
import android.view.View
import androidx.annotation.AnimRes
import androidx.lifecycle.*
import com.gmail.shellljx.wrapper.*
import com.gmail.shellljx.wrapper.service.gesture.*
import java.lang.Exception
import java.lang.IllegalStateException
import java.util.*

class PanelService : IPanelService, LifecycleObserver, OnSingleTapObserver {
    companion object {
        private const val TAG = "PanelService"
    }

    private lateinit var mContainer: IContainer
    private var mPanelContainer: PanelContainer? = null
    private val mPanelStackChangedObservers = arrayListOf<PanelStackChangedObserver>()
    private val mPanelTokenMap = hashMapOf<PanelToken, PanelRecord>()
    private val mPanelStack = Stack<PanelRecord>()
    override fun onStart() {
        mContainer.getLifeCycleService()?.addObserver(this)
        mContainer.getGestureService()?.addSingleTapObserver(this, GesturePriorityProcessor.GESTURE_PRIORITY_HIGHT)
    }

    override fun bindVEContainer(veContainer: IContainer) {
        mContainer = veContainer
    }

    override fun createView(context: Context): View {
        val panelContainer = PanelContainer(context)
        panelContainer.id = R.id.panel_container
        panelContainer.bindVEContainer(mContainer)
        mPanelContainer = panelContainer
        return panelContainer
    }

    override fun onBackPressed(): Boolean {
        if (mPanelStack.isNotEmpty()) {
            val topPanel = mPanelStack.peek()
            hidePanelInternal(topPanel, false)
            return true
        }
        return false
    }

    override fun showPanel(panelClazz: Class<out AbsPanel>, payload: Any?): PanelToken? {
        mPanelContainer ?: return null
        var record = findPanelRecord(panelClazz)
        if (record != null && !record.config.forceNewInstance) {
            //如果record已经处于打开的状态并且同一个panel不会有多个实例，则把该panel上面的所有都关闭
            if (mPanelStack.contains(record)) {
                popToTargetPanel(record)
                record.panel.resume()
                payload?.let {
                    getStackTopPanel()?.panel?.updatePayload(payload)
                }
            } else {
                showPanelInternal(record, payload)
            }
            return record.token
        }
        //新创建token 和 panel 实例
        val panel = createPanel(panelClazz) ?: return null
        val token = PanelToken(panelClazz)
        panel.mToken = token
        record = PanelRecord(panel, token, panel.panelConfig)
        panel.bindVEContainer(mContainer)
        mPanelTokenMap[token] = record
        showPanelInternal(record, payload)
        return token
    }

    override fun showPanel(token: PanelToken, payload: Any?) {
        showPanel(token.clazz, payload)
    }

    override fun getPanelStackSize(): Int {
        return mPanelStack.size
    }

    override fun updatePayload(token: PanelToken, payload: Any?) {
        payload?.let {
            val record = mPanelTokenMap[token] ?: return
            record.panel.updatePayload(payload)
        }
    }

    override fun hidePanel(token: PanelToken, immediately: Boolean) {
        val record = mPanelTokenMap[token] ?: return
        hidePanelInternal(record, immediately)
    }

    override fun addPanelStackChangedObserver(observer: PanelStackChangedObserver) {
        if (!mPanelStackChangedObservers.contains(observer)) {
            mPanelStackChangedObservers.add(observer)
        }
    }

    override fun removePanelStackChangedObserver(observer: PanelStackChangedObserver) {
        mPanelStackChangedObservers.remove(observer)
    }

    private fun showPanelInternal(record: PanelRecord, payload: Any?) {
        payload?.let {
            record.panel.updatePayload(it)
        }
        mPanelContainer?.let { it ->
            getStackTopPanel()?.panel?.onPause()
            mPanelStack.push(record)
            it.showPanel(record.panel)
            mPanelStackChangedObservers.forEach { observer ->
                observer.onTopPanelChanged(getStackTopPanel()?.token)
            }
        }
    }

    private fun hidePanelInternal(record: PanelRecord, immediately: Boolean) {
        if (!mPanelStack.contains(record)) {
            return
        }
        mPanelContainer?.let {
            mPanelStack.remove(record)
            it.hidePanel(record.panel, immediately)
            mPanelStackChangedObservers.forEach { observer ->
                observer.onTopPanelChanged(getStackTopPanel()?.token)
            }
        }
    }

    private fun findPanelRecord(clazz: Class<out AbsPanel>): PanelRecord? {
        val iterator = mPanelTokenMap.values.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next.panel::class.java == clazz) {
                return next
            }
        }
        return null
    }

    private fun popToTargetPanel(record: PanelRecord) {
        val topPanelRecord = getStackTopPanel() ?: return
        if (topPanelRecord != record) {
            hidePanelInternal(topPanelRecord, true)
            popToTargetPanel(record)
        }
    }

    private fun getStackTopPanel(): PanelRecord? {
        return if (mPanelStack.empty()) {
            null
        } else {
            mPanelStack.peek()
        }
    }

    private fun createPanel(clazz: Class<out AbsPanel>): AbsPanel? {
        try {
            val constructor = clazz.getConstructor(Context::class.java)
            return constructor.newInstance(mContainer.getContext())
        } catch (e: Exception) {
            throw IllegalStateException("create panel failed: $e")
        }
    }

    override fun onSingleTap(): Boolean {
        val topPanel = getStackTopPanel() ?: return false
        val config = mPanelTokenMap[topPanel.token]?.config ?: return false
        if (config.clickOutsideDismiss) {
            hidePanel(topPanel.token, false)
            return true
        }
        return false
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onLifecycleResume() {
        getStackTopPanel()?.panel?.resume()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onLifecyclePause() {
        getStackTopPanel()?.panel?.pause()
    }

    override fun onStop() {
        mPanelContainer?.release()
        mContainer.getLifeCycleService()?.addObserver(this)
    }
}

private class PanelRecord(
    val panel: AbsPanel,
    val token: PanelToken,
    val config: PanelConfig
)

class PanelToken(internal val clazz: Class<out AbsPanel>) {
    var isTopStack: Boolean = false
        internal set
    var isAttach: Boolean? = false
        internal set
    val className: String
        get() = clazz.simpleName
}

class PanelConfig {
    /**
     * 代表该类型的面板是否强制每次打开都是一个新的Panel实例
     * true 代表每次打开都会重新初始化一个panel
     * false 代表会服用之前的panel实例，如果该实例已经在栈内，则把顶层的所有panel都pop
     */
    var forceNewInstance = false

    /**
     * 是否点击空白区域关闭panel
     */
    var clickOutsideDismiss = true

    /**
     * 打开动画
     */
    @AnimRes
    var enterAnim = 0

    /**
     * 关闭动画
     */
    @AnimRes
    var exitAnim = 0
}

interface IPanelService : IService {
    /**
     * 打开一个面板
     * @param panelClazz 面板的类
     * @param payload 可选的数据
     * @return 面板的token 后续关闭面板或者更新面板数据都可以使用token
     */
    fun showPanel(panelClazz: Class<out AbsPanel>, payload: Any? = null): PanelToken?

    /**
     * 使用token打开面板
     */
    fun showPanel(token: PanelToken, payload: Any? = null)

    /**
     * 使用token关闭面板
     * @param immediately 如果配置了 exitAnim，是否无视关闭动画马上消失
     */
    fun hidePanel(token: PanelToken, immediately: Boolean = false)

    /**
     * 更新面板里的数据
     */
    fun updatePayload(token: PanelToken, payload: Any?)

    /**
     * 获取现在已经打开里多少面板
     * 面板栈的大小
     */
    fun getPanelStackSize(): Int
    fun createView(context: Context): View
    fun onBackPressed(): Boolean

    /**
     * 添加面板栈变化的监听
     */
    fun addPanelStackChangedObserver(observer: PanelStackChangedObserver)
    fun removePanelStackChangedObserver(observer: PanelStackChangedObserver)
}

interface PanelStackChangedObserver {
    /**
     * 最顶层面板发生改变
     * @param token 顶层面板的token
     */
    fun onTopPanelChanged(token: PanelToken?)
}
