//
// Created by 李金祥 on 2023/1/21.
//

#include "ImageEngine.h"
#include "Messages.h"
#include "Log.h"
#include "Global.h"
#include "Local.h"
#include "json/json.h"
#include "utils/ImageDecoder.h"
#ifndef STBI_IMAGE
#define STBI_IMAGE
#define STB_IMAGE_WRITE_STATIC
#define STB_IMAGE_WRITE_IMPLEMENTATION
#include "stb_image_write.h"
#include "stb_image.h"
#endif

ImageEngine::ImageEngine(jobject object) : renderer(new Renderer(this)) {
  std::string name("ImageEngineThread");
  handlerThread_ = std::unique_ptr<thread::HandlerThread>(thread::HandlerThread::Create(name));
  handler_ = std::make_unique<thread::Handler>(handlerThread_->getLooper(), this);
  pixelator_.reset(JNIEnvironment::Current(), object);
  auto *msg = new thread::Message();
  msg->what = EGLMessage::kCreateEGL;
  handler_->sendMessage(msg);
}

ImageEngine::~ImageEngine() {
  delete renderer;
  renderer = nullptr;
}

void ImageEngine::onSurfaceCreate(jobject surface) {
  LOGI("enter %s", __func__);
  JNIEnv *env = JNIEnvironment::Current();
  //todo 赋值和销毁在一个线程
  if (env != nullptr) {
    nativeWindow_ = ANativeWindow_fromSurface(env, surface);
  }
  if (nativeWindow_ == nullptr) {
    return;
  }
  auto msg = new thread::Message();
  msg->what = EGLMessage::kCreateEGLSurface;
  handler_->sendMessage(msg);
  LOGI("leave %s", __func__);
}

void ImageEngine::onSurfaceChanged(int width, int height) {
  LOGI("enter %s", __func__);
  auto msg = new thread::Message();
  msg->what = EGLMessage::kSurfaceChanged;
  msg->arg1 = width;
  msg->arg2 = height;
  handler_->sendMessage(msg);
  LOGI("leave %s", __func__);
}

void ImageEngine::onSurfaceDestroy() {
  LOGI("enter %s", __func__);
  auto msg = new thread::Message();
  msg->what = EGLMessage::kDestroyEGLSurface;
  handler_->sendMessage(msg);
  LOGI("leave %s", __func__);
}

void ImageEngine::onMiniSurfaceCreate(jobject surface) {
  JNIEnv *env = JNIEnvironment::Current();
  if (env != nullptr) {
    miniScreenWindow_ = ANativeWindow_fromSurface(env, surface);
  }
  if (miniScreenWindow_ != nullptr) {
    auto msg = new thread::Message();
    msg->what = PixelateMessage::kCreateMiniSurface;
    handler_->sendMessage(msg);
  }
}

void ImageEngine::onMiniSurfaceChanged(int width, int height) {
  auto msg = new thread::Message();
  msg->what = PixelateMessage::kMiniSurfaceChanged;
  msg->arg1 = width;
  msg->arg2 = height;
  handler_->sendMessage(msg);
}

void ImageEngine::onMiniSurfaceDestroy() {
  auto msg = new thread::Message();
  msg->what = PixelateMessage::kMiniSurfaceDestroy;
  handler_->sendMessage(msg);
}

void ImageEngine::addImagePath(const char *path, int rotate) {
  if (path == nullptr) {
    LOGE("image path is null");
    return;
  }
  auto length = strlen(path) + 1;
  auto tempPath = new char[length];
  snprintf(tempPath, length, "%s%c", path, 0);

  auto msg = new thread::Message();
  msg->what = PixelateMessage::kInsertImage;
  msg->obj1 = tempPath;
  msg->arg1 = rotate;
  handler_->sendMessage(msg);
}

