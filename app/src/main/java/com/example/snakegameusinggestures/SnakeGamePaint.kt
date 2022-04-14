package com.example.snakegameusinggestures

import android.graphics.Color
import android.graphics.Paint

object SnakeGamePaint {
    /**
     * 畫蛇身體的畫筆
     * */
    val snakeBodyPaint = Paint().apply {
        isDither = true
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.GREEN
    }

    /**
     * 畫蛇頭部的畫筆
     * */
    val snakeHeaderPaint = Paint().apply {
        isDither = true
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.BLUE
    }
    /**
     * 畫食物的畫筆
     * */
    val foodPaint = Paint().apply {
        isDither = true
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.RED
    }

    /**
     * 畫牆壁的畫筆
     * */
    val wallPaint = Paint().apply {
        isDither = true
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.BLACK
    }
}