//
// Created by shell m1 on 2023/5/21.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_RENDER_BLENDRENDER_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_RENDER_BLENDRENDER_H_

#include <GLES3/gl3.h>
#include "glm/glm.hpp"
#include "glm/gtc/type_ptr.hpp"
#include "glm/gtc/matrix_transform.hpp"
#include "FrameBuffer.h"

class BlendRender {
 public:
  BlendRender();
  ~BlendRender();

  GLuint draw(GLuint textureId, GLuint maskTexture, int width, int height);

  void drawTexture(GLuint textureId, bool revert);

  GLuint getFrameBuffer();
  GLuint getTexture();
  int getWidth();
  int getHeight();

 private:
  FrameBuffer *frameBuffer_;
  GLuint program_ = 0;
  uint8_t *buffer_;
};

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_RENDER_BLENDRENDER_H_
