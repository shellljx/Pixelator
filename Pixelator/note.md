1.边缘模糊问题与顶点构造问题
待优化内容:减少顶点数量和绘制次数，优化边缘渐变方案
最开始构造矩形和圆的顶点，但是边缘模糊不好实现，取纹理可以使用下面这句，感觉有优化空间
texture2D(inputImageTexture, vec2(gl_FragCoord.x,gl_FragCoord.y)/textureSize);
