//
// Created by shell m1 on 2023/6/7.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_RENDER_DEEPLABMASKRENDER_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_RENDER_DEEPLABMASKRENDER_H_

#include "FrameBuffer.h"
#include <GLES3/gl3.h>
#include "ImageInfo.h"
#include <Global.h>

class DeeplabMaskRender {
 public:
  DeeplabMaskRender(jobject object);
  ~DeeplabMaskRender();

  void draw(GLuint textureId, int width, int height);

  GLuint getTexture();

  void setMaskMode(int mode);

  void download();

 private:
  void createBlendBitmap(int width, int height);
 private:
  FrameBuffer *frame_buffer_;
  GLuint program_ = 0;
  int maskMode_ = 0;
  uint8_t *buffer_;
  Global<jobject> pixelator_;
};

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_RENDER_DEEPLABMASKRENDER_H_
