package com.example.snakegameusinggestures

import android.graphics.Canvas
import android.graphics.Paint
//import com.miles.snakegameusinggestures.logerlibrary.e

/**
 * 貪吃蛇遊戲中所有物件的父類,所有的遊戲物件都將會從此類繼承
 * */
open class GameObject(var row: Int, var column: Int) {

    /**
     * 繪製遊戲物件
     * @param canvas 畫布物件
     * @param paint 畫筆物件
     * */
    open fun draw(canvas: Canvas, x: Float, y: Float, paint: Paint) {
        canvas.drawRect(x, y, x + SnakeGameConfiguration.GRID_WIDTH, y + SnakeGameConfiguration.GRID_HEIGHT, paint)
    }
}