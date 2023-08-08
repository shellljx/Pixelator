package com.gmail.shellljx.pixelate.panel

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.gmail.shellljx.pixelate.R
import com.gmail.shellljx.pixelate.extension.viewModels
import com.gmail.shellljx.pixelate.viewmodel.MediaViewModel
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.service.panel.AbsPanel
import com.gmail.shellljx.wrapper.service.panel.PanelConfig
import com.google.errorprone.annotations.Keep

/**
 * @Author: shell
 * @Email: shellljx@gmail.com
 * @Date: 2023/8/2
 * @Description:
 */
@Keep
class AlbumPanel(context: Context) : AbsPanel(context) {

    private var mAlbumListView: RecyclerView? = null
    private val mAdapter = AlbumAdapter()
    private val viewModel: MediaViewModel by viewModels()
    private val mBuckets = arrayListOf<MediaViewModel.MediaBucket>()

    override val tag: String
        get() = AlbumPanel::class.java.simpleName

    override val panelConfig: PanelConfig
        get() {
            return PanelConfig()
        }

    override fun onBindVEContainer(container: IContainer) {
        mContainer = container
    }

    override fun getLayoutId() = R.layout.layout_album_panel

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View?) {
        view ?: return
        mAlbumListView = view.findViewById(R.id.recycleview)
        mAlbumListView?.layoutManager = LinearLayoutManager(context)
        mAlbumListView?.adapter = mAdapter
        view.setOnClickListener {
            mContainer.getPanelService()?.hidePanel(mToken)
        }
        viewModel.bucketLiveData.observe(this) {
            mBuckets.clear()
            mBuckets.addAll(it)
            mAdapter.notifyDataSetChanged()
        }
    }

    override fun onDetach() {
        mContainer.getPanelService()?.showPanel(MediasPanel::class.java)
    }

    inner class AlbumAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val holder = AlbumHolder(LayoutInflater.from(context).inflate(R.layout.layout_album_item, parent, false))
            holder.itemView.setOnClickListener {
                val bucket = mBuckets[holder.adapterPosition]
                viewModel.bucket = bucket
                mContainer.getPanelService()?.hidePanel(mToken)
            }
            return holder
        }

        override fun getItemCount(): Int {
            return mBuckets.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val bucket = mBuckets[position]
            if (holder is AlbumHolder) {
                val requestBuilder = ImageRequestBuilder.newBuilderWithSource(Uri.parse("file://" + bucket.cover))
                requestBuilder.resizeOptions = ResizeOptions(100, 100)
                val controlBuilder = Fresco.newDraweeControllerBuilder()
                controlBuilder.imageRequest = requestBuilder.build()//设置图片请求
                controlBuilder.tapToRetryEnabled = true//设置是否允许加载失败时点击再次加载
                controlBuilder.oldController = holder.coverView.controller
                holder.coverView.controller = controlBuilder.build()
                holder.titleView.text = bucket.name
                holder.countView.text = bucket.count.toString()
            }
        }
    }

    inner class AlbumHolder(root: View) : RecyclerView.ViewHolder(root) {
        val coverView = itemView.findViewById<SimpleDraweeView>(R.id.iv_cover)
        val titleView = itemView.findViewById<TextView>(R.id.tv_title)
        val countView = itemView.findViewById<TextView>(R.id.tv_count)

    }
}