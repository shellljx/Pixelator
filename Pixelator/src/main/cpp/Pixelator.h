//
// Created by 李金祥 on 2023/1/21.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_PIXELATOR_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_PIXELATOR_H_

#include "Handler.h"
#include "HandlerThread.h"
#include "EGLCore.h"
#include <pthread.h>
#include <jni.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include "Global.h"
#include <detail/type_mat.hpp>
#include <detail/type_mat4x4.hpp>
#include <detail/type_vec2.hpp>
#include <detail/type_vec3.hpp>
#include <vector>
#include <GLES3/gl3.h>

#include "FrameBuffer.h"
#define TRIANGLE_NUM  43

using namespace glm;

class Pixelator : public thread::HandlerCallback {
 public:
  explicit Pixelator(jobject object);
  virtual ~Pixelator();

  void onSurfaceCreate(jobject surface);
  void onSurfaceChanged(int width, int height);
  void addImagePath(const char *path);
  void onTouchEvent(float x, float y);
  void refreshFrame();
  void handleMessage(thread::Message *msg) override;

 private:
  int createEGLInternal();
  int createEGLSurfaceInternal();
  int surfaceChangedInternal(int width, int height);
  int insertImageInternal(const char *path);
  int processTouchEventInternal(float x, float y);
  int refreshFrameInternal();
  int decodeImage(const char *path, int *width, int *height);
  GLuint renderPixelator(GLuint texture, int width, int height);
  void calculateMesh(vec2 pre, vec2 cur);
  GLuint rendImage(GLuint texture, int width, int height);
  void renderScreen(GLuint texture);
  void callJavaEGLContextCreate();
  void callJavaEGLWindowCreate();

 private:
  std::unique_ptr<thread::HandlerThread> handlerThread_ = nullptr;
  std::unique_ptr<thread::Handler> handler_ = nullptr;
  std::unique_ptr<EGLCore> eglCore_ = nullptr;
  ANativeWindow *nativeWindow_ = nullptr;
  EGLSurface renderSurface_ = EGL_NO_SURFACE;
  Global<jobject> pixelator_;

  GLuint imageTexture_ = 0;
  FrameBuffer *frameBuffer_ = nullptr;
  GLuint program1_ = 0;
  GLuint program2_ = 0;
  FrameBuffer *pixelateFrameBuffer_ = nullptr;
  GLuint program3_ = 0;
  int surfaceWidth_ = 0;
  int surfaceHeight_ = 0;
  int imageWidth_ = 0;
  int imageHeight_ = 0;
  std::vector<vec4> m_PointVector_;
  vec2 m_pVtxCoords[TRIANGLE_NUM * 3];
  vec2 m_pTexCoords[TRIANGLE_NUM * 3];
  vec2 currentPoint_;
  vec2 prePoint_;
};

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_PIXELATOR_H_
