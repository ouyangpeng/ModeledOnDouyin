package com.csdn.oyp.douyin

import android.annotation.TargetApi
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import com.csdn.oyp.douyin.adapter.MyRecyclerViewAdapter
import com.csdn.oyp.douyin.widget.MyLayoutManager
import com.csdn.oyp.douyin.widget.OnViewPagerListener
import com.csdn.oyp.douyin.widget.TextureVideoView

class RecyclerViewShowActivity : AppCompatActivity() {
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: MyRecyclerViewAdapter? = null
    var myLayoutManager: MyLayoutManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recyclerview_show)
        initView()
        initListener()
    }

    private fun initView() {
        myLayoutManager = MyLayoutManager(this, OrientationHelper.VERTICAL, false)
        mAdapter = MyRecyclerViewAdapter(this)
        mRecyclerView = findViewById(R.id.recycler)
        mRecyclerView?.setLayoutManager(myLayoutManager)
        mRecyclerView?.setAdapter(mAdapter)
    }

    private fun initListener() {
        myLayoutManager!!.setOnViewPagerListener(object : OnViewPagerListener {
            override fun onInitComplete() {}
            override fun onPageRelease(isNext: Boolean, position: Int) {
                Log.e(TAG, "释放位置:$position 下一页:$isNext")
                var index = 0
                index = if (isNext) {
                    0
                } else {
                    1
                }
                releaseVideo(index)
            }

            override fun onPageSelected(position: Int, bottom: Boolean) {
                Log.e(TAG, "选择位置:$position 下一页:$bottom")
                playVideo(0)
            }
        })
    }

    private fun releaseVideo(index: Int) {
        val itemView = mRecyclerView!!.getChildAt(index)
        val videoView = itemView.findViewById<TextureVideoView>(R.id.video_view)
        val imgThumb = itemView.findViewById<ImageView>(R.id.img_thumb)
        val imgPlay = itemView.findViewById<ImageView>(R.id.img_play)
        videoView.stopPlayback()
        imgThumb.animate().alpha(1f).start()
        imgPlay.animate().alpha(0f).start()
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun playVideo(position: Int) {
        val itemView = mRecyclerView!!.getChildAt(position)
        val videoView = itemView.findViewById<TextureVideoView>(R.id.video_view)
        val imgPlay = itemView.findViewById<ImageView>(R.id.img_play)
        val imgThumb = itemView.findViewById<ImageView>(R.id.img_thumb)
        val rootView = itemView.findViewById<RelativeLayout>(R.id.root_view)
        val mediaPlayer = arrayOfNulls<MediaPlayer>(1)
        videoView.setOnPreparedListener { }
        videoView.setOnInfoListener { mp, what, extra ->
            mediaPlayer[0] = mp
            mp.isLooping = true
            imgThumb.animate().alpha(0f).setDuration(200).start()
            false
        }
        videoView.start()
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
    }

    companion object {
        private const val TAG = "RecyclerViewShowActivity"
    }
}