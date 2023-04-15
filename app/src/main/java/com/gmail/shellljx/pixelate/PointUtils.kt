package com.gmail.shellljx.pixelate

import android.graphics.PointF
import kotlin.math.*

object PointUtils {

    fun generatePoints(start: PointF, control: PointF, end: PointF, interval: Int): List<PointF> {
        val points = arrayListOf<PointF>()
        points.add(start)
        var t = 0.0
        while (t <= 1) {
            val x = ((1 - t).pow(2.0) * start.x + 2 * t * (1 - t) * control.x + t.pow(2.0) * end.x).roundToInt()
            val y = ((1 - t).pow(2.0) * start.y + 2 * t * (1 - t) * control.y + t.pow(2.0) * end.y).roundToInt()
            points.add(PointF(x.toFloat(), y.toFloat()))
            t += interval * 1.0 / calculateBezierLength(start, control, end)
        }
        points.add(end)
        return points
    }

    private fun calculateBezierLength(start: PointF, control: PointF, end: PointF): Double {
        return (calculatePointLength(start, control) + calculatePointLength(control, end)).toDouble()
    }

    private fun calculatePointLength(from: PointF, to: PointF): Int {
        val dx = (to.x - from.x).toDouble()
        val dy = (to.y - from.y).toDouble()
        return sqrt(dx * dx + dy * dy).roundToInt()
    }

    fun distanceTo(from: PointF, to: PointF): Float {
        return sqrt((to.x - from.x).pow(2) + (to.y - from.y).pow(2))
    }

    fun pointsWith(from: PointF, to: PointF, control: PointF, size: Float): ArrayList<PointF> {
        val p0 = from
        //如果 control 是 from 和 to 的中点，则将 control 设置为 from
        val p1 = if (isCenter(control, from, to)) {
            from
        } else {
            control
        }

        val p2 = to

        val ax = p0.x - 2 * p1.x + p2.x
        val ay = p0.y - 2 * p1.y + p2.y
        val bx = 2 * p1.x - 2 * p0.x
        val by = 2 * p1.y - 2 * p0.y

        val a = 4 * (ax * ax + ay * ay)
        val b = 4 * (ax * bx + ay * by)
        val c = bx * bx + by * by

        //整条曲线的长度
        val totalLength = lengthWithT(1.0, a.toDouble(), b.toDouble(), c.toDouble())
        //生成的点数
        val count = max(1.0, ceil(totalLength / size * 5)).toInt()
        val list = arrayListOf<PointF>()
        for (i in 0 until count) {
            var t = i * 1.0 / count
            val length = t * totalLength
            t = tWithT(t, length, a.toDouble(), b.toDouble(), c.toDouble())
            val x = (1 - t) * (1 - t) * p0.x + 2 * (1 - t) * t * p1.x + t * t * p2.x
            val y = (1 - t) * (1 - t) * p0.y + 2 * (1 - t) * t * p1.y + t * t * p2.y
            list.add(PointF(x.toFloat(), y.toFloat()))
        }
        return list
    }

    private fun isCenter(control: PointF, from: PointF, to: PointF): Boolean {
        val isCenterX = abs((from.x + to.x) / 2f - control.x) < 0.0001
        val isCenterY = abs((from.y - to.y) / 2f - control.y) < 0.0001
        return isCenterX && isCenterY
    }

    private fun lengthWithT(t: Double, A: Double, B: Double, C: Double): Double {
        if (A < 0.00001f) {
            return 0.0
        }
        val temp1 = sqrt(C + t * (B + A * t))
        val temp2 = (2 * A * t * temp1 + B * (temp1 - sqrt(C)));
        val temp3 = ln(abs(B + 2 * sqrt(A) * sqrt(C) + 0.0001))
        val temp4 = ln(abs(B + 2 * A * t + 2 * sqrt(A) * temp1) + 0.0001)
        val temp5 = 2 * sqrt(A) * temp2;
        val temp6 = (B * B - 4 * A * C) * (temp3 - temp4);

        return (temp5 + temp6) / (8 * A.pow(1.5))
    }

    /**
    长度函数反函数，根据 length，求出对应的 t，使用牛顿切线法求解

    @param t 给出的近似的 t，比如求长度占弧长 0.3 的 t，t 应该是接近 0.3，则传入近似值 0.3
    @param length 目标弧长，实际长度，非占比
    @param A 见【注意】
    @param B 见【注意】
    @param C 见【注意】
    @return 结果 t 值
     */
    private fun tWithT(t: Double, length: Double, A: Double, B: Double, C: Double): Double {
        var t1 = t
        var t2: Double
        var lastDouble = 0.0
        while (true) {
            val speed = speedWithT(t, A, B, C)
            if (speed < 0.0001) {
                t2 = t1
                break
            }
            t2 = t1 - (lengthWithT(t1, A, B, C) - length) / speed
            if (abs(t1 - t2) < 0.0001) {
                break
            }
            //02-04 23:21:59.644 20094-20094/? E/11111: t1 0.501487692151914 t2 0.4985078813922082

            //Log.e("11111", "t1 $t1 t2 $t2")
            if (lastDouble == t1) {
                break
            }

            lastDouble = t2
            t1 = t2

        }
        return t2
    }

    /**
    速度函数 s(t) = sqrt(A * t^2 + B * t + C)
    @param t t 值
    @param A 见【注意】
    @param B 见【注意】
    @param C 见【注意】
    @return 贝塞尔曲线某一点的速度
     */
    private fun speedWithT(t: Double, A: Double, B: Double, C: Double): Double {
        return sqrt((A * t.pow(2.0) + B * t + C).coerceAtLeast(0.0))
    }
}