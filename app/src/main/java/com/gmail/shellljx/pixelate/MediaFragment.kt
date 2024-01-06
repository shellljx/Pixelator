package com.gmail.shellljx.pixelate

import android.animation.*
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.gmail.shellljx.pixelate.extension.dp
import com.gmail.shellljx.pixelate.viewmodel.MainViewModel
import com.gmail.shellljx.pixelate.viewmodel.MediaViewModel
import com.gmail.shellljx.wrapper.extension.activityViewModels

class MediaFragment : Fragment() {

    private lateinit var mMediaListView: RecyclerView
    private lateinit var mAlbumView: TextView
    private lateinit var mAlbumSelectView: View
    private lateinit var mCloseView: View
    private val mMediaAdapter = MediaAdapter()
    private val mMedias = arrayListOf<MediaViewModel.MediaResource>()
    private val mediaViewModel: MediaViewModel by activityViewModels()
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_medias, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mMediaListView = view.findViewById(R.id.rv_medias)
        mAlbumView = view.findViewById(R.id.tv_album)
        mAlbumSelectView = view.findViewById(R.id.album_select)
        mCloseView = view.findViewById(R.id.iv_close)
        mMediaListView.layoutManager = GridLayoutManager(context, 3)
        mMediaListView.adapter = mMediaAdapter
        mAlbumView.text = mediaViewModel.bucket?.name ?: context?.getString(R.string.album_all)

        mMediaListView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = mMediaListView.layoutManager as? GridLayoutManager
                layoutManager ?: return
                val totalItemCount: Int = layoutManager.itemCount
                val lastVisibleItemPosition: Int = layoutManager.findLastVisibleItemPosition()

                // 判断是否滑动到最后两行
                if (totalItemCount - lastVisibleItemPosition <= 4 && !mediaViewModel.loading && mediaViewModel.hasMore) {
                    mediaViewModel.fetchMedias(requireContext())
                }
            }
        })

        mediaViewModel.mediasLiveData.observe(viewLifecycleOwner) {
            val start = mMedias.size
            mMedias.addAll(it)
            mMediaAdapter.notifyItemRangeInserted(start, it.size)
        }
        mainViewModel.adStateLiveData.observe(viewLifecycleOwner) {
        }
        mAlbumSelectView.setOnClickListener {
        }
        mCloseView.setOnClickListener {
        }
        loadMedia()
    }

    private fun loadMedia() {
        mediaViewModel.fetchMedias(requireContext(), true)
        mediaViewModel.fetchBuckets(requireContext())
    }

    inner class MediaAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val mAnimators = hashMapOf<Int, AnimatorSet>()
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val holder = MediaHolder(LayoutInflater.from(context).inflate(R.layout.layout_media_item, parent, false))
            holder.itemView.setOnClickListener {
                val position = holder.adapterPosition
                val media = mMedias[position]
                mediaViewModel.selectedLiveData.value = media.path
            }
            return holder
        }

        override fun getItemCount(): Int {
            return mMedias.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val media = mMedias[position]
            if (holder is MediaHolder) {
                val size = (mMediaListView.width - 8.dp()) / 3
                val lp = holder.itemView.layoutParams
                lp?.height = size
                holder.itemView.layoutParams = lp
                val requestBuilder = ImageRequestBuilder.newBuilderWithSource(Uri.parse("file://" + media.path))
                requestBuilder.resizeOptions = ResizeOptions(size / 2, size / 2)
                val controlBuilder = Fresco.newDraweeControllerBuilder()
                controlBuilder.imageRequest = requestBuilder.build()//设置图片请求
                controlBuilder.tapToRetryEnabled = true//设置是否允许加载失败时点击再次加载
                controlBuilder.oldController = holder.coverView.controller
                holder.coverView.controller = controlBuilder.build()
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