package com.csdn.oyp.douyin.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import com.csdn.oyp.douyin.R

//   参考自： https://blog.csdn.net/hhw332704304/article/details/89486901
class SlidingIndicator @JvmOverloads constructor(
    private val mContext: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HorizontalScrollView(
    mContext, attrs, defStyleAttr
) {
    private var mNumber = 1
    private var mLinearLayout: LinearLayout? = null
    private var mFrameLayout: FrameLayout? = null
    private var imageView: ImageView? = null
    var leftMargin = 0f
    private var ivWidth = 40
    private var ivHeight = 20
    private var dotBgId = R.drawable.bg_page_indicator_unfocused
    private var ivBgid = R.drawable.bg_page_indicator_focused

    /**
     * 设置指示器点的个数
     */
    fun setNumber(number: Int) {
        if (number > 0) {
            mNumber = number
        }
    }

    /**
     * 设置指示器滚动条宽度
     */
    fun setIvWidth(width: Int) {
        if (width > 0 && width <= 100) {
            ivWidth = width
        }
    }

    /**
     * 设置指示器滚动条高度
     */
    fun setIvHeight(height: Int) {
        if (height > 0 && height <= 100) {
            ivHeight = height
        }
    }

    /**
     * 展示指示器
     */
    fun slidingIndicatorShow() {
        init(mContext, ivWidth, ivHeight)
    }

    /**
     * 设置底部小点背景图片或颜色
     */
    fun setDotBg(drawableId: Int) {
        dotBgId = drawableId
    }

    /**
     * 设置滑动滚动条背景
     */
    fun setIvSlidingBg(drawableId: Int) {
        ivBgid = drawableId
    }

    private fun init(context: Context, width: Int, height: Int) {
        mFrameLayout = FrameLayout(context)
        mFrameLayout!!.layoutParams = LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        mLinearLayout = LinearLayout(context)
        mLinearLayout!!.orientation = LinearLayout.HORIZONTAL
        mLinearLayout!!.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        // 高亮的块
        imageView = ImageView(context)
        val vmi = MarginLayoutParams(width, height)
        imageView!!.layoutParams = vmi
        imageView!!.setBackgroundResource(ivBgid)

        // 背景的小灰显的块
        for (i in 0 until mNumber) {
            val imageView = ImageView(context)
            imageView.setBackgroundResource(dotBgId)
            // 代码动态设置间距，setMargins没有作用，出现无效的情况。
            // https://blog.csdn.net/a260724032/article/details/81028820
            // https://wenwen.sogou.com/z/q892890111.htm
            // https://www.jianshu.com/p/5c3b23cc4dce
            val vm = LinearLayout.LayoutParams(width / 2, height)
            vm.setMargins(height / 2, 0, height / 2, 0)
            imageView.layoutParams = vm
            mLinearLayout!!.addView(imageView)
        }
        mFrameLayout!!.addView(mLinearLayout)
        mFrameLayout!!.addView(imageView)
        addView(mFrameLayout)
    }

    /**
     * 根据viewpager滑动而滑动的指示器
     */
    fun setViewLayoutParams(position: Int, positionOffset: Float) {
        Log.d(TAG, "setViewLayoutParams() position = $position ,positionOffset =$positionOffset")
        var viewWidth = ivWidth
        //当posion == 0时算法 为1？
        if (positionOffset > 0 && positionOffset <= 0.5) {
            viewWidth = (viewWidth * (1 - positionOffset)).toInt()
            leftMargin = (ivWidth + viewWidth) * positionOffset + ivWidth * position
        } else if (positionOffset > 0.5 && positionOffset <= 1) {
            viewWidth = (ivWidth * positionOffset).toInt()
            leftMargin = (ivWidth + ivWidth - viewWidth) * positionOffset + ivWidth * position
        }
        val params = imageView!!.layoutParams as MarginLayoutParams
        params.width = viewWidth
        params.leftMargin = Math.round(leftMargin)
        imageView!!.layoutParams = params
    }

    companion object {
        private const val TAG = "SlidingIndicator"
    }
}