void ImageEngine::setEffect(const char *config) {
  if (config == nullptr) {
    return;
  }
  auto length = strlen(config) + 1;
  auto tempConfig = new char[length];
  snprintf(tempConfig, length, "%s%c", config, 0);
  auto msg = new thread::Message();
  msg->what = PixelateMessage::kSetEffect;
  msg->obj1 = tempConfig;
  handler_->sendMessage(msg);
}

void ImageEngine::updateEffect(const char *config) {
  if (config == nullptr) {
    return;
  }
  auto length = strlen(config) + 1;
  auto tempConfig = new char[length];
  snprintf(tempConfig, length, "%s%c", config, 0);
  auto msg = new thread::Message();
  msg->what = PixelateMessage::kUpdateEffect;
  msg->obj1 = tempConfig;
  handler_->sendMessage(msg);
}

bool ImageEngine::setBrush(jobject bitmap) {
  ImageInfo *image = nullptr;
  auto ret = createBitmapInfo(bitmap, &image);
  if (ret != 0 || image == nullptr) {
    LOGE("create bitmap info error %d", ret);
    return false;
  }
  auto msg = new thread::Message();
  msg->what = PixelateMessage::kSetBrush;
  msg->obj1 = image;
  handler_->sendMessage(msg);
  return true;
}

void ImageEngine::setPaintSize(int size) {
  auto msg = new thread::Message();
  msg->what = PixelateMessage::kSetPaintSize;
  msg->arg1 = size;
  handler_->sendMessage(msg);
}

void ImageEngine::setCanvasHide(bool hide) {
  auto msg = new thread::Message();
  msg->what = PixelateMessage::kSetCanvasHide;
  msg->flag1 = hide;
  handler_->sendMessage(msg);
}

void ImageEngine::pushTouchBuffer(float *buffer, int length, float cx, float cy) {
  auto msg = new thread::Message();
  msg->what = PixelateMessage::kTouchEvent;
  msg->arg1 = length;
  msg->arg3 = cx;
  msg->arg4 = cy;
  msg->obj1 = buffer;
  handler_->sendMessage(msg);
}

void ImageEngine::setMatrix(float *matrix) {
  auto msg = new thread::Message();
  msg->what = PixelateMessage::kSetMatrix;
  msg->obj1 = matrix;
  handler_->sendMessage(msg);
}

void ImageEngine::refreshFrame() {
  auto msg = new thread::Message();
  msg->what = PixelateMessage::kRefreshFrame;
  handler_->sendMessage(msg);
}

void ImageEngine::save(const char *path) {
  auto length = strlen(path) + 1;
  auto tempPath = new char[length];
  snprintf(tempPath, length, "%s%c", path, 0);
  auto msg = new thread::Message();
  msg->what = kSave;
  msg->obj1 = tempPath;
  handler_->sendMessage(msg);
}

void ImageEngine::redo() {
  auto msg = new thread::Message();
  msg->what = kRedo;
  handler_->sendMessage(msg);
}

void ImageEngine::undo() {
  auto msg = new thread::Message();
  msg->what = kUndo;
  handler_->sendMessage(msg);
}

