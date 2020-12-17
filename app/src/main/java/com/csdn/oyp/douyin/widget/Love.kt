package com.csdn.oyp.douyin.widget

import android.animation.*
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import com.csdn.oyp.douyin.R
import java.util.*

class Love : RelativeLayout {
    private var mContext: Context? = null
    var num = floatArrayOf(-30f, -20f, 0f, 20f, 30f) //随机心形图片角度

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(context)
    }

    private fun initView(context: Context) {
        mContext = context
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val imageView = ImageView(mContext)
        val params = LayoutParams(300, 300)
        params.leftMargin = event.x.toInt() - 150
        params.topMargin = event.y.toInt() - 300
        imageView.setImageDrawable(resources.getDrawable(R.mipmap.icon_home_like_after))
        imageView.layoutParams = params
        addView(imageView)

        val animatorSet = AnimatorSet()
        animatorSet.play(scale(imageView, "scaleX", 2f, 0.9f, 100, 0))
            .with(scale(imageView, "scaleY", 2f, 0.9f, 100, 0))
            .with(rotation(imageView, 0, 0, num[Random().nextInt(4)]))
            .with(alpha(imageView, 0f, 1f, 100, 0))
            .with(scale(imageView, "scaleX", 0.9f, 1f, 50, 150))
            .with(scale(imageView, "scaleY", 0.9f, 1f, 50, 150))
            .with(translationY(imageView, 0f, -600f, 800, 400))
            .with(alpha(imageView, 1f, 0f, 300, 400))
            .with(scale(imageView, "scaleX", 1f, 3f, 700, 400))
            .with(scale(imageView, "scaleY", 1f, 3f, 700, 400))

        animatorSet.start()

        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                removeViewInLayout(imageView)
            }
        })

        return super.onTouchEvent(event)
    }

    companion object {
        fun scale(
            view: View?,
            propertyName: String?,
            from: Float,
            to: Float,
            time: Long,
            delayTime: Long
        ): ObjectAnimator {
            val translation = ObjectAnimator.ofFloat(
                view, propertyName, from, to
            )
            translation.interpolator = LinearInterpolator()
            translation.startDelay = delayTime
            translation.duration = time
            return translation
        }

        fun translationX(
            view: View?,
            from: Float,
            to: Float,
            time: Long,
            delayTime: Long
        ): ObjectAnimator {
            val translation = ObjectAnimator.ofFloat(
                view, "translationX", from, to
            )
            translation.interpolator = LinearInterpolator()
            translation.startDelay = delayTime
            translation.duration = time
            return translation
        }

        fun translationY(
            view: View?,
            from: Float,
            to: Float,
            time: Long,
            delayTime: Long
        ): ObjectAnimator {
            val translation = ObjectAnimator.ofFloat(
                view, "translationY", from, to
            )
            translation.interpolator = LinearInterpolator()
            translation.startDelay = delayTime
            translation.duration = time
            return translation
        }

        fun alpha(
            view: View?,
            from: Float,
            to: Float,
            time: Long,
            delayTime: Long
        ): ObjectAnimator {
            val translation = ObjectAnimator.ofFloat(
                view, "alpha", from, to
            )
            translation.interpolator = LinearInterpolator()
            translation.startDelay = delayTime
            translation.duration = time
            return translation
        }

        fun rotation(
            view: View?,
            time: Long,
            delayTime: Long,
            vararg values: Float
        ): ObjectAnimator {
            val rotation = ObjectAnimator.ofFloat(view, "rotation", *values)
            rotation.duration = time
            rotation.startDelay = delayTime
            rotation.interpolator = TimeInterpolator { input -> input }
            return rotation
        }
    }
}