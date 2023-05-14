package com.example.ar

import android.content.Context
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.core.graphics.toRectF
import kotlin.math.*

class TestViewGroup : FrameLayout {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, def: Int) : super(context, attrs, def)

    private val INVALID_POINTER_ID = -1
    private var mActivePointerId1 = INVALID_POINTER_ID
    private var mActivePointerId2 = INVALID_POINTER_ID

    // 记录缩放和旋转的起始值
    private var mLastTouchX1 = 0f  // 记录缩放和旋转的起始值
    private var mLastTouchY1 = 0f  // 记录缩放和旋转的起始值
    private var mLastTouchX2 = 0f  // 记录缩放和旋转的起始值
    private var mLastTouchY2 = 0f
    private var mLastAngle = 0.0
    private var mLastScale = 1f

    var centerAdsorbOffset = 11
    // 垂直中心吸附矩形
    var verticalCenterAdsorbRectF = RectF()

    // 水平中心吸附矩形
    var horizontalCenterAdsorbRectF = RectF()
    var isVerticalCenterAdsorbing = false
    var isHorizontalCenterAdsorbing = false

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                mActivePointerId1 = event.getPointerId(0)
                mLastTouchX1 = event.x
                mLastTouchY1 = event.y
                return true
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                mActivePointerId2 = event.getPointerId(event.actionIndex)
                mLastTouchX2 = event.getX(event.findPointerIndex(mActivePointerId2))
                mLastTouchY2 = event.getY(event.findPointerIndex(mActivePointerId2))
                mLastAngle = atan2(
                    (mLastTouchY2 - mLastTouchY1).toDouble(),
                    (mLastTouchX2 - mLastTouchX1).toDouble()
                )
            }
            MotionEvent.ACTION_MOVE -> {
                if (mActivePointerId1 != INVALID_POINTER_ID && mActivePointerId2 != INVALID_POINTER_ID) {
                    val x1 = event.getX(event.findPointerIndex(mActivePointerId1))
                    val y1 = event.getY(event.findPointerIndex(mActivePointerId1))
                    val x2 = event.getX(event.findPointerIndex(mActivePointerId2))
                    val y2 = event.getY(event.findPointerIndex(mActivePointerId2))
                    val angle = atan2((y2 - y1).toDouble(), (x2 - x1).toDouble())
                    val scale = sqrt(((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)).toDouble())
                        .toFloat() / sqrt(
                        ((mLastTouchX2 - mLastTouchX1) * (mLastTouchX2 - mLastTouchX1) +
                                (mLastTouchY2 - mLastTouchY1) * (mLastTouchY2 - mLastTouchY1)).toDouble()
                    ).toFloat()

                    val child = getChildAt(0)
                    val centerX = child.width / 2f
                    val centerY = child.height / 2f
                    child.pivotX = centerX
                    child.pivotY = centerY

                    child.scaleX = mLastScale * scale
                    child.scaleY = mLastScale * scale
                    var rotation = child.rotation + Math.toDegrees(angle - mLastAngle).toFloat()

                    if (rotation > 360) {
                        rotation -= 360
                    }
                    if (rotation < -360) {
                        rotation += 360
                    }

                    val offset = rotation % 90
                    System.out.println("lijinxiang rotation $rotation $offset")

                    if (offset < 10 && offset > -10) {
                        rotation -= offset
                    } else if (offset > 80) {
                        rotation += (90 - offset)
                    } else if (offset < -80) {
                        rotation -= (90 + offset)
                    } else {
                        mLastAngle = angle
                    }


                    child.rotation = rotation
                    // 更新上一次缩放和旋转的值
                    mLastTouchX1 = x1
                    mLastTouchY1 = y1
                    mLastTouchX2 = x2
                    mLastTouchY2 = y2
                    mLastScale *= scale
                } else if (mActivePointerId1 != INVALID_POINTER_ID) {
                    val child = getChildAt(0)
                    val x = event.getX(event.findPointerIndex(mActivePointerId1))
                    val y = event.getY(event.findPointerIndex(mActivePointerId1))

                    var dx = x - mLastTouchX1
                    var dy = y - mLastTouchY1
                    val centerX = child.width / 2f + child.x
                    val centerY = child.height / 2f + child.y

                    val viewRectF = Rect(child.left, child.top, child.right, child.bottom).toRectF().apply {
                        child.matrix.mapRect(this)
                    }
                    val space = if (viewRectF.width() < 50) viewRectF.width() else 50f
                    val maxTransX = (viewRectF.width() - child.width) / 2f - space + width
                    val minTransX = -((viewRectF.width() - child.width) / 2f - space + child.width)
                    val minTransY = -((viewRectF.height() - child.height) / 2f - space + child.height)
                    val maxTransY = (viewRectF.height() - child.height) / 2f - space + height

                    child.x += dx
                    child.y += dy

                    if (child.x > maxTransX) {
                        child.x = maxTransX
                    } else if (child.x < minTransX) {
                        child.x = minTransX
                    }

                    if (child.y < minTransY) {
                        child.y = minTransY
                    } else if (child.y > maxTransY) {
                        child.y = maxTransY
                    }

                    if (verticalCenterAdsorbRectF.isEmpty || horizontalCenterAdsorbRectF.isEmpty) {
                        val percentX = width / 2f
                        val percentY = height / 2f
                        verticalCenterAdsorbRectF.set(percentX - centerAdsorbOffset, 0f, percentX + centerAdsorbOffset, height.toFloat())
                        horizontalCenterAdsorbRectF.set(0f, percentY - centerAdsorbOffset, width.toFloat(), percentY + centerAdsorbOffset)
                    }
                    val stickerCenterX = child.x + child.width / 2f
                    val stickerCenterY = child.y + child.height / 2f
                    isVerticalCenterAdsorbing = verticalCenterAdsorbRectF.contains(stickerCenterX, stickerCenterY)
                    if (isVerticalCenterAdsorbing) {
                        child.x = verticalCenterAdsorbRectF.centerX() - child.width / 2f
                    }
                    isHorizontalCenterAdsorbing = horizontalCenterAdsorbRectF.contains(stickerCenterX, stickerCenterY)
                    if (isHorizontalCenterAdsorbing) {
                        child.y = horizontalCenterAdsorbRectF.centerY() - child.height / 2f
                    }

                    System.out.println("lijinxiang $right ${child.x}")
                    mLastTouchX1 = x
                    mLastTouchY1 = y
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val pointerIndex = event.actionIndex
                val pointId = event.getPointerId(pointerIndex)
                if (pointId == mActivePointerId1) {
                    mActivePointerId1 = mActivePointerId2;
                    mLastTouchX1 = mLastTouchX2
                    mLastTouchY1 = mLastTouchY2
                }
                mLastScale = getChildAt(0).scaleX
                mActivePointerId2 = INVALID_POINTER_ID
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mActivePointerId1 = INVALID_POINTER_ID
                mActivePointerId2 = INVALID_POINTER_ID
            }
        }
        return true
    }

}