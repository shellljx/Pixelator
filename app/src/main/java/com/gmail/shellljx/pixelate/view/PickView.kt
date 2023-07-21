package com.gmail.shellljx.pixelate.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.isVisible
import com.gmail.shellljx.pixelate.R
import com.gmail.shellljx.pixelate.extension.dp
import kotlin.math.abs
import kotlin.math.floor

/**
 * @Author: shell
 * @Email: shellljx@gmail.com
 * @Date: 2023/6/22
 * @Description:
 */
class PickView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private lateinit var overlayContent: ViewGroup
    private lateinit var overlayIconView: ImageView
    private lateinit var overlayTextView: TextView
    private lateinit var contentView: ViewGroup
    private lateinit var optionGroup: LinearLayout
    private val pickItems = arrayListOf<PickItem>()

    private var lastX = 0f
    private var lastY = 0f
    private var moved = false
    private val tochSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val moveAnimator = ValueAnimator.ofFloat(0f, 1f)
    private var optionPickListener: OptionPickListener? = null
    private var animatorstartY = 0f
    private var animatorEndY = 0f
    private var lastAnimPercent = 0f
    private var selectedPosition = 0

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_wheel_pick, this)
        moveAnimator.duration = 200
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        overlayContent = findViewById(R.id.lv_selected_option)
        overlayIconView = findViewById(R.id.selected_icon)
        overlayTextView = findViewById(R.id.selected_text)
        contentView = findViewById(R.id.option_content)
        optionGroup = findViewById(R.id.ll_option_group)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        moveAnimator.cancel()
        moveAnimator.removeAllListeners()
        moveAnimator.removeAllUpdateListeners()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return !moveAnimator.isRunning
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = x
                lastY = y
                moved = false
            }

            MotionEvent.ACTION_MOVE -> {
                val dy = y - lastY
                if (abs(dy) < tochSlop && !moved) {
                    return true
                }
                moved = true
                selectedPosition = moveOptions(dy)
                lastY = y
            }

            MotionEvent.ACTION_UP -> {
                if (!moved) {
                    val position = getClickedOptionPosition(x, y)
                    if (position >= 0) {
                        selectPosition(position, true, byUser = true)
                    } else {
                        optionPickListener?.onCancelPick()
                    }
                } else {
                    optionPickListener?.onPickOption(selectedPosition, true)
                }
            }
        }
        return true
    }

    fun setItems(items: List<PickItem>, selectedPosition: Int) {
        pickItems.clear()
        optionGroup.removeAllViews()
        pickItems.addAll(items)
        items.forEach {
            optionGroup.addView(createPickView(it))
        }
        val height = ((pickItems.size - 1) * 2 + 1) * 60.dp()

        val lp = layoutParams
        lp.height = height
        layoutParams = lp
        addOnLayoutChangeListener(object : OnLayoutChangeListener {
            override fun onLayoutChange(v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                removeOnLayoutChangeListener(this)
                selectPosition(selectedPosition, animator = false, byUser = false)
            }
        })
    }

    fun selectPosition(position: Int, animator: Boolean, byUser: Boolean) {
        val endY = height / 2 - 25.dp()
        val startY = contentView.y + position * 50.dp()
        selectedPosition = position
        if (animator) {
            animatorstartY = startY
            animatorEndY = endY.toFloat()
            moveAnimator.start()
            moveAnimator.removeAllListeners()
            moveAnimator.removeAllUpdateListeners()
            moveAnimator.addUpdateListener {
                val percent = it.animatedValue as Float
                val translate = (percent - lastAnimPercent) * (animatorEndY - animatorstartY)
                moveOptions(translate)
                lastAnimPercent = percent
            }

            moveAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    lastAnimPercent = 0f
                    selectedPosition = position
                    optionPickListener?.onPickOption(position, byUser)
                }
            })
        } else {
            moveOptions(endY - startY)
            optionPickListener?.onPickOption(position, byUser)
        }
    }

    fun setOptionPickListener(listener: OptionPickListener) {
        this.optionPickListener = listener
    }

    private fun createPickView(item: PickItem): View {
        val view = LayoutInflater.from(context).inflate(R.layout.layout_wheel_item, optionGroup, false)
        val textView = view.findViewById<TextView>(R.id.tv_text)
        val iconView = view.findViewById<ImageView>(R.id.icon)
        iconView.isVisible = item.icon > 0
        if (item.icon > 0) {
            iconView.setImageResource(item.icon)
        }
        textView.text = item.text
        return view
    }

    private fun moveOptions(translate: Float): Int {
        var dy = translate
        if (overlayContent.y < dy) {
            dy = overlayContent.y
        } else if (overlayContent.y + overlayContent.height - dy > contentView.height) {
            dy = overlayContent.y + overlayContent.height - contentView.height
        }
        contentView.y += dy
        overlayContent.y -= dy

        val position = floor((overlayContent.y + overlayContent.height / 2 - optionGroup.y) / 50.dp()).toInt()
        if (position in pickItems.indices) {
            val pickItem = pickItems[position]
            overlayTextView.text = pickItem.text
            if (pickItem.icon > 0) {
                overlayIconView.isVisible = true
                overlayIconView.setImageResource(pickItem.icon)
            } else {
                overlayIconView.isVisible = false
            }
        }
        return position
    }

    private fun getClickedOptionPosition(x: Float, y: Float): Int {
        optionGroup.children.forEachIndexed { index, view ->
            val localX = x - contentView.x
            val localY = y - contentView.y
            if (localY > view.top && localY < view.bottom && localX > view.left && localX < view.right) {
                return index
            }
        }
        return -1
    }
}

class PickItem(val icon: Int, val text: String)

interface OptionPickListener {
    fun onPickOption(position: Int, byUser: Boolean)
    fun onCancelPick()
}