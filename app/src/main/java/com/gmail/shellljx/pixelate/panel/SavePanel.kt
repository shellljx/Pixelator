package com.gmail.shellljx.pixelate.panel

import android.content.Context
import android.net.Uri
import android.view.View
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ResizeOptions
import com.facebook.imagepipeline.postprocessors.IterativeBoxBlurPostProcessor
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.gmail.shellljx.pixelate.R
import com.gmail.shellljx.wrapper.IContainer
import com.gmail.shellljx.wrapper.service.panel.AbsPanel
import com.gmail.shellljx.wrapper.service.panel.PanelConfig
import com.google.errorprone.annotations.Keep

/**
 * @Author: shell
 * @Email: shellljx@gmail.com
 * @Date: 2023/8/8
 * @Description:
 */
@Keep
class SavePanel(context: Context) : AbsPanel(context) {
    private lateinit var mBlurBgView: SimpleDraweeView
    private var mPath: String? = null
    override val tag: String
        get() = SavePanel::class.java.simpleName

    override val panelConfig: PanelConfig
        get() = PanelConfig()

    override fun onBindVEContainer(container: IContainer) {
    }

    override fun getLayoutId() = R.layout.panel_save_success_layout

    override fun onViewCreated(view: View?) {
        view ?: return
        mBlurBgView = view.findViewById(R.id.iv_blur_bg)
    }

    override fun onPayloadUpdate(any: Any) {
        mPath = any as? String
    }

    override fun onAttach() {
        val path = mPath ?: return
        val requestBuilder = ImageRequestBuilder.newBuilderWithSource(Uri.parse("file://$path"))
        requestBuilder.postprocessor = IterativeBoxBlurPostProcessor(20, 5)
        //requestBuilder.resizeOptions = ResizeOptions(size / 2, size / 2)
        val controlBuilder = Fresco.newDraweeControllerBuilder()
        controlBuilder.imageRequest = requestBuilder.build()//设置图片请求
        controlBuilder.tapToRetryEnabled = true//设置是否允许加载失败时点击再次加载
        controlBuilder.oldController = mBlurBgView.controller
        mBlurBgView.controller = controlBuilder.build()
    }
}