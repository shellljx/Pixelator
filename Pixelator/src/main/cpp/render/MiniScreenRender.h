//
// Created by shell m1 on 2023/5/26.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_RENDER_MINISCREENRENDER_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_RENDER_MINISCREENRENDER_H_

#include "EGLCore.h"
#include "Program.h"
#include <memory>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <GLES3/gl3.h>
#include "glm/glm.hpp"
#include "glm/gtc/type_ptr.hpp"
#include "glm/gtc/matrix_transform.hpp"

class MiniScreenRender {
 public:
  MiniScreenRender(EGLCore *eglCore);
  ~MiniScreenRender();
  void createEglSurface(EGLCore *eglCore, ANativeWindow *window);
  void surfaceChanged(int width, int height);
  void draw(GLuint texture, EGLCore *eglCore, int textureWidth, int textureHeight);
  void setTransformMatrix(glm::mat4 matrix) {
    transformMatrix_ = matrix;
  }
  void destroyEglSurface(EGLCore *eglCore);
  void tranlate(float x, float y);
  void setBounds(float left, float top, float right, float bottom);
 private:
  ANativeWindow *nativeWindow_ = nullptr;
  EGLSurface eglSurface_ = EGL_NO_SURFACE;
  GLuint program_ = 0;
  glm::mat4 transformMatrix_ = glm::mat4(1);
  int surfaceWidth_ = 0;
  int surfaceHeight_ = 0;
  float x_ = 0.f;
  float y_ = 0.f;
  float left_ = 0.f;
  float top_ = 0.f;
  float right_ = 0.f;
  float bottom_ = 0.f;
};

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_RENDER_MINISCREENRENDER_H_
