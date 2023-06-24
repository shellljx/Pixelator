//
// Created by 李金祥 on 2022/11/12.
//

#ifndef CAMERARECORD_FRAMEBUFFER_H
#define CAMERARECORD_FRAMEBUFFER_H

#include "Program.h"

class FrameBuffer {
 public:
  FrameBuffer();

  ~FrameBuffer();

  /**
   * 创建 framebuffer 或者更新 framebuffer的尺寸
   * @param width
   * @param height
   */
  void createFrameBuffer(int width, int height);

  GLuint getTexture();

  int getTextureWidth();

  int getTextureHeight();

  GLuint getFrameBuffer();

  void deleteFrameBuffer();

 private:
  GLuint frameBufferId_ = 0;
  GLuint frameTextureId_ = 0;
  int width_ = 0;
  int height_ = 0;
};

#endif //CAMERARECORD_FRAMEBUFFER_H
