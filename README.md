<p align="center">
  <img width="100" src="https://raw.githubusercontent.com/shellljx/Pixelator/main/app/src/main/res/mipmap-xxxhdpi/ic_launcher.webp" />
</p>

<h1 align="center">涂鸦笔 APP (Magic Brush)</h1>
<div align="center">

自定义涂鸦笔，马赛克涂鸦，图片涂鸦，智能抠图

<a href='https://play.google.com/store/apps/details?id=com.gmail.shellljx.pixelate'><img width="100" src='https://simplemobiletools.com/images/button-google-play.svg' alt='Get it on Google Play'/></a>
</div>

录屏 | 截图 | 截图 |
:-: | :-: | :-: 
<video src='https://github.com/shellljx/Pixelator/assets/7572018/727583de-e9ec-4413-92b9-1af16f918039' width=100/> | <img src="https://raw.githubusercontent.com/shellljx/Pixelator/main/img/screenshot2.webp" alt="图片2"> | <img src="https://raw.githubusercontent.com/shellljx/Pixelator/main/img/screenshot1.webp" alt="图片1">

## 运行

```
git clone git@github.com:shellljx/Pixelator.git

// Java 版本 17
Android Studio 打开项目
```

## 功能
- 图片渲染: 双指缩放，平移
- 涂鸦绘制: 单指绘制轨迹，边缘顺滑模糊，轨迹应用马赛克和图片特效
- 矩形框选
- 马赛克特效
- 橡皮擦
- AI 扣图: 使用的 https://pixian.ai/
- undo redo
- 小窗显示
- 导出
- 相册

## 项目结构
- Pixelator: c++ 编辑器模块 [详细文档]()
- Wrapper: 上层业务框架，抽象业务模块(Service) 面板(panel) 控件(Widget) [详细文档]()
- app: 应用业务模块 [详细文档]()
