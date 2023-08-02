package com.gmail.shellljx.pixelate.panel

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

    private lateinit var mContainer: IContainer
    private var mAlbumListView: RecyclerView? = null
    private val mAdapter = AlbumAdapter()

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

    override fun onViewCreated(view: View?) {
        view ?: return
        mAlbumListView = view.findViewById(R.id.recycleview)
        mAlbumListView?.layoutManager = LinearLayoutManager(context)
        mAlbumListView?.adapter = mAdapter
        view.setOnClickListener {
            mContainer.getPanelService()?.hidePanel(mToken)
        }
    }

    inner class AlbumAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val holder = AlbumHolder(LayoutInflater.from(context).inflate(R.layout.layout_album_item, parent, false))
            return holder
        }

        override fun getItemCount(): Int {
            return 10
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is AlbumHolder) {
                val requestBuilder = ImageRequestBuilder.newBuilderWithSource(Uri.parse("file://" + "/sdcard/DCIM/20211213174105209.png"))
                requestBuilder.resizeOptions = ResizeOptions(100, 100)
                val controlBuilder = Fresco.newDraweeControllerBuilder()
                controlBuilder.imageRequest = requestBuilder.build()//设置图片请求
                controlBuilder.tapToRetryEnabled = true//设置是否允许加载失败时点击再次加载
                controlBuilder.oldController = holder.coverView.controller
                holder.coverView.controller = controlBuilder.build()
                holder.titleView.text = "相册"
                holder.countView.text = "123"
            }
        }
    }

    inner class AlbumHolder(root: View) : RecyclerView.ViewHolder(root) {
        val coverView = itemView.findViewById<SimpleDraweeView>(R.id.iv_cover)
        val titleView = itemView.findViewById<TextView>(R.id.tv_title)
        val countView = itemView.findViewById<TextView>(R.id.tv_count)

    }
}