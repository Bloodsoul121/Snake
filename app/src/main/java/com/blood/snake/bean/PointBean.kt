package com.blood.snake.bean

class PointBean {

    var x: Int
    var y: Int

    constructor() {
        this.x = 0
        this.y = 0
    }

    constructor(x: Int, y: Int) : this() {
        this.x = x
        this.y = y
    }

    override fun equals(other: Any?): Boolean {
        if (other is PointBean) {
            return x == other.x && y == other.y
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }

}