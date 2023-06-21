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
#include "render/effect/PixelationRender.h"
#include "render/PaintRender.h"
#include "render/ScreenRender.h"
#include "render/BlendRender.h"
#include "render/MiniScreenRender.h"
#include "render/DeeplabMaskRender.h"
#include "render/effect/BaseEffectRender.h"

using namespace glm;

class ImageEngine : public thread::HandlerCallback {
 public:
  explicit ImageEngine(jobject object);
  virtual ~ImageEngine();

  void onSurfaceCreate(jobject surface);
  void onSurfaceChanged(int width, int height);
  void onMiniSurfaceCreate(jobject surface);
  void onMiniSurfaceChanged(int width, int height);
  void onMiniSurfaceDestroy();
  void addImagePath(const char *path, int rotate);
  bool setBrush(jobject bitmap);
  void setPaintSize(int size);
  void pushTouchBuffer(float *buffer, int length, float cx, float cy);
  void setMatrix(float *matrix);
  void refreshFrame();
  void save();
  void redo();
  void undo();
  void handleMessage(thread::Message *msg) override;
  void stopTouch();
  void setPaintType(int paintType);
  void setDeeplabMask(jobject bitmap);
  void setDeeplabMaskMode(int mode);
  void setEffect(const char *config);
  void updateEffect(const char *config);
 private:
  int createEGLInternal();
  int createEGLSurfaceInternal();
  int surfaceChangedInternal(int width, int height);
  int insertImageInternal(const char *path, int rotate);
  void setEffectInternal(char *effect);
  void updateEffectInternal(char *config);
  int refreshFrameInternal();
  void refreshTransform();
  void saveInternal();
  void redoInternal();
  void undoInternal();
  void callJavaEGLContextCreate();
  void callJavaEGLWindowCreate();
  void callJavaFrameBoundsChanged(float left, float top, float right, float bottom);

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
  BaseEffectRender *effectRender_;
  PaintRender *paintRender_;
  ScreenRender *screenRender_;
  BlendRender *blendRender_;
  MiniScreenRender *miniScreenRender_;
  DeeplabMaskRender *deeplabMaskRender_;
  std::vector<LineData> undoStack_;
  std::vector<LineData> redoStack_;
  std::vector<float> touchData_;
};

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_PIXELATOR_H_
