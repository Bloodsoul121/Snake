package com.blood.snake.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.blood.snake.R
import com.blood.snake.bean.Direction
import com.blood.snake.bean.FrameBean
import com.blood.snake.bean.PointBean
import com.blood.snake.bean.SnakeBean
import kotlin.math.abs
import kotlin.random.Random

class FrameGridView : View, GestureDetector.OnGestureListener, View.OnTouchListener {

    companion object {
        const val BASE_SNAKE_LENGTH = 3
        const val BASE_SPEED = 800L
        const val BASE_SPEED_INTERVAL = 80L
        const val BASE_SPEED_MOST_FAST = 250L

        const val WHAT_COMMAND_MOVE = 0
        const val WHAT_COMMAND_RANDOM_POINT = 1
        const val WHAT_COMMAND_RANDOM_SPEED_UP = 2
    }

    private var isRunning = false
    private var isPrepareAdd = false
    private var speed = BASE_SPEED
    private var nextDirection: Direction = Direction.RIGHT
    private var frameBean: FrameBean = FrameBean()
    private var snakeBean: SnakeBean = SnakeBean()
    private var randomPointBean: PointBean = PointBean(-1, -1)
    private lateinit var randomAddPointBean: PointBean
    private val paint: Paint
    private val gesture = GestureDetector(context, this)
    private var callback: Callback? = null
    private val beanBitmap = BitmapFactory.decodeResource(resources, R.drawable.bean)
    private val beanBitmapRectF = RectF()

