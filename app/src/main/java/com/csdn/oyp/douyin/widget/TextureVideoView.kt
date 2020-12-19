package com.csdn.oyp.douyin.widget

import android.content.Context
import android.graphics.SurfaceTexture
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.*
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.MediaController
import android.widget.MediaController.MediaPlayerControl
import androidx.annotation.RequiresApi
import java.io.IOException

/**
 * copy自： https://www.jianshu.com/p/769b2fc1966c
 *
 * 1.查看VideoView源码，发现它是直接继承于SurfaceView，实现MediaPlayerControl接口的，所以我们类似的实现它。好，现在的代码长这样子。
 *
 *
 * 2.复制下VideoView通用逻辑。调整下面几个地方：
 * 2.1 将SurfaceView相关的地方删除；
 * 2.2 将subtitle相关的东西删除；
 * 2.3 在init中加入对TextureView的初始化，添加监听SurfaceTextureListener并实现；
 * 2.4 增加对尺寸变动的支持，之前surfaceView里面有setFixedSize方法来修改大小，所以我们也需要实现个；
 *
 * 作者：MD_凌霄
 * 链接：https://www.jianshu.com/p/769b2fc1966c
 * 来源：简书
 * 著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
 */
open class TextureVideoView : TextureView, MediaPlayerControl {
    // settable by the client
    private var mUri: Uri? = null
    private var mHeaders: Map<String, String>? = null

    // mCurrentState is a VideoView object's current state.
    // mTargetState is the state that a method caller intends to reach.
    // For instance, regardless the VideoView object's current state,
    // calling pause() intends to bring the object to a target state
    // of STATE_PAUSED.
    private var mCurrentState = STATE_IDLE
    private var mTargetState = STATE_IDLE

    // All the stuff we need for playing and showing a video
    private var mMediaPlayer: MediaPlayer? = null
    private var mAudioSession = 0
    private var mVideoWidth = 0
    private var mVideoHeight = 0
    private var mSurfaceWidth = 0
    private var mSurfaceHeight = 0
    private var mMediaController: MediaController? = null
    private var mOnCompletionListener: OnCompletionListener? = null
    private var mOnPreparedListener: OnPreparedListener? = null
    private var mCurrentBufferPercentage = 0
    private var mOnErrorListener: OnErrorListener? = null
    private var mOnInfoListener: OnInfoListener? = null
    private var mSeekWhenPrepared // recording the seek position while preparing
            = 0
    private var mCanPause = false
    private var mCanSeekBack = false
    private var mCanSeekForward = false
    private var mAudioManager: AudioManager? = null
    private var mContext: Context? = null
    private var mSurfaceTexture: SurfaceTexture? = null

    @JvmOverloads
    constructor(context: Context?, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init()
    }

