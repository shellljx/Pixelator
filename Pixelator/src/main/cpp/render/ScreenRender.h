//
// Created by 李金祥 on 2023/4/23.
//

#ifndef PIXELATE_SCREENRENDER_H
#define PIXELATE_SCREENRENDER_H
#include <GLES3/gl3.h>
#include "FrameBuffer.h"

class ScreenRender {
 public:
  ScreenRender();
  ~ScreenRender();

  /**
 * 绘制一个纹理到 framebuffer
 * @param textureId 纹理 id
 * @param width 纹理宽
 * @param height 纹理高
 * @return 绘制之后的纹理
 */
  GLuint draw(GLuint textureId,GLuint maskTexture, int width, int height, int screenWidth, int screenHeight);

  void drawTexture(GLuint textureId, int width, int height, int screenWidth, int screenHeight);

  GLuint getTexture();

 private:
  GLuint program_ = 0;
  FrameBuffer *frameBuffer_;
  float *vertexCoordinate_;
};

#endif //PIXELATE_SCREENRENDER_H
