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
#include <jni.h>
#include <Global.h>

class BlendRender {
 public:
  BlendRender(jobject object);
  ~BlendRender();

  GLuint draw(GLuint textureId, GLuint maskTexture, int width, int height);

  void drawTexture(GLuint textureId, int width, int height);

  void save();
  GLuint getTexture();
  int getWidth();
  int getHeight();

 private:
  void createBlendBitmap(int width, int height);
 private:
  FrameBuffer *frameBuffer_;
  GLuint program_ = 0;
  uint8_t *buffer_;
  Global<jobject> pixelator_;
};

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_RENDER_BLENDRENDER_H_
