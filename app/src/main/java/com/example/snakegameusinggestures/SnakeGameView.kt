package com.example.snakegameusinggestures

//import com.miles.snakegameusinggestures.logerlibrary.e
import android.content.Context
import android.graphics.Canvas
import android.graphics.PointF
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import com.example.snakegameusinggestures.callbacks.OnCrashListener
import com.example.snakegameusinggestures.callbacks.OnEatenFoodListener
import java.util.*
import kotlin.concurrent.thread

class SnakeGameView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    /**
     * 組成貪食蛇方塊的列表
     * */
    private val snake = mutableListOf<SnakeBlock>()

    /**
     * 貪食蛇的食物
     * */
    private lateinit var food: Food

    /**
     * 單元格集合
     * */
    private lateinit var gridList: MutableList<MutableList<PointF>>

    /**
     * 當撞到牆壁的監聽器
     * */
    var crashListener: OnCrashListener? = null

    /**
     * 當吃到食物的監聽器
     * */
    var eatenListener: OnEatenFoodListener? = null

    /**
     *
    獲取或設置貪吃蛇的移動方向
     * */
    var direction = DIRECTION.DIRECTION_RIGHT
        set(value) {  // 重寫屬性的Set方法，來避免錯誤的移動
            when (value) {
                DIRECTION.DIRECTION_UP -> {
                    if (field != DIRECTION.DIRECTION_DOWN) {
                        field = value
                    }
                }
                DIRECTION.DIRECTION_DOWN -> {
                    if (field != DIRECTION.DIRECTION_UP) {
                        field = value
                    }
                }
                DIRECTION.DIRECTION_LEFT -> {
                    if (field != DIRECTION.DIRECTION_RIGHT) {
                        field = value
                    }
                }
                DIRECTION.DIRECTION_RIGHT -> {
                    if (field != DIRECTION.DIRECTION_LEFT) {
                        field = value
                    }
                }
            }
        }

    private var frequency: Long = 800//贪吃蛇移动的速率

    /**
     *獲取遊戲是否已經開始
     * */
    var isStarted = false
        private set

    private val random = Random()

    var isRunning = true

    /**
     * 繪製遊戲對象
     * */
    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        if (!isStarted)
            return

        drawSnake(canvas!!)
        drawFood(canvas)
    }

    /**
     * 繪製貪吃蛇
     * */
    private fun drawSnake(canvas: Canvas) {
        snake.forEach {
            val pointF = this.gridList[it.row][it.column]
            if (it.isHead) {
                it.draw(canvas, pointF.x, pointF.y, SnakeGamePaint.snakeHeaderPaint)
            } else {
                it.draw(canvas, pointF.x, pointF.y, SnakeGamePaint.snakeBodyPaint)
            }
        }
    }

    /**
     *繪製食物
     * */
    private fun drawFood(canvas: Canvas) {
        val pointF = this.gridList[food.row][food.column]

        food.draw(canvas, pointF.x, pointF.y, SnakeGamePaint.foodPaint)
    }

    /**
     * 移動貪吃蛇
     * */
    private fun moveTo() {
        //預先計算好蛇頭將要到達的位置
        var newHeadRow = snake[0].row
        var newHeadColumn = snake[0].column
        when (this.direction) {
            DIRECTION.DIRECTION_UP -> {
                newHeadRow -= 1
            }
            DIRECTION.DIRECTION_DOWN -> {
                newHeadRow += 1
            }
            DIRECTION.DIRECTION_LEFT -> {
                newHeadColumn -= 1
            }
            DIRECTION.DIRECTION_RIGHT -> {
                newHeadColumn += 1
            }
        }

        //檢測是否吃到食物
        if (food.row == newHeadRow && food.column == newHeadColumn) {
            //如果吃到了食物，則不移動貪吃蛇，將食物的位置變為貪吃蛇的腦袋
            snake[0].isHead = false

            val newHead = SnakeBlock(newHeadRow, newHeadColumn, true)
            snake.add(0, newHead)

            if (this.eatenListener != null) {
                this.eatenListener!!.onEaten()
            }
            //加速貪吃蛇的移動速度
            if (frequency > 500) {
                frequency -= 50
            }
            //重新生成食物
            generateFoodInRandom()
        } else {
            //碰撞檢測開始
            //想蛇頭方向移動貪吃蛇的身子
            for (i in this.snake.size - 1 downTo 1) {
                val previous = this.snake[i - 1]
                val current = this.snake[i]
                current.row = previous.row
                current.column = previous.column

            }
            //移動蛇頭
            val head = snake[0]
            head.row = newHeadRow
            head.column = newHeadColumn

            //判斷超出邊界
            if (head.row < 0
                || head.row > SnakeGameConfiguration.GAME_ROW_COUNT - 1
                || head.column < 0
                || head.column > SnakeGameConfiguration.GAME_COLUMN_COUNT - 1
            ) {
                isStarted = false
                if (this.crashListener != null) {
                    //"Out of the border".e()
                    //"head row ${head.row}".e()
                    //"head column ${head.column}".e()
                    crashListener!!.onCrash()
                }
            }
            //和自己碰撞的檢測
            else if (snake.firstOrNull { it.isHead == false && it.row == head.row && it.column == head.column } != null) {
                isStarted = false
                if (this.crashListener != null) {
                    //"Catch itself".e()
                    //"head row ${head.row}".e()
                    //"head column ${head.column}".e()
                    crashListener!!.onCrash()
                }
            }
            //碰撞檢測結束
        }

        //重繪
        this.invalidate()
    }

    /**
     * 測量地圖獲取地圖的基本參數
     * */
    private fun measureGameMap() {
        val w = this.width
        val h = this.height
        SnakeGameConfiguration.GRID_HEIGHT = (h / SnakeGameConfiguration.GAME_ROW_COUNT).toFloat()
        SnakeGameConfiguration.GRID_WIDTH = (w / SnakeGameConfiguration.GAME_COLUMN_COUNT).toFloat()
    }

    /**
     * 生成遊戲的單元格
     * */
    private fun generateGird() {
        this.gridList = mutableListOf()

        for (i in 0 until SnakeGameConfiguration.GAME_ROW_COUNT) {
            val tempList = mutableListOf<PointF>()
            for (j in 0 until SnakeGameConfiguration.GAME_COLUMN_COUNT) {
                val point = PointF(j * SnakeGameConfiguration.GRID_WIDTH, i * SnakeGameConfiguration.GRID_HEIGHT)
                tempList.add(point)
            }
            this.gridList.add(tempList)
        }
    }

    /**
     * 隨機生成食物
     * */
    private fun generateFoodInRandom() {
        var row = this.random.nextInt(SnakeGameConfiguration.GAME_ROW_COUNT)
        var column = this.random.nextInt(SnakeGameConfiguration.GAME_COLUMN_COUNT)
        while (true) {
            //避免生成的食物和貪吃蛇的位置重疊
            if (this.snake.firstOrNull { it.row == row && it.column == column } == null)
                break
            row = this.random.nextInt(SnakeGameConfiguration.GAME_ROW_COUNT)
            column = this.random.nextInt(SnakeGameConfiguration.GAME_COLUMN_COUNT)
        }
        this.food = Food(row, column)
    }

    /**
     * 生成最初的貪吃蛇
     * */
    private fun generateSnake() {
        this.snake.clear()
        this.snake.add(SnakeBlock(0, 2, true))
        this.snake.add(SnakeBlock(0, 1, false))
        this.snake.add(SnakeBlock(0, 0, false))
    }

    /**
     * 開始遊戲
     * */
    fun start(view: View) {
        //初始化地圖
        //1. 計算地圖的佈局
        this.measureGameMap()

        this.generateGird()
        this.generateFoodInRandom()
        this.generateSnake()

        isStarted = true
        this.invalidate()
        //開始線程移動貪吃蛇
        thread {
            while (isRunning) {
                if (isStarted) {
                    //通過線程的睡眠，來控制貪吃蛇的移動速度
                    this.post {
                        view.bringToFront()
                        view.alpha
                        moveTo()
                    }
                    SystemClock.sleep(this.frequency)
                }
            }
        }
    }

    /**
     * 重新開始
     * */
    fun restart() {
        this.generateGird()
        this.generateFoodInRandom()
        this.generateSnake()
        this.direction = DIRECTION.DIRECTION_RIGHT
        isStarted = true
        invalidate()
    }

    /**
     * 貪吃蛇移動的方向的常數類
     * */
    object DIRECTION {
        val DIRECTION_UP = 0
        val DIRECTION_DOWN = 1
        val DIRECTION_LEFT = 2
        val DIRECTION_RIGHT = 3
    }
}