//
// Created by 李金祥 on 2023/1/21.
//

#include "ImageEngine.h"
#include "Messages.h"
#include <memory>
#include "Log.h"
#include "Global.h"
#include "Local.h"

#define STB_IMAGE_IMPLEMENTATION

#include "stb_image.h"
#include "OpenGL.h"

ImageEngine::ImageEngine(jobject object)
    : sourceRender_(nullptr), pixelationRender_(nullptr), paintRender_(nullptr), screenRender_(nullptr), blendRender_(nullptr) {
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
  delete pixelationRender_;
  pixelationRender_ = nullptr;
  delete sourceRender_;
  sourceRender_ = nullptr;
  delete screenRender_;
  screenRender_ = nullptr;
  delete blendRender_;
  blendRender_ = nullptr;
}

void ImageEngine::onSurfaceCreate(jobject surface) {
  LOGI("enter %s", __func__);
  JNIEnv *env = JNIEnvironment::Current();
  if (env != nullptr) {
    nativeWindow_ = ANativeWindow_fromSurface(env, surface);
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

void ImageEngine::pushTouchBuffer(float *buffer, int length) {
  auto msg = new thread::Message();
  msg->what = PixelateMessage::kTouchEvent;
  msg->arg1 = length;
  msg->obj1 = buffer;
  handler_->sendMessage(msg);
}

void ImageEngine::translate(float scale, float pivotX, float pivotY, float angle, float translateX, float translateY) {
  auto msg = new thread::Message();
  msg->what = PixelateMessage::kTranslate;
  msg->arg3 = scale;
  msg->arg4 = angle;
  msg->arg5 = translateX;
  msg->arg6 = translateY;
  msg->arg7 = pivotX;
  msg->arg8 = pivotY;
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
    case PixelateMessage::kSetBrush: {
      auto image = reinterpret_cast<ImageInfo *>(msg->obj1);
      paintRender_->setBrush(image);
      delete image;
      break;
    }
    case PixelateMessage::kTouchEvent: {
      auto *buffer = reinterpret_cast<float *>(msg->obj1);
      int length = msg->arg1;
      paintRender_->processPushBufferInternal(buffer, length);
      delete[] buffer;
      break;
    }

    case PixelateMessage::kTranslate: {
      auto scale = msg->arg3;
      auto angle = msg->arg4;
      auto translateX = msg->arg5;
      auto translateY = msg->arg6;
      auto pivotX = msg->arg7;
      auto pivotY = msg->arg8;
      screenRender_->translate(scale, pivotX, pivotY, angle, translateX, translateY);
      paintRender_->setMatrix(screenRender_->getMatrix());
      paintRender_->translate(screenRender_->getMatrix()[0][0], 0.f, 0.f, 0.f, 0.f, 0.f);
      renderScreen(sourceRender_->getTexture(), sourceRender_->getTextureWidth(), sourceRender_->getTextureHeight());
      break;
    }

    case PixelateMessage::kSetMatrix: {
      auto *buffer = reinterpret_cast<float *>(msg->obj1);
      glm::mat4 matrix = glm::make_mat4(buffer);
      screenRender_->setMatrix(matrix);
      paintRender_->setMatrix(screenRender_->getMatrix());
      paintRender_->translate(screenRender_->getMatrix()[0][0], 0.f, 0.f, 0.f, 0.f, 0.f);
      renderScreen(sourceRender_->getTexture(), sourceRender_->getTextureWidth(), sourceRender_->getTextureHeight());
      delete[] buffer;
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
  if (pixelationRender_ == nullptr) {
    pixelationRender_ = new PixelationRender();
  }
  if (paintRender_ == nullptr) {
    paintRender_ = new PaintRender();
  }
  if (screenRender_ == nullptr) {
    screenRender_ = new ScreenRender();
  }
  if (blendRender_ == nullptr) {
    blendRender_ = new BlendRender(pixelator_.get());
  }
  callJavaEGLWindowCreate();
  LOGI("leave %s", __func__);
  return 0;
}

int ImageEngine::surfaceChangedInternal(int width, int height) {
  surfaceWidth_ = width;
  surfaceHeight_ = height;
  return 0;
}

int ImageEngine::insertImageInternal(const char *path, int rotate) {
  auto ret = decodeImage(imageTexture_, path, &imageWidth_, &imageHeight_);
  sourceRender_->draw(imageTexture_, imageWidth_, imageHeight_, rotate, surfaceWidth_, surfaceHeight_);
  pixelationRender_->draw(sourceRender_->getTexture(), sourceRender_->getTextureWidth(), sourceRender_->getTextureHeight());
  refreshFrameInternal();
  callJavaFrameAvaliable(screenRender_->getX(), screenRender_->getY(), screenRender_->getFitWidth(), screenRender_->getFitHeight());
  return 0;
}

int ImageEngine::refreshFrameInternal() {
  paintRender_->setMatrix(screenRender_->getMatrix());
  paintRender_->translate(screenRender_->getMatrix()[0][0], 0.f, 0.f, 0.f, 0.f, 0.f);
  paintRender_->draw(pixelationRender_->getTexture(), sourceRender_->getTextureWidth(), sourceRender_->getTextureHeight(), surfaceWidth_, surfaceHeight_);
  renderScreen(sourceRender_->getTexture(), sourceRender_->getTextureWidth(), sourceRender_->getTextureHeight());
  return 0;
}

void ImageEngine::saveInternal() {
  blendRender_->draw(sourceRender_->getTexture(), paintRender_->getTexture(), sourceRender_->getTextureWidth(), sourceRender_->getTextureHeight());
}

int ImageEngine::decodeImage(GLuint &texture, const char *path, int *width, int *height) {

  int channel = 0;
  auto data = stbi_load(path, width, height, &channel, STBI_rgb_alpha);
  if (*width == 0 || *height == 0 || data == nullptr) {
    LOGE("decode image error");
    return -1;
  }
  if (texture == 0) {
    glGenTextures(1, &texture);
  }
  if (*width % 2 != 0 || *height % 2 != 0) {
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
  }
  glActiveTexture(GL_TEXTURE0);
  glBindTexture(GL_TEXTURE_2D, texture);
  glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
  glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
  glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
  glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
  glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, *width, *height, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
  glBindTexture(GL_TEXTURE_2D, 0);
  delete[] data;
  return 0;
}

void ImageEngine::renderScreen(GLuint texture, int width, int height) {
  screenRender_->draw(texture, paintRender_->getTexture(), width, height, surfaceWidth_, surfaceHeight_);
  eglCore_->swapBuffers(renderSurface_);
}

void ImageEngine::renderScreenTexture(GLuint texture) {

  GL_CHECK(glUseProgram(program2_));
  auto positionLoction = glGetAttribLocation(program2_, "position");
  GL_CHECK(glEnableVertexAttribArray(positionLoction))
  GL_CHECK(glVertexAttribPointer(positionLoction, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat),
                                 DEFAULT_VERTEX_COORDINATE))
  auto textureLocation = glGetAttribLocation(program2_, "inputTextureCoordinate");
  GL_CHECK(glEnableVertexAttribArray(textureLocation));
  GL_CHECK(glVertexAttribPointer(textureLocation, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat),
                                 TEXTURE_COORDINATE_FLIP_UP_DOWN))
  GL_CHECK(glActiveTexture(GL_TEXTURE0));
  GL_CHECK(glBindTexture(GL_TEXTURE_2D, texture));
  auto inputTextureLocation = glGetUniformLocation(program2_, "inputImageTexture");
  GL_CHECK(glUniform1i(inputTextureLocation, 0))
  GL_CHECK(glDrawArrays(GL_TRIANGLE_STRIP, 0, 4))
  GL_CHECK(glDisableVertexAttribArray(positionLoction))
  GL_CHECK(glDisableVertexAttribArray(textureLocation))
  GL_CHECK(glBindTexture(GL_TEXTURE_2D, 0))
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

void ImageEngine::callJavaFrameAvaliable(int x, int y, int width, int height) {
  if (pixelator_.empty()) {
    return;
  }
  JNIEnv *env = JNIEnvironment::Current();
  if (env != nullptr) {
    Local<jclass> jclass = {env, env->GetObjectClass(pixelator_.get())};
    jmethodID frameAvaliableMethodId = env->GetMethodID(jclass.get(), "onFrameAvaliable", "(IIII)V");
    if (frameAvaliableMethodId != nullptr) {
      env->CallVoidMethod(pixelator_.get(), frameAvaliableMethodId, x, y, width, height);
    }
  }
}
