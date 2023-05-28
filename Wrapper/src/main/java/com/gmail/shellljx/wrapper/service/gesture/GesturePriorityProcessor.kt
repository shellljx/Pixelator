package com.gmail.shellljx.wrapper.service.gesture

class GesturePriorityProcessor<T> {
    companion object {
        const val GESTURE_PRIORITY_LOW = 0
        const val GESTURE_PRIORITY_NORMAL = 1
        const val GESTURE_PRIORITY_HIGHT = 2
    }

    private val mProcessorMap = hashMapOf<Int, ArrayList<T>>()
    fun add(processor: T, priority: Int) {
        if (!isAvaliablePriority(priority)) return
        var listeners = mProcessorMap[priority]
        if (listeners == null) {
            listeners = arrayListOf()
            mProcessorMap[priority] = listeners
        }
        if (!listeners.contains(processor)) {
            listeners.add(processor)
        }
    }

    fun remove(processor: T) {
        mProcessorMap.values.forEach {
            if (it.contains(processor)) {
                it.remove(processor)
                return@forEach
            }
        }
    }

    fun process(callback: (processor: T) -> Boolean): Boolean {
        mProcessorMap[GESTURE_PRIORITY_HIGHT]?.let { list ->
            list.forEach {
                if (callback.invoke(it)) {
                    return true
                }
            }
        }

        mProcessorMap[GESTURE_PRIORITY_NORMAL]?.let { list ->
            list.forEach {
                if (callback.invoke(it)) {
                    return true
                }
            }
        }

        mProcessorMap[GESTURE_PRIORITY_LOW]?.let { list ->
            list.forEach {
                if (callback.invoke(it)) {
                    return true
                }
            }
        }
        return false
    }

    private fun isAvaliablePriority(priority: Int): Boolean {
        return priority == GESTURE_PRIORITY_LOW || priority == GESTURE_PRIORITY_NORMAL || priority == GESTURE_PRIORITY_HIGHT
    }
}