void ImageEngine::handleMessage(thread::Message *msg) {
  int what = msg->what;
  switch (what) {
    case EGLMessage::kCreateEGL: {
      int ret = createEGLInternal();
      if (ret != 0) {
        LOGE("create egl error code %d", ret);
      }
      break;
    }
    case EGLMessage::kCreateEGLSurface: {
      createEGLSurfaceInternal();
      break;
    }
    case EGLMessage::kSurfaceChanged: {
      int width = msg->arg1;
      int height = msg->arg2;
      surfaceChangedInternal(width, height);
      break;
    }
    case EGLMessage::kDestroyEGLSurface: {
      surfaceDestroyInternal();
      break;
    }
    case EGLMessage::kDestroyEGL: {
      destroyEGLInternal();
      break;
    }
    case PixelateMessage::kCreateMiniSurface: {
      createMiniEGLSurfaceInternal();
      break;
    }
    case PixelateMessage::kMiniSurfaceChanged: {
      int width = msg->arg1;
      int height = msg->arg2;
      renderer->setMiniSurfaceChanged(width, height);
      break;
    }
    case PixelateMessage::kMiniSurfaceDestroy: {
      miniSurfaceDestroyInternal();
      break;
    }
    case PixelateMessage::kInsertImage: {
      auto path = reinterpret_cast<char *>(msg->obj1);
      auto rotate = msg->arg1;
      bindScreen();
      insertImageInternal(path, rotate);
      delete[] path;
      break;
    }
    case PixelateMessage::kSetEffect: {
      auto config = reinterpret_cast<char *>(msg->obj1);
      bindScreen();
      setEffectInternal(config);
      delete[] config;
      break;
    }
    case PixelateMessage::kUpdateEffect: {
      auto config = reinterpret_cast<char *>(msg->obj1);
      updateEffectInternal(config);
      delete[] config;
      break;
    }
    case PixelateMessage::kSetBrush: {
      //todo 创建image子线程
      auto image = reinterpret_cast<ImageInfo *>(msg->obj1);
      bindScreen();
      renderer->setBrushImage(image);
      delete image;
      break;
    }
    case PixelateMessage::kSetDeeplabMask: {
      //todo image创建在子线程
      auto image = reinterpret_cast<ImageInfo *>(msg->obj1);
      bindScreen();
      renderer->setMaskImage(image);
      delete image;
      break;
    }
    case PixelateMessage::ksetDeeplabMaskMode: {
      auto mode = msg->arg1;
      renderer->setMaskMode(mode);
      break;
    }
    case PixelateMessage::kSetPaintSize: {
      auto paintSize = msg->arg1;
      renderer->setPaintSize(paintSize);
      break;
    }
    case PixelateMessage::kSetCanvasHide: {
      auto hide = msg->flag1;
      renderer->setCanvasHide(hide);
      break;
    }
    case PixelateMessage::kStartTouch: {
      float x = msg->arg3;
      float y = msg->arg4;
      renderer->startTouch(x, y);
      break;
    }
    case PixelateMessage::kStopTouch: {
      stopTouchInternal();
      break;
    }
    case PixelateMessage::kTouchEvent: {
      auto *buffer = reinterpret_cast<float *>(msg->obj1);
      int length = msg->arg1;
      float cx = msg->arg3;
      float cy = msg->arg4;
      bindScreen();
      renderer->updateTouchBuffer(buffer, length, cx, cy);
      delete[] buffer;
      break;
    }

    case PixelateMessage::kSetMatrix: {
      auto *buffer = reinterpret_cast<float *>(msg->obj1);
      renderer->setTransformMatrix(buffer);
      delete[] buffer;
      break;
    }
    case PixelateMessage::kUpdateViewPort: {
      auto offset = msg->arg1;
      renderer->setBottomOffset(offset);
      break;
    }
    case PixelateMessage::kRefreshFrame: {
      renderer->drawScreen();
      break;
    }
    case PixelateMessage::kSave: {
      auto path = reinterpret_cast<char *>(msg->obj1);
      saveInternal(path);
      delete[] path;
      break;
    }
    case PixelateMessage::kUndo: {
      undoInternal();
      break;
    }
    case PixelateMessage::kRedo: {
      redoInternal();
      break;
    }
  }
}

int ImageEngine::createEGLInternal() {
  LOGI("enter %s", __func__);
  eglCore_ = std::make_unique<EGLCore>();
  int ret = eglCore_->init(nullptr);
  if (ret != 0) {
    eglCore_ = nullptr;
    return ret;
  }
  callJavaEGLContextCreate();
  LOGI("leave %s", __func__);
  return 0;
}

