package com.example.snakegameusinggestures.callbacks

open class OnEatenFoodListener {
    interface OnEatenFoodListener {
        fun onEaten()
    }

    open fun onEaten() {}
}