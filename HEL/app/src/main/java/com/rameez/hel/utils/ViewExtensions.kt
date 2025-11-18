package com.rameez.hel.utils

import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.ScaleAnimation

// Button press animation
fun View.animatePress() {
    val anim = ScaleAnimation(
        1f, 0.9f,
        1f, 0.9f,
        ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
        ScaleAnimation.RELATIVE_TO_SELF, 0.5f
    )
    anim.duration = 80
    anim.fillAfter = true
    this.startAnimation(anim)
}

// Auto-repeat on long press
fun View.setupAutoRepeat(
    interval: Long = 120,
    action: () -> Unit
) {
    val handler = Handler(Looper.getMainLooper())
    var isPressed = false

    val runnable = object : Runnable {
        override fun run() {
            if (isPressed) {
                action()
                handler.postDelayed(this, interval)
            }
        }
    }

    setOnLongClickListener {
        isPressed = true
        action()
        handler.postDelayed(runnable, interval)
        true
    }

    setOnTouchListener { _, event ->
        if (event.action == android.view.MotionEvent.ACTION_UP ||
            event.action == android.view.MotionEvent.ACTION_CANCEL
        ) {
            isPressed = false
        }
        false
    }
}