int ImageEngine::createEGLSurfaceInternal() {
  LOGI("enter %s", __func__);
  if (eglCore_ == nullptr) {
    LOGE("egl core is null");
    return -1;
  } else if (nativeWindow_ == nullptr) {
    LOGE("native window is null");
    return -1;
  }
  renderSurface_ = eglCore_->createWindowSurface(nativeWindow_);
  if (renderSurface_ == EGL_NO_SURFACE) {
    LOGE("create egl surface error");
    return -1;
  }
  eglCore_->makeCurrent(renderSurface_);
  callJavaEGLWindowCreate();
  renderer->drawScreen();
  LOGI("leave %s", __func__);
  return 0;
}

int ImageEngine::createMiniEGLSurfaceInternal() {
  LOGI("enter %s", __func__);
  if (eglCore_ == nullptr) {
    LOGE("egl core is null");
    return -1;
  } else if (miniScreenWindow_ == nullptr) {
    LOGE("mini native window is null");
    return -1;
  }
  miniSurface_ = eglCore_->createWindowSurface(miniScreenWindow_);
  if (miniSurface_ == EGL_NO_SURFACE) {
    LOGE("create egl mini surface error");
    return -1;
  }
  renderer->drawMiniScreen();
  return 0;
}

int ImageEngine::surfaceChangedInternal(int width, int height) {
  renderer->setSurfaceChanged(width, height);
  return 0;
}

void ImageEngine::surfaceDestroyInternal() {
  if (nativeWindow_ != nullptr) {
    ANativeWindow_release(nativeWindow_);
    nativeWindow_ = nullptr;
  }
  if (eglCore_ != nullptr) {
    if (renderSurface_ != EGL_NO_SURFACE) {
      eglCore_->makeCurrent(renderSurface_);
      eglCore_->releaseSurface(renderSurface_);
      eglCore_->makeCurrent(EGL_NO_SURFACE);
      renderSurface_ = EGL_NO_SURFACE;
    }
  }
}

void ImageEngine::miniSurfaceDestroyInternal() {
  if (miniScreenWindow_ != nullptr) {
    ANativeWindow_release(miniScreenWindow_);
    miniScreenWindow_ = nullptr;
  }
  if (eglCore_ != nullptr) {
    if (miniSurface_ != EGL_NO_SURFACE) {
      eglCore_->makeCurrent(miniSurface_);
      eglCore_->releaseSurface(miniSurface_);
      eglCore_->makeCurrent(EGL_NO_SURFACE);
      miniSurface_ = EGL_NO_SURFACE;
    }
  }
}

void ImageEngine::destroyEGLInternal() {
  if (eglCore_ != nullptr) {
    eglCore_->release();
    eglCore_ = nullptr;
  }
}

int ImageEngine::insertImageInternal(const char *path, int rotate) {
  ImageDecoder imageDecoder;
  GLuint imageTexture = 0;
  int width = 0;
  int height = 0;
  auto ret = imageDecoder.decodeImage(imageTexture, path, &width, &height);
  if (ret == 0 && width > 0 && height > 0) {
    renderer->setInputImage(imageTexture, width, height);
  }
  if (imageTexture > 0) {
    glDeleteTextures(1, &imageTexture);
  }
  return 0;
}

void ImageEngine::setEffectInternal(char *effect) {
  Json::CharReaderBuilder builder;
  JSONCPP_STRING err;
  Json::Value root;
  const std::unique_ptr<Json::CharReader> reader(builder.newCharReader());
  std::string effect_json(effect);
  if (!reader->parse(effect_json.c_str(),
                     effect_json.c_str() + effect_json.length(),
                     &root,
                     &err)) {
    LOGE("%s parse json error $s", effect_json.c_str(), err.c_str());
    return;
  }
  renderer->setEffect(root);
}

void ImageEngine::updateEffectInternal(char *config) {

}

void ImageEngine::bindScreen() {
  if (renderSurface_ != EGL_NO_SURFACE) {
    eglCore_->makeCurrent(renderSurface_);
  }
}

