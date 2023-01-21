package com.gmail.shellljx.pixelator

class Pixelator {

    private var mId = 0L

    init {
       mId = create()
    }
    private external fun create(): Long

    companion object {
        // Used to load the 'pixelator' library on application startup.
        init {
            System.loadLibrary("pixelator")
        }
    }
}
