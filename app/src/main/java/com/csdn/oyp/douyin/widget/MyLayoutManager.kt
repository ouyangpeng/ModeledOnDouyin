package com.csdn.oyp.douyin.widget

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnChildAttachStateChangeListener
import androidx.recyclerview.widget.RecyclerView.Recycler

class MyLayoutManager : LinearLayoutManager, OnChildAttachStateChangeListener {
    private var mDrift //位移，用来判断移动方向
            = 0
    private var mPagerSnapHelper: PagerSnapHelper? = null
    private var mOnViewPagerListener: OnViewPagerListener? = null

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, orientation: Int, reverseLayout: Boolean) : super(
        context,
        orientation,
        reverseLayout
    ) {
        mPagerSnapHelper = PagerSnapHelper()
    }

    override fun onAttachedToWindow(view: RecyclerView) {
        view.addOnChildAttachStateChangeListener(this)
        mPagerSnapHelper!!.attachToRecyclerView(view)
        super.onAttachedToWindow(view)
    }

    //当Item添加进来了  调用这个方法
    override fun onChildViewAttachedToWindow(view: View) {
        // 播放视频操作 即将要播放的是上一个视频 还是下一个视频
        val position = getPosition(view)
        if (0 == position) {
            if (mOnViewPagerListener != null) {
                mOnViewPagerListener!!.onPageSelected(getPosition(view), false)
            }
        }
    }

    fun setOnViewPagerListener(mOnViewPagerListener: OnViewPagerListener?) {
        this.mOnViewPagerListener = mOnViewPagerListener
    }

    override fun onScrollStateChanged(state: Int) {
        when (state) {
            RecyclerView.SCROLL_STATE_IDLE -> {
                val view = mPagerSnapHelper!!.findSnapView(this)
                val position = getPosition(view!!)
                if (mOnViewPagerListener != null) {
                    mOnViewPagerListener!!.onPageSelected(position, position == itemCount - 1)
                }
            }
        }
        super.onScrollStateChanged(state)
    }

    override fun onChildViewDetachedFromWindow(view: View) {
        //暂停播放操作
        if (mDrift >= 0) {
            if (mOnViewPagerListener != null) mOnViewPagerListener!!.onPageRelease(
                true,
                getPosition(view)
            )
        } else {
            if (mOnViewPagerListener != null) mOnViewPagerListener!!.onPageRelease(
                false,
                getPosition(view)
            )
        }
    }

    override fun scrollVerticallyBy(dy: Int, recycler: Recycler, state: RecyclerView.State): Int {
        mDrift = dy
        return super.scrollVerticallyBy(dy, recycler, state)
    }

    override fun canScrollVertically(): Boolean {
        return true
    }
}