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
#include "BitmapUtils.h"
#include "FrameBuffer.h"
#include "render/SourceRender.h"
#include "render/PixelationRender.h"
#include "render/PaintRender.h"
#include "render/ScreenRender.h"

using namespace glm;

class ImageEngine : public thread::HandlerCallback {
 public:
  explicit ImageEngine(jobject object);
  virtual ~ImageEngine();

  void onSurfaceCreate(jobject surface);
  void onSurfaceChanged(int width, int height);
  void addImagePath(const char *path);
  bool setBrush(jobject bitmap);
  void pushTouchBuffer(float *buffer, int length);
  void translate(float scale, float angle, float translateX, float translateY);
  void refreshFrame();
  void handleMessage(thread::Message *msg) override;

 private:
  int createEGLInternal();
  int createEGLSurfaceInternal();
  int surfaceChangedInternal(int width, int height);
  int insertImageInternal(const char *path);
  int refreshFrameInternal();
  int decodeImage(GLuint &texture, const char *path, int *width, int *height);
  void renderScreen(GLuint texture, int width, int height);
  void renderScreenTexture(GLuint texture);
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
  GLuint program2_ = 0;
  int surfaceWidth_ = 0;
  int surfaceHeight_ = 0;
  int imageWidth_ = 0;
  int imageHeight_ = 0;
  //笔刷
  ImageInfo *brushImage_ = nullptr;
  SourceRender *sourceRender_;
  PixelationRender *pixelationRender_;
  PaintRender *paintRender_;
  ScreenRender *screenRender_;
};

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_PIXELATOR_H_
