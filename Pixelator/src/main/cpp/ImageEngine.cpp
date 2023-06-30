//
// Created by 李金祥 on 2023/1/21.
//

#include "ImageEngine.h"
#include "Messages.h"
#include <memory>
#include "Log.h"
#include "Global.h"
#include "Local.h"
#include "render/effect/ImageEffect.h"
#include "OpenGL.h"
#include "json/json.h"
#include "utils/ImageDecoder.h"

ImageEngine::ImageEngine(jobject object)
    : sourceRender_(nullptr),
      effectRender_(nullptr),
      paintRender_(nullptr),
      screenRender_(nullptr),
      blendRender_(nullptr),
      miniScreenRender_(nullptr) {
  std::string name("pixelator thread");
  handlerThread_ = std::unique_ptr<thread::HandlerThread>(thread::HandlerThread::Create(name));
  handler_ = std::make_unique<thread::Handler>(handlerThread_->getLooper(), this);
  pixelator_.reset(JNIEnvironment::Current(), object);
  auto *msg = new thread::Message();
  msg->what = EGLMessage::kCreateEGL;
  handler_->sendMessage(msg);
}

ImageEngine::~ImageEngine() {
  if (program2_ > 0) {
    glDeleteProgram(program2_);
    program2_ = 0;
  }
  if (imageTexture_ > 0) {
    glDeleteTextures(1, &imageTexture_);
    imageTexture_ = 0;
  }

  delete brushImage_;
  delete frameBuffer_;
  frameBuffer_ = nullptr;

  delete paintRender_;
  paintRender_ = nullptr;
  delete effectRender_;
  effectRender_ = nullptr;
  delete sourceRender_;
  sourceRender_ = nullptr;
  delete screenRender_;
  screenRender_ = nullptr;
  delete blendRender_;
  blendRender_ = nullptr;
  delete miniScreenRender_;
  miniScreenRender_ = nullptr;
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

void ImageEngine::onMiniSurfaceCreate(jobject surface) {
  JNIEnv *env = JNIEnvironment::Current();
  ANativeWindow *window = nullptr;
  if (env != nullptr) {
    window = ANativeWindow_fromSurface(env, surface);
  }
  if (window != nullptr) {
    auto msg = new thread::Message();
    msg->what = PixelateMessage::kCreateMiniSurface;
    msg->obj1 = window;
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

void ImageEngine::save() {
  auto msg = new thread::Message();
  msg->what = kSave;
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
    case PixelateMessage::kInsertImage: {
      auto path = reinterpret_cast<char *>(msg->obj1);
      auto rotate = msg->arg1;
      insertImageInternal(path, rotate);
      delete[] path;
      break;
    }
    case PixelateMessage::kSetEffect: {
      auto config = reinterpret_cast<char *>(msg->obj1);
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
      auto image = reinterpret_cast<ImageInfo *>(msg->obj1);
      paintRender_->setBrush(image);
      delete image;
      break;
    }
    case PixelateMessage::kSetDeeplabMask: {
      auto image = reinterpret_cast<ImageInfo *>(msg->obj1);
      paintRender_->setDeeplabMask(image);
      delete image;
      break;
    }
    case PixelateMessage::ksetDeeplabMaskMode: {
      auto mode = msg->arg1;
      paintRender_->setDeeplabMaskMode(mode);
      break;
    }
    case PixelateMessage::kSetPaintSize: {
      auto paintSize = msg->arg1;
      paintRender_->setPaintSize(paintSize);
      break;
    }
    case PixelateMessage::kTouchEvent: {
      auto *buffer = reinterpret_cast<float *>(msg->obj1);
      int length = msg->arg1;
      float cx = msg->arg3;
      float cy = msg->arg4;

      if (miniScreenRender_ != nullptr && length >= 2) {
        miniScreenRender_->tranlate(cx, cy);
      }
      //没有选择特效不让绘制轨迹
      if (effectRender_ == nullptr) {
        delete[] buffer;
        return;
      }
      paintRender_->processPushBufferInternal(buffer, length);
      for (int i = 0; i < length; ++i) {
        touchData_.push_back(buffer[i]);
      }
      paintRender_->draw(effectRender_->getTexture(),
                         sourceRender_->getTextureWidth(),
                         sourceRender_->getTextureHeight());
      delete[] buffer;
      break;
    }

    case PixelateMessage::kSetMatrix: {
      auto *buffer = reinterpret_cast<float *>(msg->obj1);
      glm::mat4 matrix = glm::make_mat4(buffer);
      screenRender_->setTransformMatrix(matrix);
      if (miniScreenRender_ != nullptr) {
        miniScreenRender_->setTransformMatrix(screenRender_->getModelMatrix());
      }
      refreshTransform();
      refreshFrameInternal();
      delete[] buffer;
      break;
    }
    case PixelateMessage::kUpdateViewPort: {
      auto offset = msg->arg1;
      screenRender_->updateViewPort(offset);
      screenRender_->initMatrix(surfaceWidth_,
                                surfaceHeight_,
                                sourceRender_->getTextureWidth(),
                                sourceRender_->getTextureHeight());
      refreshTransform();
      refreshFrameInternal();
      break;
    }
    case PixelateMessage::kRefreshFrame: {
      refreshFrameInternal();
      break;
    }
    case PixelateMessage::kSave: {
      saveInternal();
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
    case PixelateMessage::kCreateMiniSurface: {
      if (eglCore_ == nullptr) {
        return;
      }
      if (miniScreenRender_ == nullptr) {
        miniScreenRender_ = new MiniScreenRender(eglCore_.get());
      }
      auto window = reinterpret_cast<ANativeWindow *>(msg->obj1);
      miniScreenRender_->createEglSurface(eglCore_.get(), window);
      miniScreenRender_->setTransformMatrix(screenRender_->getModelMatrix());
      break;
    }
    case PixelateMessage::kMiniSurfaceChanged: {
      if (miniScreenRender_ != nullptr) {
        int width = msg->arg1;
        int height = msg->arg2;
        miniScreenRender_->surfaceChanged(width, height);
      }
      break;
    }
    case PixelateMessage::kMiniSurfaceDestroy: {
      if (miniScreenRender_ != nullptr) {
        miniScreenRender_->destroyEglSurface(eglCore_.get());
      }
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

  if (sourceRender_ == nullptr) {
    sourceRender_ = new SourceRender();
  }
  if (paintRender_ == nullptr) {
    paintRender_ = new PaintRender();
  }
  if (screenRender_ == nullptr) {
    screenRender_ = new ScreenRender();
  }
  if (blendRender_ == nullptr) {
    blendRender_ = new BlendRender();
  }
  callJavaEGLWindowCreate();
  refreshFrameInternal();
  LOGI("leave %s", __func__);
  return 0;
}

int ImageEngine::surfaceChangedInternal(int width, int height) {
  surfaceWidth_ = width;
  surfaceHeight_ = height;
  return 0;
}

int ImageEngine::insertImageInternal(const char *path, int rotate) {
  ImageDecoder imageDecoder;
  auto ret = imageDecoder.decodeImage(imageTexture_, path, &imageWidth_, &imageHeight_);
  sourceRender_->draw(imageTexture_,
                      imageWidth_,
                      imageHeight_,
                      rotate,
                      surfaceWidth_,
                      surfaceHeight_);
  if (effectRender_ != nullptr) {
    effectRender_->draw(sourceRender_->getTexture(),
                        sourceRender_->getTextureWidth(),
                        sourceRender_->getTextureHeight());
    paintRender_->draw(effectRender_->getTexture(),
                       sourceRender_->getTextureWidth(),
                       sourceRender_->getTextureHeight());
  }
  screenRender_->initMatrix(surfaceWidth_,
                            surfaceHeight_,
                            sourceRender_->getTextureWidth(),
                            sourceRender_->getTextureHeight());
  refreshTransform();
  refreshFrameInternal();
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
  int type = root["type"].asInt();
  auto config = root["config"];
  LOGI("parse effect json: type = %d ", type);
  switch (type) {
    case TypeMosaic: {
      delete effectRender_;
      effectRender_ = new PixelationRender();
      break;
    }
    case TypeImage: {
      delete effectRender_;
      effectRender_ = new ImageEffect();
      break;
    }
    default: {
      LOGE("%d effect type not avaliable", type);
      return;
    }
  }
  effectRender_->updateConfig(config);
  if (sourceRender_ != nullptr) {
    effectRender_->draw(sourceRender_->getTexture(),
                        sourceRender_->getTextureWidth(),
                        sourceRender_->getTextureHeight());
    paintRender_->draw(effectRender_->getTexture(),
                       sourceRender_->getTextureWidth(),
                       sourceRender_->getTextureHeight());
  }
}

void ImageEngine::updateEffectInternal(char *config) {

}

int ImageEngine::refreshFrameInternal() {
  if (renderSurface_ != EGL_NO_SURFACE) {
    eglCore_->makeCurrent(renderSurface_);
  }
  if (effectRender_ != nullptr) {
    blendRender_->draw(sourceRender_->getTexture(),
                       paintRender_->getTexture(),
                       sourceRender_->getTextureWidth(),
                       sourceRender_->getTextureHeight());
    screenRender_->draw(blendRender_->getTexture(),
                        sourceRender_->getTextureWidth(),
                        sourceRender_->getTextureHeight(),
                        surfaceWidth_,
                        surfaceHeight_);
  } else {
    screenRender_->draw(sourceRender_->getTexture(),
                        sourceRender_->getTextureWidth(),
                        sourceRender_->getTextureHeight(),
                        surfaceWidth_,
                        surfaceHeight_);
  }

  eglCore_->swapBuffers(renderSurface_);
  eglCore_->makeCurrent(EGL_NO_SURFACE);
  if (miniScreenRender_ != nullptr) {
    miniScreenRender_->draw(blendRender_->getTexture(),
                            eglCore_.get(),
                            blendRender_->getWidth(),
                            blendRender_->getHeight());
  }
  return 0;
}

void ImageEngine::refreshTransform() {
  paintRender_->setMatrix(screenRender_->getModelMatrix());
  paintRender_->translate(screenRender_->getModelMatrix()[0][0]);
  if (sourceRender_->getTextureHeight() <= 0) {
    return;
  }
  glm::vec4 lt = vec4(0.f, 0.f, 0.f, 1.f);
  glm::vec4
      rb = vec4(sourceRender_->getTextureWidth(), sourceRender_->getTextureHeight(), 0.f, 1.f);
  lt = screenRender_->getModelMatrix() * lt;
  rb = screenRender_->getModelMatrix() * rb;
  if (miniScreenRender_ != nullptr) {
    miniScreenRender_->setBounds(lt.x, lt.y, rb.x, rb.y);
  }
  callJavaFrameBoundsChanged(lt.x, lt.y, rb.x, rb.y);
}

void ImageEngine::saveInternal() {
  saveFrameBufferToBitmap(pixelator_.get(),
                          blendRender_->getFrameBuffer(),
                          blendRender_->getWidth(),
                          blendRender_->getHeight());
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

void ImageEngine::callJavaFrameBoundsChanged(float left, float top, float right, float bottom) {
  if (pixelator_.empty()) {
    return;
  }
  JNIEnv *env = JNIEnvironment::Current();
  if (env != nullptr) {
    Local<jclass> jclass = {env, env->GetObjectClass(pixelator_.get())};
    jmethodID
        frameAvaliableMethodId = env->GetMethodID(jclass.get(), "onFrameBoundsChanged", "(FFFF)V");
    if (frameAvaliableMethodId != nullptr) {
      env->CallVoidMethod(pixelator_.get(), frameAvaliableMethodId, left, top, right, bottom);
    }
  }
}

void ImageEngine::redoInternal() {
  if (!redoStack_.empty()) {
    auto data = redoStack_.back();
    redoStack_.pop_back();
    undoStack_.push_back(data);
    paintRender_->setMatrix(data.matrix);
    paintRender_->translate(data.matrix[0][0]);
    paintRender_->setPaintSize(data.paintSize);
    paintRender_->setPaintType(data.paintType);
    paintRender_->processPushBufferInternal(data.data, data.length);
    paintRender_->draw(effectRender_->getTexture(),
                       sourceRender_->getTextureWidth(),
                       sourceRender_->getTextureHeight());
    refreshFrameInternal();
  }
}

void ImageEngine::undoInternal() {
  if (!undoStack_.empty()) {
    auto data = undoStack_.back();
    undoStack_.pop_back();
    redoStack_.push_back(data);

    paintRender_->clear();
    for (auto &element : undoStack_) {
      paintRender_->processPushBufferInternal(element.data, element.length);
      paintRender_->setMatrix(element.matrix);
      paintRender_->setPaintSize(element.paintSize);
      paintRender_->translate(element.matrix[0][0]);
      paintRender_->setPaintType(element.paintType);
      paintRender_->draw(effectRender_->getTexture(),
                         sourceRender_->getTextureWidth(),
                         sourceRender_->getTextureHeight());
    }
    refreshFrameInternal();
  }
}
void ImageEngine::stopTouch() {
  if (touchData_.empty())return;
  auto *data = new float[touchData_.size()];
  memcpy(data, touchData_.data(), touchData_.size() * sizeof(float));
  undoStack_.push_back({data, (int) touchData_.size(), screenRender_->getModelMatrix(),
                        paintRender_->getPaintSize(), paintRender_->getPaintType()});
  touchData_.clear();

  for (auto &element : redoStack_) {
    delete[] element.data;
  }
  redoStack_.clear();
}
void ImageEngine::setPaintType(int paintType) {
  paintRender_->setPaintType(paintType);
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
