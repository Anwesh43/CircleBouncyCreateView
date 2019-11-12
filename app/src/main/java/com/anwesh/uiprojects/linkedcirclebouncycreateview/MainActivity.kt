package com.anwesh.uiprojects.linkedcirclebouncycreateview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.circlebouncycreateview.CircleBouncyCreateView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CircleBouncyCreateView.create(this)
    }
}
