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

#include "FrameBuffer.h"

class Pixelator : public thread::HandlerCallback {
 public:
  explicit Pixelator(jobject object);
  virtual ~Pixelator();

  void onSurfaceCreate(jobject surface);
  void onSurfaceChanged(int width, int height);
  void addImagePath(const char *path);
  void handleMessage(thread::Message *msg) override;

 private:
  int createEGLInternal();
  int createEGLSurfaceInternal();
  int surfaceChangedInternal(int width, int height);
  int insertImageInternal(const char *path);
  int decodeImage(const char *path, int *width, int *height);
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
  int surfaceWidth_ = 0;
  int surfaceHeight_ = 0;
};

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_PIXELATOR_H_
