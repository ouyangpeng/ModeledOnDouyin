package com.csdn.oyp.douyin.adapter

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.VideoView
import androidx.viewpager.widget.PagerAdapter
import com.csdn.oyp.douyin.R
import java.util.*

class ViewPagerAdapter(
    private val context: Context,
    private val imgArray: IntArray,
    private val videoArray: IntArray
) : PagerAdapter() {
    // 自己做一个缓存，来缓存view和position的关联关系
    private val indexToView: MutableMap<Int, View> = HashMap()

    fun getIndexToView(): Map<Int, View> {
        return indexToView
    }

    override fun getCount(): Int {
        return videoArray.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val itemView = View.inflate(context, R.layout.item_view_pager, null)
        val imgThumb = itemView.findViewById<ImageView>(R.id.img_thumb)
        val videoView = itemView.findViewById<VideoView>(R.id.video_view)
        val imgPlay = itemView.findViewById<ImageView>(R.id.img_play)
        imgThumb.setImageResource(imgArray[position])
        videoView.setVideoURI(Uri.parse("android.resource://" + context.packageName + "/" + videoArray[position]))
        videoView.setOnPreparedListener { Log.d(TAG, "playVideo() onPrepared()") }
        videoView.setOnInfoListener { mp, what, extra ->
            mp.isLooping = true
            imgThumb.animate().alpha(0f).setDuration(200).start()
            false
        }

        videoView.setZOrderMediaOverlay(true)
        videoView.setZOrderOnTop(true)

        // 如果是第一个View，直接播放，不然会黑屏
        // 其他的View，滑动过去的时候，再播放，不然滑动过去的时候播放的是不是第一帧
        if (position == 0) {
            videoView.start()
        }

        imgPlay.setOnClickListener(object : View.OnClickListener {
            var isPlaying = true
            override fun onClick(v: View) {
                isPlaying = if (videoView.isPlaying) {
                    imgPlay.animate().alpha(0.7f).start()
                    videoView.pause()
                    false
                } else {
                    imgPlay.animate().alpha(0f).start()
                    videoView.start()
                    true
                }
            }
        })

        container.addView(itemView)
        // 缓存起来view和position的关联关系
        indexToView[position] = itemView
        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val itemView = `object` as View
        val videoView = itemView.findViewById<VideoView>(R.id.video_view)
        val imgThumb = itemView.findViewById<ImageView>(R.id.img_thumb)
        val imgPlay = itemView.findViewById<ImageView>(R.id.img_play)
        videoView.stopPlayback()
        imgThumb.animate().alpha(1f).start()
        imgPlay.animate().alpha(0f).start()
        container.removeView(itemView)
    }

    companion object {
        private const val TAG = "ViewPagerAdapter"
    }
}