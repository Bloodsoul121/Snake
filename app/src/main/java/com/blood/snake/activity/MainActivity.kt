package com.blood.snake.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.blood.snake.R
import com.blood.snake.view.FrameGridView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        snake.setCallback(object : FrameGridView.Callback {

            override fun onProgress(result: Int) {
                num.text = "score : $result"
            }

            override fun onGameOver() {
                AlertDialog.Builder(this@MainActivity).setMessage("Game Over").create().show()
            }
        })
    }

    fun start(view: View) {
        snake.start()
    }

    fun speedup(view: View) {
        snake.speedUp()
    }
}