    private val handle: Handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                WHAT_COMMAND_MOVE -> {
                    if (isRunning) {
                        move(nextDirection)
                        sendEmptyMessageDelayed(WHAT_COMMAND_MOVE, speed)
                    }
                }
                WHAT_COMMAND_RANDOM_POINT -> {
                    randomPointBean = PointBean(
                        Random.nextInt(frameBean.gridCount),
                        Random.nextInt(frameBean.gridCount)
                    )
                }
                WHAT_COMMAND_RANDOM_SPEED_UP -> {
                    if (snakeBean.mPointList.size % 2 == 0) {
                        speedUp()
                    }
                }
            }
        }
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    init {
        createSnake()
        paint = Paint()
        paint.color = Color.BLACK
        paint.isAntiAlias = true
        setOnTouchListener(this)
    }

    private fun createSnake() {
        snakeBean.mPointList.clear()
        val centerXY = frameBean.gridCount / 2
        for (i in 0 until BASE_SNAKE_LENGTH) {
            val pointBean = PointBean(centerXY - i, centerXY)
            snakeBean.mPointList.add(pointBean)
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return gesture.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // 画横线
        paint.color = Color.BLACK
        for (i in 0..frameBean.gridCount) {
            val startX = frameBean.offset
            val startY = frameBean.offset + frameBean.gridWidth * i
            val stopX = frameBean.offset + frameBean.frameWidth
            val stopY = startY
            canvas?.drawLine(startX, startY, stopX, stopY, paint)
        }

        // 画竖线
        paint.color = Color.BLACK
        for (i in 0..frameBean.gridCount) {
            val startX = frameBean.offset + frameBean.gridWidth * i
            val startY = frameBean.offset
            val stopX = startX
            val stopY = frameBean.offset + frameBean.frameWidth
            canvas?.drawLine(startX, startY, stopX, stopY, paint)
        }

        // 画果子
        paint.color = Color.BLUE
        if (randomPointBean.x > -1 && randomPointBean.y > -1) {
            val startX = frameBean.offset + frameBean.gridWidth * randomPointBean.x
            val startY = frameBean.offset + frameBean.gridWidth * randomPointBean.y
            val stopX = startX + frameBean.gridWidth
            val stopY = startY + frameBean.gridWidth
            beanBitmapRectF.set(startX, startY, stopX, stopY)
            canvas?.drawBitmap(beanBitmap, null, beanBitmapRectF, paint)
        }

        // 画蛇
        snakeBean.mPointList.forEachIndexed { index, pointBean ->
            paint.color = if (index == 0) Color.GREEN else Color.GRAY
            val startX = frameBean.offset + frameBean.gridWidth * pointBean.x
            val startY = frameBean.offset + frameBean.gridWidth * pointBean.y
            val radius = frameBean.gridWidth / 2f
            val cx = startX + radius
            val cy = startY + radius
            canvas?.drawCircle(cx, cy, radius, paint)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        handle.removeCallbacksAndMessages(null)
    }

    fun move(direction: Direction) {
        nextDirection = direction
        val firstPoint = snakeBean.mPointList[0]
        val directionPoint = PointBean()
        when (direction) {
            Direction.UP -> {
                directionPoint.x = firstPoint.x
                directionPoint.y = firstPoint.y - 1
            }
            Direction.DOWN -> {
                directionPoint.x = firstPoint.x
                directionPoint.y = firstPoint.y + 1
            }
            Direction.LEFT -> {
                directionPoint.x = firstPoint.x - 1
                directionPoint.y = firstPoint.y
            }
            Direction.RIGHT -> {
                directionPoint.x = firstPoint.x + 1
                directionPoint.y = firstPoint.y
            }
        }

        if (directionPoint.x < 0 || directionPoint.x >= frameBean.gridCount
            || directionPoint.y < 0 || directionPoint.y >= frameBean.gridCount
            || snakeBean.mPointList.contains(directionPoint)
        ) {
            Toast.makeText(
                context,
                "game over : ${snakeBean.mPointList.size - BASE_SNAKE_LENGTH}",
                Toast.LENGTH_LONG
            ).show()
            callback?.onGameOver()
            handle.removeCallbacksAndMessages(null)
            isRunning = false
            return
        }

        snakeBean.mPointList.add(0, directionPoint)
        snakeBean.mPointList.removeAt(snakeBean.mPointList.size - 1)

        if (directionPoint.x == randomPointBean.x && directionPoint.y == randomPointBean.y) {
            isPrepareAdd = true
            randomAddPointBean = randomPointBean
            handle.sendEmptyMessage(WHAT_COMMAND_RANDOM_POINT)
        }

        if (isPrepareAdd && !snakeBean.mPointList.contains(randomAddPointBean)) {
            snakeBean.mPointList.add(randomAddPointBean)
            isPrepareAdd = false
            handle.sendEmptyMessage(WHAT_COMMAND_RANDOM_SPEED_UP)
            callback?.onProgress(snakeBean.mPointList.size - BASE_SNAKE_LENGTH)
        }

        postInvalidate()
    }

    fun start() {
        if (isRunning) {
            Toast.makeText(context, "game is running !", Toast.LENGTH_SHORT).show()
            return
        }

        isPrepareAdd = false
        isRunning = true
        createSnake()
        speed = BASE_SPEED
        nextDirection = Direction.RIGHT
        handle.removeCallbacksAndMessages(null)
        callback?.onProgress(snakeBean.mPointList.size - BASE_SNAKE_LENGTH)
        handle.sendEmptyMessage(WHAT_COMMAND_MOVE)
        handle.sendEmptyMessage(WHAT_COMMAND_RANDOM_POINT)
    }

    fun speedUp() {
        speed -= BASE_SPEED_INTERVAL
        if (speed < BASE_SPEED_MOST_FAST) {
            speed = BASE_SPEED_MOST_FAST
        }
    }

    override fun onShowPress(e: MotionEvent?) {

    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        return false
    }

    override fun onDown(e: MotionEvent?): Boolean {
        return false
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        val offsetX = (e1?.x ?: 0f) - (e2?.x ?: 0f)
        val offsetY = (e1?.y ?: 0f) - (e2?.y ?: 0f)
        val userControlDirection = if (abs(offsetX) > abs(offsetY)) {
            if (offsetX < 0) {
                Direction.RIGHT
            } else {
                Direction.LEFT
            }
        } else {
            if (offsetY < 0) {
                Direction.DOWN
            } else {
                Direction.UP
            }
        }
        when {
            userControlDirection == Direction.UP && nextDirection == Direction.DOWN -> return false
            userControlDirection == Direction.DOWN && nextDirection == Direction.UP -> return false
            userControlDirection == Direction.LEFT && nextDirection == Direction.RIGHT -> return false
            userControlDirection == Direction.RIGHT && nextDirection == Direction.LEFT -> return false
        }
        nextDirection = userControlDirection
        return false
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        return false
    }

    override fun onLongPress(e: MotionEvent?) {

    }

    interface Callback {
        fun onProgress(result: Int)
        fun onGameOver()
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

}