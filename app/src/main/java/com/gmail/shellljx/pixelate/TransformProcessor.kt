package com.gmail.shellljx.pixelate

import android.graphics.Matrix
import android.view.MotionEvent

class TransformProcessor {
    // initial coordinates from finger down event for each finger in following order [x1,y1, x2,y2, ...]
    private val source: FloatArray = FloatArray(4)
    // new coordinates from finger move event for each finger in following order [x1,y1, x2,y2, ...]
    private val distance: FloatArray = FloatArray(4)
    private val tempMatrix: Matrix = Matrix()
    val matrix = Matrix()
    private var count: Int = 0
    private var pointerIndex = 0

    fun onTouchEvent(event: MotionEvent) {
        if (event.pointerCount > 2) {
            return
        }
        val action = event.actionMasked
        val index = event.actionIndex
        when (action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                // get the coordinates for the particular finger
                val idx = index * 2
                source[idx] = event.getX(index)
                source[idx + 1] = event.getY(index)

                count++
                pointerIndex = 0
            }

            MotionEvent.ACTION_MOVE -> {
                var i = 0
                while (i < count) {
                    val idx = pointerIndex + i * 2
                    distance[idx] = event.getX(i)
                    distance[idx + 1] = event.getY(i)
                    i++
                }

                // use poly to poly to detect transformations
                tempMatrix.setPolyToPoly(source, pointerIndex, distance, pointerIndex, count)
                matrix.postConcat(tempMatrix)
                System.arraycopy(distance, 0, source, 0, distance.size)

            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                if (event.getPointerId(index) == 0) pointerIndex = 2
                count--
            }
        }
    }
}