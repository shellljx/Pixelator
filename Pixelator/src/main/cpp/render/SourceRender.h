//
// Created by shell m1 on 2023/5/10.
//

#ifndef PIXELATE_SOURCERENDER_H
#define PIXELATE_SOURCERENDER_H

#include <GLES3/gl3.h>
#include "FrameBuffer.h"

class SourceRender {
 public:
  SourceRender();

  ~SourceRender();

  /**
   * 绘制一个纹理到 framebuffer
   * @param textureId 纹理 id
   * @param width 纹理宽
   * @param height 纹理高
   * @return 绘制之后的纹理
   */
  GLuint draw(GLuint textureId, int width, int height, int rotate);

  GLuint getFrameBuffer();

  GLuint getTexture();

  int getTextureWidth();

  int getTextureHeight();

 private:
  GLuint program_ = 0;
  FrameBuffer *frameBuffer_ = nullptr;
};

#endif //PIXELATE_SOURCERENDER_H
