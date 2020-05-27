package com.blood.snake.bean

class FrameBean {

    val width = 1080f
    val height = 1920f
    val offset = 90f // 内间距
    val gridCount = 30 // 每行格子的数量
    val frameWidth by lazy { width - offset * 2 } // 布局宽度
    var gridWidth = frameWidth / gridCount // 格子宽

}