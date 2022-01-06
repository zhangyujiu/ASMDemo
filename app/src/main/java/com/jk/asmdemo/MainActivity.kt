package com.jk.asmdemo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.button).setOnClickListener {
            test()
        }
        findViewById<View>(R.id.button1).setOnClickListener {
            test1()
        }
    }

    @AsmInject
    fun test() {
        Thread.sleep(2000)
    }

    fun test1() {

    }
}