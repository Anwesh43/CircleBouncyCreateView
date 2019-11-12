package com.anwesh.uiprojects.circlebouncycreateview

/**
 * Created by anweshmishra on 12/11/19.
 */

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.Color

val nodes : Int = 5
val parts : Int = 2
val scGap : Float = 0.02f
val strokeFactor : Int = 90
val sizeFactor : Float = 2.9f
val foreColor : Int = Color.parseColor("#673AB7")
val backColor : Int = Color.parseColor("#BDBDBD")
val delay : Long = 20

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()
fun Float.cosify() : Float = 1f - Math.sin(Math.PI / 2 + (Math.PI / 2) * this).toFloat()

fun Canvas.drawCircleBouncyLine(i : Int, scale : Float, size : Float, h : Float, paint : Paint) {
    val sf : Float = scale.sinify()
    val y : Float = (h / 2 - size) * sf
    val cy : Float = (h / 2 - size) * scale.divideScale(1, 2).cosify()
    save()
    scale(1f, 1f - 2 * i)
    drawLine(0f, 0f, 0f, y, paint)
    drawCircle(0f, cy, size, paint)
    restore()
}

fun Canvas.drawCBCNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (nodes + 1)
    val size : Float = gap / sizeFactor
    paint.color = foreColor
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    paint.strokeCap = Paint.Cap.ROUND
    save()
    translate(gap * (i + 1), h / 2)
    for (j in 0..1) {
        drawCircleBouncyLine(i, scale, size, h, paint)
    }
    restore()
}

class CircleBouncyCreateView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class CBCNode(var i : Int, val state : State = State()) {

        private var next : CBCNode? = null
        private var prev : CBCNode? = null

        init {

        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = CBCNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawCBCNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : CBCNode {
            var curr : CBCNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class CircleBouncyCreate(var i : Int) {

        private val root : CBCNode = CBCNode(0)
        private var curr : CBCNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : CircleBouncyCreateView) {

        private val animator : Animator = Animator(view)
        private val cbc : CircleBouncyCreate = CircleBouncyCreate(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            cbc.draw(canvas, paint)
            animator.animate {
                cbc.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            cbc.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : CircleBouncyCreateView {
            val view : CircleBouncyCreateView = CircleBouncyCreateView(activity)
            activity.setContentView(view)
            return view
        }
    }
}