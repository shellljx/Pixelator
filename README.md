# Pixelator

### 设置图片特效，创建了纹理再删除无法马上释放显存，内存爆涨

1. 需要确保每次opengl相关操作时都要正确的makecurrent到对应到eglsurface
2. 删除纹理只是标记了该纹理可以删除，调用了swapbuffers之后，所有绘制操作结束，交换了缓存，才会真正清理


1. 先打 release
2. 执行 ./gradlew app:uploadCrashlyticsSymbolFileRelease 上传符号表