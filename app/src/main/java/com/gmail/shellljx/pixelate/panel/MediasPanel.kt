package com.gmail.shellljx.pixelate.panel

import android.animation.*
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.view.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.gmail.shellljx.imagePicker.MediaLoader
import com.gmail.shellljx.pixelate.R
import com.gmail.shellljx.pixelate.extension.dp
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.service.panel.AbsPanel
import com.gmail.shellljx.wrapper.service.panel.PanelConfig
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.google.errorprone.annotations.Keep

/**
 * @Author: shell
 * @Email: shellljx@gmail.com
 * @Date: 2023/8/1
 * @Description:
 */
@Keep
class MediasPanel(context: Context) : AbsPanel(context) {

    private lateinit var mContainer: IContainer
    private lateinit var mMediaContainer: View
    private lateinit var mMediaListView: RecyclerView
    private lateinit var mAlbumView: View
    private lateinit var mCloseView: View
    private val mMediaAdapter = MediaAdapter()
    private val mMediaBuckets = arrayListOf<MediaLoader.MediaBucket>()
    private var isMediaFadeIn = true

    override val tag: String
        get() = MediasPanel::class.java.simpleName

    override val panelConfig: PanelConfig
        get() {
            val config = PanelConfig()
            return config
        }

    override fun onBindVEContainer(container: IContainer) {
        mContainer = container
    }

    override fun getLayoutId(): Int {
        return R.layout.panel_medias_layout
    }

    override fun onViewCreated(view: View?) {
        view ?: return
        mMediaContainer = view.findViewById(R.id.container)
        mMediaListView = view.findViewById(R.id.rv_medias)
        mAlbumView = view.findViewById(R.id.tv_album)
        mCloseView = view.findViewById(R.id.iv_close)
        mMediaListView.layoutManager = GridLayoutManager(context, 3)
        mMediaListView.adapter = mMediaAdapter
        val behavior = BottomSheetBehavior.from(mMediaContainer)
        behavior.addBottomSheetCallback(object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == STATE_HIDDEN) {
                    mContainer.getPanelService()?.hidePanel(mToken)
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })
        mAlbumView.setOnClickListener {
            mContainer.getPanelService()?.showPanel(AlbumPanel::class.java)
        }
        view.setOnClickListener {
            mContainer.getPanelService()?.hidePanel(mToken)
        }
        mCloseView.setOnClickListener {
            mContainer.getPanelService()?.hidePanel(mToken)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onAttach() {
        isMediaFadeIn = true
        MediaLoader.load(context) {
            mMediaBuckets.clear()
            mMediaBuckets.addAll(it)
            mMediaAdapter.notifyDataSetChanged()
        }
    }

    override fun onDetach() {
        val behavior = BottomSheetBehavior.from(mMediaContainer)
        behavior.state = STATE_COLLAPSED
    }

    override fun onResume() {
        mMediaListView.scrollToPosition(0)
        mMediaContainer.alpha = 0f
        val alphaAnimator: Animator = ObjectAnimator.ofFloat(mMediaContainer, "alpha", 0f, 1f)
        alphaAnimator.duration = 400
        alphaAnimator.start()
    }

    inner class MediaAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val mAnimators = hashMapOf<Int, AnimatorSet>()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val holder = MediaHolder(LayoutInflater.from(context).inflate(R.layout.layout_media_item, parent, false))
            return holder
        }

        override fun getItemCount(): Int {
            if (mMediaBuckets.size == 0) return 0
            return mMediaBuckets.get(0).resources.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val media = mMediaBuckets.get(0).resources.get(position)
            if (holder is MediaHolder) {
                val size = (mMediaListView.width - 8.dp()) / 3
                val lp = holder.itemView.layoutParams
                lp?.height = size
                holder.itemView.layoutParams = lp
                val requestBuilder = ImageRequestBuilder.newBuilderWithSource(Uri.parse("file://" + media.path))
                requestBuilder.resizeOptions = ResizeOptions(size, size)
                val controlBuilder = Fresco.newDraweeControllerBuilder()
                controlBuilder.imageRequest = requestBuilder.build()//设置图片请求
                controlBuilder.tapToRetryEnabled = true//设置是否允许加载失败时点击再次加载
                controlBuilder.oldController = holder.coverView.controller
                holder.coverView.controller = controlBuilder.build()
            }
        }

        override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
            val position = holder.adapterPosition
            if (position >= 9) {
                isMediaFadeIn = false
            }
            if (!isMediaFadeIn) {
                return
            }
            holder.itemView.pivotX = holder.itemView.width * 0.3f
            holder.itemView.pivotY = holder.itemView.height * 0.3f
            holder.itemView.scaleX = 0f
            holder.itemView.scaleY = 0f
            holder.itemView.alpha = 0f
            val alphaAnimator: Animator = ObjectAnimator.ofFloat(holder.itemView, "alpha", 0f, 1f)
            val scaleXAnimator: Animator = ObjectAnimator.ofFloat(holder.itemView, "scaleX", 0f, 1f)
            val scaleYAnimator: Animator = ObjectAnimator.ofFloat(holder.itemView, "scaleY", 0f, 1f)
            val animatorSet = AnimatorSet()
            animatorSet.playTogether(alphaAnimator, scaleXAnimator, scaleYAnimator)
            animatorSet.duration = 200
            animatorSet.startDelay = 50 * getDelayFactor(position)
            animatorSet.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    removeAnimator(position)
                }
            })
            animatorSet.start()
        }

        private fun getDelayFactor(position: Int): Long {
            return when (position) {
                0 -> 0
                1, 3 -> 1
                2, 4, 6 -> 2
                5, 7 -> 3
                else -> 4
            }
        }

        override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
            removeAnimator(holder.adapterPosition)
        }

        private fun removeAnimator(position: Int) {
            mAnimators[position]?.let {
                it.cancel()
                it.removeAllListeners()
                mAnimators.remove(position)
            }
        }
    }

    inner class MediaHolder(root: View) : RecyclerView.ViewHolder(root) {
        val coverView = itemView.findViewById<SimpleDraweeView>(R.id.iv_cover)

    }
}