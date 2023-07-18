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
#include <vector>
#include <GLES3/gl3.h>
#include "BitmapUtils.h"
#include "FrameBuffer.h"
#include "render/Renderer.h"

using namespace glm;

class ImageEngine : public thread::HandlerCallback, RenderCallback {
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
  void setPaintMode(int paintMode);
  void setDeeplabMask(jobject bitmap);
  void setDeeplabMaskMode(int mode);
  void setEffect(const char *config);
  void updateEffect(const char *config);
  void updateViewPort(int offset);
  void onSurfaceDestroy();
  void destroy();
  void startTouch(float x, float y);
  void setPaintType(int type);
 private:
  void bindScreen() override;
  void flushScreen() override;
  void bindMiniScreen() override;
  void flushMiniScreen() override;
  void onTransformChanged(float left, float top, float right, float bottom, bool reset) override;
  void onInitBoundChanged(float left, float top, float right, float bottom) override;
  void onGenerateDrawOp() override;
  void saveFrameBuffer(FrameBuffer *frameBuffer, int width, int height) override;
  int createEGLInternal();
  int createEGLSurfaceInternal();
  int createMiniEGLSurfaceInternal();
  int surfaceChangedInternal(int width, int height);
  void surfaceDestroyInternal();
  void miniSurfaceDestroyInternal();
  void destroyEGLInternal();
  int insertImageInternal(const char *path, int rotate);
  void setEffectInternal(char *effect);
  void updateEffectInternal(char *config);
  void stopTouchInternal();
  void saveInternal();
  void redoInternal();
  void undoInternal();
  void callJavaEGLContextCreate();
  void callJavaEGLWindowCreate();
  void callJavaFrameBoundsChanged(float left, float top, float right, float bottom, bool reset);
  void callJavaInitBoundsChanged(float left, float top, float right, float bottom);
  void callJavaUndoRedoChanged();
 private:
  std::unique_ptr<thread::HandlerThread> handlerThread_ = nullptr;
  std::unique_ptr<thread::Handler> handler_ = nullptr;
  std::unique_ptr<EGLCore> eglCore_ = nullptr;
  ANativeWindow *nativeWindow_ = nullptr;
  EGLSurface renderSurface_ = EGL_NO_SURFACE;
  ANativeWindow *miniScreenWindow_ = nullptr;
  EGLSurface miniSurface_ = EGL_NO_SURFACE;
  Global<jobject> pixelator_;
  Renderer *renderer;
};

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_PIXELATOR_H_
