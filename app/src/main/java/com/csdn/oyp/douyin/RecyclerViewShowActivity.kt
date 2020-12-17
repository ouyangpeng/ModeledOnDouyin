package com.csdn.oyp.douyin

import android.annotation.TargetApi
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import com.csdn.oyp.douyin.widget.MyLayoutManager
import com.csdn.oyp.douyin.widget.OnViewPagerListener

class RecyclerViewShowActivity : AppCompatActivity() {
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: MyAdapter? = null
    var myLayoutManager: MyLayoutManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recyclerview_show)
        initView()
        initListener()
    }

    private fun initView() {
        myLayoutManager = MyLayoutManager(this, OrientationHelper.VERTICAL, false)
        mAdapter = MyAdapter(this)
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

    internal inner class MyAdapter(private val mContext: Context) :
        RecyclerView.Adapter<MyAdapter.ViewHolder>() {
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
        private var index = 0
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_view_pager, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.img_thumb.setImageResource(imgs[index])
            holder.videoView.setVideoURI(Uri.parse("android.resource://" + packageName + "/" + videos[index]))
            index++
            if (index >= 7) {
                index = 0
            }
        }

        override fun getItemCount(): Int {
            return 88
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var img_thumb: ImageView
            var videoView: VideoView
            var img_play: ImageView
            var rootView: RelativeLayout

            init {
                img_thumb = itemView.findViewById(R.id.img_thumb)
                videoView = itemView.findViewById(R.id.video_view)
                img_play = itemView.findViewById(R.id.img_play)
                rootView = itemView.findViewById(R.id.root_view)
            }
        }
    }

    private fun releaseVideo(index: Int) {
        val itemView = mRecyclerView!!.getChildAt(index)
        val videoView = itemView.findViewById<VideoView>(R.id.video_view)
        val imgThumb = itemView.findViewById<ImageView>(R.id.img_thumb)
        val imgPlay = itemView.findViewById<ImageView>(R.id.img_play)
        videoView.stopPlayback()
        imgThumb.animate().alpha(1f).start()
        imgPlay.animate().alpha(0f).start()
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun playVideo(position: Int) {
        val itemView = mRecyclerView!!.getChildAt(position)
        val videoView = itemView.findViewById<VideoView>(R.id.video_view)
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
        private const val TAG = "douyin"
    }
}