package com.elivesgamez.mainlandrp

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd

object Animations {

    /**
     * Slow continuous zoom + drift on a full-bleed background ImageView,
     * giving the illusion of a living scene behind static art.
     */
    fun kenBurns(view: View) {
        view.scaleX = 1f
        view.scaleY = 1f
        val zoomIn = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, 1.08f),
                ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, 1.08f),
                ObjectAnimator.ofFloat(view, View.TRANSLATION_X, 0f, -18f)
            )
            duration = 9000
            interpolator = LinearInterpolator()
        }
        val zoomOut = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, View.SCALE_X, 1.08f, 1f),
                ObjectAnimator.ofFloat(view, View.SCALE_Y, 1.08f, 1f),
                ObjectAnimator.ofFloat(view, View.TRANSLATION_X, -18f, 0f)
            )
            duration = 9000
            interpolator = LinearInterpolator()
        }
        fun loop() {
            zoomIn.start()
            zoomIn.doOnEnd { zoomOut.start() }
            zoomOut.doOnEnd { loop() }
        }
        // guard against double-loop registration if called twice
        zoomIn.removeAllListeners()
        zoomOut.removeAllListeners()
        loop()
    }

    /**
     * Diagonal light sweep across a view (place a shimmer_gradient-backed
     * View slightly wider than its container over the target area).
     */
    fun shimmerSweep(shimmerView: View, containerWidth: Int) {
        shimmerView.translationX = -containerWidth.toFloat()
        val anim = ObjectAnimator.ofFloat(
            shimmerView, View.TRANSLATION_X,
            -containerWidth.toFloat(), containerWidth.toFloat() * 1.5f
        )
        anim.duration = 2600
        anim.startDelay = 900
        anim.interpolator = LinearInterpolator()
        anim.addListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(animation: Animator) {
                shimmerView.postDelayed({ anim.start() }, 1800)
            }
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        anim.start()
    }

    /** Gentle breathing scale on a call-to-action button. */
    fun pulse(view: View) {
        val anim = ValueAnimator.ofFloat(1f, 1.035f, 1f)
        anim.duration = 1600
        anim.repeatCount = ValueAnimator.INFINITE
        anim.interpolator = android.view.animation.AccelerateDecelerateInterpolator()
        anim.addUpdateListener {
            val v = it.animatedValue as Float
            view.scaleX = v
            view.scaleY = v
        }
        anim.start()
    }

    /** Animate a fill view's width from 0 to [targetWidth] px. */
    fun growWidth(view: View, targetWidth: Int, durationMs: Long) {
        val anim = ValueAnimator.ofInt(0, targetWidth)
        anim.duration = durationMs
        anim.addUpdateListener {
            val params = view.layoutParams
            params.width = it.animatedValue as Int
            view.layoutParams = params
        }
        anim.start()
    }
}
