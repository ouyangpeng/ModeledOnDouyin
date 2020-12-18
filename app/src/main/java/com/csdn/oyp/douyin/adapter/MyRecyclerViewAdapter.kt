package com.csdn.oyp.douyin.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.csdn.oyp.douyin.R
import com.csdn.oyp.douyin.widget.TextureVideoView

class MyRecyclerViewAdapter(private val mContext: Context) :
        RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder>() {
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
            holder.videoView.setVideoURI(Uri.parse("android.resource://" + mContext.packageName + "/" + videos[index]))

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
            var videoView: TextureVideoView
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