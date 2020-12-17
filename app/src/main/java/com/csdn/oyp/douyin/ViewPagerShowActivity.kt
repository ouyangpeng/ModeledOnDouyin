package com.csdn.oyp.douyin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.csdn.oyp.douyin.adapter.ViewPagerAdapter
import com.csdn.oyp.douyin.utils.SizeConvertUtil
import com.csdn.oyp.douyin.widget.SlidingIndicator

class ViewPagerShowActivity : AppCompatActivity() {
    private val imgs = intArrayOf(
        R.mipmap.img_video_1,
        R.mipmap.img_video_2,
        R.mipmap.img_video_3,
        R.mipmap.img_video_4,
        R.mipmap.img_video_5,
        R.mipmap.img_video_6,
        R.mipmap.img_video_7,
        R.mipmap.img_video_8
    )
    private val videos = intArrayOf(
        R.raw.video_1,
        R.raw.video_2,
        R.raw.video_3,
        R.raw.video_4,
        R.raw.video_5,
        R.raw.video_6,
        R.raw.video_7,
        R.raw.video_8
    )
    var slidingIndicator: SlidingIndicator? = null
    var viewPager: ViewPager? = null
    var viewPagerAdapter: ViewPagerAdapter? = null

    //手指是否触摸屏幕
    private var isPress = false

    //是否打开下一个activity
    private var isOpen = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewpager_show)
        initView()
    }

    private fun initView() {
        slidingIndicator = findViewById(R.id.dot_group)
        slidingIndicator?.setNumber(videos.size)
        slidingIndicator?.setIvHeight(SizeConvertUtil.dpToPx(this, 4f))
        slidingIndicator?.setIvWidth(SizeConvertUtil.dpToPx(this, 8f))
        slidingIndicator?.slidingIndicatorShow()

        viewPager = findViewById(R.id.vp_guide)
        viewPagerAdapter = ViewPagerAdapter(this, imgs, videos)
        viewPager?.setAdapter(viewPagerAdapter)
        viewPager?.addOnPageChangeListener(object : OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                Log.d(
                    TAG,
                    "onPageScrolled() position= $position ,positionOffset =$positionOffset ,positionOffsetPixels =$positionOffsetPixels"
                )
                //通过该方法设置slidingIndicator的滑动及变形事件
                slidingIndicator?.setViewLayoutParams(position, positionOffset)

                // 参考自： ViewPager滑动到最后一页继续滑动   https://blog.csdn.net/daidaishuiping/article/details/68086727
                if (videos.size - 1 == position && isPress && positionOffsetPixels == 0 && !isOpen) {
                    isOpen = true //防止多次添加activity
                    val intent = Intent(this@ViewPagerShowActivity, RecyclerViewShowActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }

            override fun onPageSelected(position: Int) {
                Log.d(TAG, "onPageSelected() position= $position")
                // 从缓存中拿到对应位置的view
                val videoView: VideoView =
                    viewPagerAdapter!!.getIndexToView()[position]!!.findViewById(R.id.video_view)
                // 开始播放
                videoView.start()
            }

            override fun onPageScrollStateChanged(state: Int) {
                Log.d(TAG, "onPageScrollStateChanged() state= $state")
                isPress = state == ViewPager.SCROLL_STATE_DRAGGING
            }
        })
    }

    companion object {
        private const val TAG = "Main2Activity"
    }
}