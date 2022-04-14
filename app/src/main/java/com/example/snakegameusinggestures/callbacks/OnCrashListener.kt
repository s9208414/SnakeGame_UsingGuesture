package com.example.snakegameusinggestures.callbacks

open class OnCrashListener {
    interface OnCrashListener {
        fun onCrash()
    }

    open fun onCrash() {}
}