void ImageEngine::flushScreen() {
  eglCore_->swapBuffers(renderSurface_);
}

void ImageEngine::bindMiniScreen() {
  if (miniSurface_ != EGL_NO_SURFACE) {
    eglCore_->makeCurrent(miniSurface_);
  }
}

void ImageEngine::flushMiniScreen() {
  eglCore_->swapBuffers(miniSurface_);
}

void ImageEngine::onTransformChanged(float left, float top, float right, float bottom, bool reset) {
  callJavaFrameBoundsChanged(left, top, right, bottom, reset);
}

void ImageEngine::onInitBoundChanged(float left, float top, float right, float bottom) {
  callJavaInitBoundsChanged(left, top, right, bottom);
}

void ImageEngine::onUndoRedoChanged(int undoSize, int redoSize) {
  callJavaUndoRedoChanged(undoSize > 0, redoSize > 0);
}

void ImageEngine::saveFrameBuffer(FrameBuffer *frameBuffer, int width, int height) {
  saveFrameBufferToBitmap(pixelator_.get(), frameBuffer->getFrameBuffer(), width, height);
}

void ImageEngine::stopTouchInternal() {
  renderer->stopTouch();
}

void ImageEngine::saveInternal(const char *path) {
  auto frameBuffer = renderer->getBlendFrameBuffer();
  if (frameBuffer->getFrameBuffer() > 0) {
    int width = frameBuffer->getTextureWidth();
    int height = frameBuffer->getTextureHeight();
    auto buffer = new uint8_t[width * height * 4];
    glBindFramebuffer(GL_READ_FRAMEBUFFER, frameBuffer->getFrameBuffer());
    glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
    glBindFramebuffer(GL_READ_FRAMEBUFFER, GL_NONE);
    stbi_write_jpg(path, width, height, 4, buffer, 100);
    memset(buffer, 0, 4 * width * height);
    delete[] buffer;
    auto env = JNIEnvironment::Current();
    Local<jclass> sdkClass = {env, env->GetObjectClass(pixelator_.get())};
    jmethodID frameSavedMethodId = env->GetMethodID(sdkClass.get(), "onSaveSuccess",
                                                    "(Ljava/lang/String;)V");
    jstring pathStringJava = env->NewStringUTF(path);
    env->CallVoidMethod(pixelator_.get(), frameSavedMethodId, pathStringJava);
    env->DeleteLocalRef(pathStringJava);
  }
}

void ImageEngine::callJavaEGLContextCreate() {
  if (pixelator_.empty()) {
    return;
  }
  JNIEnv *env = JNIEnvironment::Current();
  if (env != nullptr) {
    Local<jclass> jclass = {env, env->GetObjectClass(pixelator_.get())};
    jmethodID eglContextCreateMethodId = env->GetMethodID(jclass.get(), "onEGLContextCreate",
                                                          "()V");
    if (eglContextCreateMethodId != nullptr) {
      env->CallVoidMethod(pixelator_.get(), eglContextCreateMethodId);
    }
  }
}

void ImageEngine::callJavaEGLWindowCreate() {
  if (pixelator_.empty()) {
    return;
  }
  JNIEnv *env = JNIEnvironment::Current();
  if (env != nullptr) {
    Local<jclass> jclass = {env, env->GetObjectClass(pixelator_.get())};
    jmethodID eglWindowCreateMethodId = env->GetMethodID(jclass.get(), "onEGLWindowCreate",
                                                         "()V");
    if (eglWindowCreateMethodId != nullptr) {
      env->CallVoidMethod(pixelator_.get(), eglWindowCreateMethodId);
    }
  }
}