    private fun init() {
        mVideoWidth = 0
        mVideoHeight = 0
        mContext = context
        mAudioManager = mContext?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        isFocusable = true
        isFocusableInTouchMode = true
        requestFocus()
        mCurrentState = STATE_IDLE
        mTargetState = STATE_IDLE
        surfaceTextureListener = mSurfaceTextureListener
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        //Log.i("@@@@", "onMeasure(" + MeasureSpec.toString(widthMeasureSpec) + ", "
        //        + MeasureSpec.toString(heightMeasureSpec) + ")");
        var width = getDefaultSize(mVideoWidth, widthMeasureSpec)
        var height = getDefaultSize(mVideoHeight, heightMeasureSpec)
        if (mVideoWidth > 0 && mVideoHeight > 0) {
            val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
            val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
            val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
            val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
            if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
                // the size is fixed
                width = widthSpecSize
                height = heightSpecSize

                // for compatibility, we adjust size based on aspect ratio
                if (mVideoWidth * height < width * mVideoHeight) {
                    //Log.i("@@@", "image too wide, correcting");
                    width = height * mVideoWidth / mVideoHeight
                } else if (mVideoWidth * height > width * mVideoHeight) {
                    //Log.i("@@@", "image too tall, correcting");
                    height = width * mVideoHeight / mVideoWidth
                }
            } else if (widthSpecMode == MeasureSpec.EXACTLY) {
                // only the width is fixed, adjust the height to match aspect ratio if possible
                width = widthSpecSize
                height = width * mVideoHeight / mVideoWidth
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize
                }
            } else if (heightSpecMode == MeasureSpec.EXACTLY) {
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize
                width = height * mVideoWidth / mVideoHeight
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize
                }
            } else {
                // neither the width nor the height are fixed, try to use actual video size
                width = mVideoWidth
                height = mVideoHeight
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // too tall, decrease both width and height
                    height = heightSpecSize
                    width = height * mVideoWidth / mVideoHeight
                }
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // too wide, decrease both width and height
                    width = widthSpecSize
                    height = width * mVideoHeight / mVideoWidth
                }
            }
        } else {
            // no size yet, just adopt the given spec sizes
        }
        setMeasuredDimension(width, height)
    }

    override fun getAccessibilityClassName(): CharSequence {
        return TextureVideoView::class.java.name
    }

    /**
     * Sets video path.
     *
     * @param path the path of the video.
     */
    fun setVideoPath(path: String?) {
        setVideoURI(Uri.parse(path))
    }

    /**
     * Sets video URI.
     *
     * @param uri the URI of the video.
     */
    fun setVideoURI(uri: Uri?) {
        setVideoURI(uri, null)
    }

    /**
     * Sets video URI using specific headers.
     *
     * @param uri     the URI of the video.
     * @param headers the headers for the URI request.
     * Note that the cross domain redirection is allowed by default, but that can be
     * changed with key/value pairs through the headers parameter with
     * "android-allow-cross-domain-redirect" as the key and "0" or "1" as the value
     * to disallow or allow cross domain redirection.
     */
    fun setVideoURI(uri: Uri?, headers: Map<String, String>?) {
        mUri = uri
        mHeaders = headers
        mSeekWhenPrepared = 0
        openVideo()
        requestLayout()
        invalidate()
    }

    fun stopPlayback() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.stop()
            mMediaPlayer!!.release()
            mMediaPlayer = null
            mCurrentState = STATE_IDLE
            mTargetState = STATE_IDLE
            mAudioManager!!.abandonAudioFocus(null)
        }
    }

    private fun openVideo() {
        if (mUri == null || null == mSurfaceTexture) {
            // not ready for playback just yet, will try again later
            return
        }
        // we shouldn't clear the target state, because somebody might have
        // called start() previously
        release(false)
        mAudioManager!!.requestAudioFocus(
            null,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
        try {
            mMediaPlayer = MediaPlayer()
            // TODO: create SubtitleController in MediaPlayer, but we need
            // a context for the subtitle renderers
            val context = context
            if (mAudioSession != 0) {
                mMediaPlayer!!.audioSessionId = mAudioSession
            } else {
                mAudioSession = mMediaPlayer!!.audioSessionId
            }
            mMediaPlayer!!.setOnPreparedListener(mPreparedListener)
            mMediaPlayer!!.setOnVideoSizeChangedListener(mSizeChangedListener)
            mMediaPlayer!!.setOnCompletionListener(mCompletionListener)
            mMediaPlayer!!.setOnErrorListener(mErrorListener)
            mMediaPlayer!!.setOnInfoListener(mInfoListener)
            mMediaPlayer!!.setOnBufferingUpdateListener(mBufferingUpdateListener)
            mCurrentBufferPercentage = 0
            mMediaPlayer!!.setDataSource(mContext!!, mUri!!, mHeaders)
            // modified
            mMediaPlayer!!.setSurface(Surface(mSurfaceTexture))
            mMediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mMediaPlayer!!.setScreenOnWhilePlaying(true)
            mMediaPlayer!!.prepareAsync()

            // we don't set the target state here either, but preserve the
            // target state that was there before.
            mCurrentState = STATE_PREPARING
            attachMediaController()
        } catch (ex: IOException) {
            Log.w(TAG, "Unable to open content: $mUri", ex)
            mCurrentState = STATE_ERROR
            mTargetState = STATE_ERROR
            mErrorListener.onError(mMediaPlayer, MEDIA_ERROR_UNKNOWN, 0)
            return
        } catch (ex: IllegalArgumentException) {
            Log.w(TAG, "Unable to open content: $mUri", ex)
            mCurrentState = STATE_ERROR
            mTargetState = STATE_ERROR
            mErrorListener.onError(mMediaPlayer, MEDIA_ERROR_UNKNOWN, 0)
            return
        } finally {
        }
    }

    fun setMediaController(controller: MediaController?) {
        if (mMediaController != null) {
            mMediaController!!.hide()
        }
        mMediaController = controller
        attachMediaController()
    }

    private fun attachMediaController() {
        if (mMediaPlayer != null && mMediaController != null) {
            mMediaController!!.setMediaPlayer(this)
            val anchorView = if (this.parent is View) this.parent as View else this
            mMediaController!!.setAnchorView(anchorView)
            mMediaController!!.isEnabled = isInPlaybackState
        }
    }

    var mSizeChangedListener =
        OnVideoSizeChangedListener { mp, width, height ->
            mVideoWidth = mp.videoWidth
            mVideoHeight = mp.videoHeight
            if (mVideoWidth != 0 && mVideoHeight != 0) {
                setFixedSize(mVideoWidth, mVideoHeight)
                requestLayout()
            }
        }
    var mPreparedListener = OnPreparedListener { mp ->
        mCurrentState = STATE_PREPARED

        // 有条件的可以实现，这里我就不实现啦
        // Get the capabilities of the player for this stream
        //            Metadata data = mp.getMetadata(MediaPlayer.METADATA_ALL,
        //                    MediaPlayer.BYPASS_METADATA_FILTER);
        //
        //            if (data != null) {
        //                mCanPause = !data.has(Metadata.PAUSE_AVAILABLE)
        //                        || data.getBoolean(Metadata.PAUSE_AVAILABLE);
        //                mCanSeekBack = !data.has(Metadata.SEEK_BACKWARD_AVAILABLE)
        //                        || data.getBoolean(Metadata.SEEK_BACKWARD_AVAILABLE);
        //                mCanSeekForward = !data.has(Metadata.SEEK_FORWARD_AVAILABLE)
        //                        || data.getBoolean(Metadata.SEEK_FORWARD_AVAILABLE);
        //            } else {
        //            }
        mCanSeekForward = true
        mCanSeekBack = mCanSeekForward
        mCanPause = mCanSeekBack
        if (mOnPreparedListener != null) {
            mOnPreparedListener!!.onPrepared(mMediaPlayer)
        }
        if (mMediaController != null) {
            mMediaController!!.isEnabled = true
        }
        mVideoWidth = mp.videoWidth
        mVideoHeight = mp.videoHeight
        val seekToPosition =
            mSeekWhenPrepared // mSeekWhenPrepared may be changed after seekTo() call
        if (seekToPosition != 0) {
            seekTo(seekToPosition)
        }
        if (mVideoWidth != 0 && mVideoHeight != 0) {
            //Log.i("@@@@", "video size: " + mVideoWidth +"/"+ mVideoHeight);
            setFixedSize(mVideoWidth, mVideoHeight)
            if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
                // We didn't actually change the size (it was already at the size
                // we need), so we won't get a "surface changed" callback, so
                // start the video here instead of in the callback.
                if (mTargetState == STATE_PLAYING) {
                    start()
                    if (mMediaController != null) {
                        mMediaController!!.show()
                    }
                } else if (!isPlaying &&
                    (seekToPosition != 0 || currentPosition > 0)
                ) {
                    if (mMediaController != null) {
                        // Show the media controls when we're paused into a video and make 'em stick.
                        mMediaController!!.show(0)
                    }
                }
            }
        } else {
            // We don't know the video size yet, but should start anyway.
            // The video size might be reported to us later.
            if (mTargetState == STATE_PLAYING) {
                start()
            }
        }
    }
    private val mCompletionListener = OnCompletionListener {
        mCurrentState = STATE_PLAYBACK_COMPLETED
        mTargetState = STATE_PLAYBACK_COMPLETED
        if (mMediaController != null) {
            mMediaController!!.hide()
        }
        if (mOnCompletionListener != null) {
            mOnCompletionListener!!.onCompletion(mMediaPlayer)
        }
    }
    private val mInfoListener =
        OnInfoListener { mp, arg1, arg2 ->
            if (mOnInfoListener != null) {
                mOnInfoListener!!.onInfo(mp, arg1, arg2)
            }
            true
        }
    private val mErrorListener =
        OnErrorListener { mp, framework_err, impl_err ->
            Log.d(TAG, "Error: $framework_err,$impl_err")
            mCurrentState = STATE_ERROR
            mTargetState = STATE_ERROR
            if (mMediaController != null) {
                mMediaController!!.hide()
            }

            /* If an error handler has been supplied, use it and finish. */if (mOnErrorListener != null) {
            if (mOnErrorListener!!.onError(mMediaPlayer, framework_err, impl_err)) {
                return@OnErrorListener true
            }
        }
            true
        }
    private val mBufferingUpdateListener =
        OnBufferingUpdateListener { mp, percent -> mCurrentBufferPercentage = percent }

    /**
     * Register a callback to be invoked when the media file
     * is loaded and ready to go.
     *
     * @param l The callback that will be run
     */
    fun setOnPreparedListener(l: OnPreparedListener?) {
        mOnPreparedListener = l
    }

    /**
     * Register a callback to be invoked when the end of a media file
     * has been reached during playback.
     *
     * @param l The callback that will be run
     */
    fun setOnCompletionListener(l: OnCompletionListener?) {
        mOnCompletionListener = l
    }

    /**
     * Register a callback to be invoked when an error occurs
     * during playback or setup.  If no listener is specified,
     * or if the listener returned false, VideoView will inform
     * the user of any errors.
     *
     * @param l The callback that will be run
     */
    fun setOnErrorListener(l: OnErrorListener?) {
        mOnErrorListener = l
    }

    /**
     * Register a callback to be invoked when an informational event
     * occurs during playback or setup.
     *
     * @param l The callback that will be run
     */
    fun setOnInfoListener(l: OnInfoListener?) {
        mOnInfoListener = l
    }

    /*
     * release the media player in any state
     */
    private fun release(cleartargetstate: Boolean) {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.reset()
            mMediaPlayer!!.release()
            mMediaPlayer = null
            mCurrentState = STATE_IDLE
            if (cleartargetstate) {
                mTargetState = STATE_IDLE
            }
            mAudioManager!!.abandonAudioFocus(null)
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (isInPlaybackState && mMediaController != null) {
            toggleMediaControlsVisiblity()
        }
        return false
    }

    override fun onTrackballEvent(ev: MotionEvent): Boolean {
        if (isInPlaybackState && mMediaController != null) {
            toggleMediaControlsVisiblity()
        }
        return false
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val isKeyCodeSupported =
            keyCode != KeyEvent.KEYCODE_BACK && keyCode != KeyEvent.KEYCODE_VOLUME_UP && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN && keyCode != KeyEvent.KEYCODE_VOLUME_MUTE && keyCode != KeyEvent.KEYCODE_MENU && keyCode != KeyEvent.KEYCODE_CALL && keyCode != KeyEvent.KEYCODE_ENDCALL
        if (isInPlaybackState && isKeyCodeSupported && mMediaController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK ||
                keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
            ) {
                if (mMediaPlayer!!.isPlaying) {
                    pause()
                    mMediaController!!.show()
                } else {
                    start()
                    mMediaController!!.hide()
                }
                return true
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!mMediaPlayer!!.isPlaying) {
                    start()
                    mMediaController!!.hide()
                }
                return true
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE
            ) {
                if (mMediaPlayer!!.isPlaying) {
                    pause()
                    mMediaController!!.show()
                }
                return true
            } else {
                toggleMediaControlsVisiblity()
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun toggleMediaControlsVisiblity() {
        if (mMediaController!!.isShowing) {
            mMediaController!!.hide()
        } else {
            mMediaController!!.show()
        }
    }

    override fun start() {
        if (isInPlaybackState) {
            mMediaPlayer!!.start()
            mCurrentState = STATE_PLAYING
        }
        mTargetState = STATE_PLAYING
    }

    override fun pause() {
        if (isInPlaybackState) {
            if (mMediaPlayer!!.isPlaying) {
                mMediaPlayer!!.pause()
                mCurrentState = STATE_PAUSED
            }
        }
        mTargetState = STATE_PAUSED
    }

    fun suspend() {
        release(false)
    }

    fun resume() {
        openVideo()
    }

    override fun getDuration(): Int {
        return if (isInPlaybackState) {
            mMediaPlayer!!.duration
        } else -1
    }

    override fun getCurrentPosition(): Int {
        return if (isInPlaybackState) {
            mMediaPlayer!!.currentPosition
        } else 0
    }

    override fun seekTo(msec: Int) {
        mSeekWhenPrepared = if (isInPlaybackState) {
            mMediaPlayer!!.seekTo(msec)
            0
        } else {
            msec
        }
    }

    override fun isPlaying(): Boolean {
        return isInPlaybackState && mMediaPlayer!!.isPlaying
    }

    override fun getBufferPercentage(): Int {
        return if (mMediaPlayer != null) {
            mCurrentBufferPercentage
        } else 0
    }

    private val isInPlaybackState: Boolean
        private get() = mMediaPlayer != null && mCurrentState != STATE_ERROR && mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING

    override fun canPause(): Boolean {
        return mCanPause
    }

    override fun canSeekBackward(): Boolean {
        return mCanSeekBack
    }

    override fun canSeekForward(): Boolean {
        return mCanSeekForward
    }

    override fun getAudioSessionId(): Int {
        if (mAudioSession == 0) {
            val foo = MediaPlayer()
            mAudioSession = foo.audioSessionId
            foo.release()
        }
        return mAudioSession
    }

    private fun setFixedSize(width: Int, height: Int) {
        mSurfaceWidth = mVideoWidth
        mSurfaceHeight = mVideoHeight
        if (mVideoWidth != width && mVideoHeight != height) {
            mVideoWidth = width
            mVideoHeight = height
            requestLayout()
        }
    }

    /**
     * surface回调，需要在这里获取mSurfaceTexture
     */
    private val mSurfaceTextureListener: SurfaceTextureListener = object : SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            mSurfaceTexture = surface
            openVideo()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            mSurfaceWidth = width
            mSurfaceHeight = height
            val isValidState = mTargetState == STATE_PLAYING
            val hasValidSize = mVideoWidth == width && mVideoHeight == height
            if (mMediaPlayer != null && isValidState && hasValidSize) {
                if (mSeekWhenPrepared != 0) {
                    seekTo(mSeekWhenPrepared)
                }
                start()
            }
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            mSurfaceTexture = null
            if (mMediaController != null) {
                mMediaController!!.hide()
            }
            release(true)
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }

    companion object {
        private const val TAG = "TextureVideoView"

        // all possible internal states
        private const val STATE_ERROR = -1
        private const val STATE_IDLE = 0
        private const val STATE_PREPARING = 1
        private const val STATE_PREPARED = 2
        private const val STATE_PLAYING = 3
        private const val STATE_PAUSED = 4
        private const val STATE_PLAYBACK_COMPLETED = 5
    }
}