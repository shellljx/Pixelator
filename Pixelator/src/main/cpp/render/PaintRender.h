//
// Created by 李金祥 on 2023/4/24.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_RENDER_PAINTRENDER_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_RENDER_PAINTRENDER_H_

#include <GLES3/gl3.h>
#include "FrameBuffer.h"
#include "ImageInfo.h"

class PaintRender {
 public:
  PaintRender();
  ~PaintRender();

  void setBrush(const ImageInfo *image);

  int processPushBufferInternal(float *buffer, int length);

  /**
 * 绘制一个纹理到 framebuffer
 * @param textureId 纹理 id
 * @param width 纹理宽
 * @param height 纹理高
 * @return 绘制之后的纹理
 */
  GLuint draw(GLuint textureId, int width, int height, int screenWidth, int screenHeight);

  GLuint getTexture();

 private:
  FrameBuffer *frame_buffer_;
  GLuint brushTexture_ = 0;
  GLuint program_ = 0;
  GLuint vao_ = 0;
  GLuint pointsVbo_ = 0;
  int points = 0;
};

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_RENDER_PAINTRENDER_H_