void ImageEngine::callJavaFrameBoundsChanged(float left,
                                             float top,
                                             float right,
                                             float bottom,
                                             bool reset) {
  if (pixelator_.empty()) {
    return;
  }
  JNIEnv *env = JNIEnvironment::Current();
  if (env != nullptr) {
    Local<jclass> jclass = {env, env->GetObjectClass(pixelator_.get())};
    jmethodID boundChangedMethodId = env->GetMethodID(
        jclass.get(), "onFrameBoundsChanged", "(FFFFZ)V"
    );
    if (boundChangedMethodId != nullptr) {
      env->CallVoidMethod(pixelator_.get(),
                          boundChangedMethodId,
                          left,
                          top,
                          right,
                          bottom,
                          reset);
    }
  }
}

void ImageEngine::callJavaInitBoundsChanged(float left, float top, float right, float bottom) {
  if (pixelator_.empty()) {
    return;
  }
  JNIEnv *env = JNIEnvironment::Current();
  if (env != nullptr) {
    Local<jclass> jclass = {env, env->GetObjectClass(pixelator_.get())};
    jmethodID boundChangedMethodId = env->GetMethodID(
        jclass.get(), "onInitBoundsChanged", "(FFFF)V"
    );
    if (boundChangedMethodId != nullptr) {
      env->CallVoidMethod(pixelator_.get(),
                          boundChangedMethodId,
                          left,
                          top,
                          right,
                          bottom);
    }
  }
}

void ImageEngine::callJavaUndoRedoChanged(bool canUndo, bool canRedo) {
  if (pixelator_.empty()) {
    return;
  }
  JNIEnv *env = JNIEnvironment::Current();
  if (env != nullptr) {
    Local<jclass> sdkClass = {env, env->GetObjectClass(pixelator_.get())};
    jmethodID undoRedoChangedMethodId = env->GetMethodID(
        sdkClass.get(), "onUndoRedoChanged", "(ZZ)V"
    );
    if (undoRedoChangedMethodId != nullptr) {
      env->CallVoidMethod(pixelator_.get(), undoRedoChangedMethodId, canUndo, canRedo);
    }
  }
}

void ImageEngine::redoInternal() {
  bindScreen();
  renderer->redo();
}

void ImageEngine::undoInternal() {
  bindScreen();
  renderer->undo();
}

void ImageEngine::startTouch(float x, float y) {
  auto msg = new thread::Message();
  msg->what = PixelateMessage::kStartTouch;
  msg->arg3 = x;
  msg->arg4 = y;
  handler_->sendMessage(msg);
}

void ImageEngine::stopTouch() {
  auto msg = new thread::Message();
  msg->what = PixelateMessage::kStopTouch;
  handler_->sendMessage(msg);
}

void ImageEngine::setPaintMode(int paintMode) {
  renderer->setPaintMode(paintMode);
}

void ImageEngine::setPaintType(int type) {
  renderer->setPaintType(type);
}

void ImageEngine::setDeeplabMask(jobject bitmap) {
  ImageInfo *image = nullptr;
  auto ret = createBitmapInfo(bitmap, &image);
  if (ret != 0 || image == nullptr) {
    LOGE("create bitmap info error %d", ret);
    return;
  }
  auto msg = new thread::Message();
  msg->what = PixelateMessage::kSetDeeplabMask;
  msg->obj1 = image;
  handler_->sendMessage(msg);
}

void ImageEngine::setDeeplabMaskMode(int mode) {
  auto msg = new thread::Message();
  msg->what = PixelateMessage::ksetDeeplabMaskMode;
  msg->arg1 = mode;
  handler_->sendMessage(msg);
}

void ImageEngine::updateViewPort(int offset) {
  auto msg = new thread::Message();
  msg->what = PixelateMessage::kUpdateViewPort;
  msg->arg1 = offset;
  handler_->sendMessage(msg);
}

void ImageEngine::destroy() {
  auto msg = new thread::Message();
  msg->what = EGLMessage::kDestroyEGL;
  handler_->sendMessage(msg);
  handlerThread_->quitSafely();
}
