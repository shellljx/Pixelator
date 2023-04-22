//
// Created by 李金祥 on 2023/1/21.
//

#include "Pixelator.h"
#include "Messages.h"
#include <memory>
#include "Log.h"
#include "Global.h"
#include "Local.h"

#define STB_IMAGE_IMPLEMENTATION

#include "stb_image.h"
#include "OpenGL.h"

Pixelator::Pixelator(jobject object) {
  std::string name("pixelator thread");
  handlerThread_ = std::unique_ptr<thread::HandlerThread>(thread::HandlerThread::Create(name));
  handler_ = std::make_unique<thread::Handler>(handlerThread_->getLooper(), this);
  pixelator_.reset(JNIEnvironment::Current(), object);
  auto *msg = new thread::Message();
  msg->what = EGLMessage::kCreateEGL;
  handler_->sendMessage(msg);
}

Pixelator::~Pixelator() {
  if (program1_ > 0) {
    glDeleteProgram(program1_);
    program1_ = 0;
  }
  if (program2_ > 0) {
    glDeleteProgram(program2_);
    program2_ = 0;
  }
  if (program3_ > 0) {
    glDeleteProgram(program3_);
    program3_ = 0;
  }
  if (imageTexture_ > 0) {
    glDeleteTextures(1, &imageTexture_);
    imageTexture_ = 0;
  }
  if (brushTexture_ > 0) {
    glDeleteTextures(1, &brushTexture_);
    brushTexture_ = 0;
  }
  if (pointsVbo_ > 0) {
    glDeleteBuffers(1, &pointsVbo_);
    pointsVbo_ = 0;
  }
  if (vao_ > 0) {
    glDeleteVertexArrays(1, &vao_);
    vao_ = 0;
  }
  delete brushImage_;
  delete frameBuffer_;
  frameBuffer_ = nullptr;
  delete pixelateFrameBuffer_;
  pixelateFrameBuffer_ = nullptr;
}

void Pixelator::onSurfaceCreate(jobject surface) {
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

void Pixelator::onSurfaceChanged(int width, int height) {
  LOGI("enter %s", __func__);
  auto msg = new thread::Message();
  msg->what = EGLMessage::kSurfaceChanged;
  msg->arg1 = width;
  msg->arg2 = height;
  handler_->sendMessage(msg);
  LOGI("leave %s", __func__);
}

void Pixelator::addImagePath(const char *path) {
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
  handler_->sendMessage(msg);
}

bool Pixelator::setBrush(jobject bitmap) {
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

void Pixelator::pushTouchBuffer(float *buffer, int length) {
  auto msg = new thread::Message();
  msg->what = PixelateMessage::kTouchEvent;
  msg->arg1 = length;
  msg->obj1 = buffer;
  handler_->sendMessage(msg);
}

void Pixelator::refreshFrame() {
  auto msg = new thread::Message();
  msg->what = PixelateMessage::kRefreshFrame;
  handler_->sendMessage(msg);
}

void Pixelator::handleMessage(thread::Message *msg) {
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
      insertImageInternal(path);
      delete[] path;
      break;
    }
    case PixelateMessage::kSetBrush: {
      auto image = reinterpret_cast<ImageInfo *>(msg->obj1);
      setBrushInternal(image);
      break;
    }
    case PixelateMessage::kTouchEvent: {
      auto *buffer = reinterpret_cast<float *>(msg->obj1);
      int length = msg->arg1;
      processPushBufferInternal(buffer, length);
      delete[] buffer;
      break;
    }

    case PixelateMessage::kRefreshFrame: {
      refreshFrameInternal();
      break;
    }
  }
}

int Pixelator::createEGLInternal() {
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

int Pixelator::createEGLSurfaceInternal() {
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
  LOGI("leave %s", __func__);
  return 0;
}

int Pixelator::surfaceChangedInternal(int width, int height) {
  surfaceWidth_ = width;
  surfaceHeight_ = height;
  return 0;
}

int Pixelator::insertImageInternal(const char *path) {
  auto ret = decodeImage(imageTexture_, path, &imageWidth_, &imageHeight_);
  int imageWith;
  int imageHeight;
  decodeImage(imageTextureOverlay_, "/sdcard/aftereffect/ae/tt/resource/assets/a1.png", &imageWith, &imageHeight);
  auto frameTexture = rendImage(imageTexture_, imageWidth_, imageHeight_);
  auto pixelaTexture = renderPixelator(frameTexture, imageWidth_, imageHeight_);
  renderScreen(frameTexture);
  return 0;
}

void Pixelator::setBrushInternal(ImageInfo *image) {
  if (brushImage_ != nullptr || image == nullptr) {
    return;
  }
  if (image->pixels_ != nullptr) {
    if (brushTexture_ == 0) {
      glGenTextures(1, &brushTexture_);
      glBindTexture(GL_TEXTURE_2D, brushTexture_);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
      glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

      glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, image->width_, image->height_, 0,
                   GL_RGBA, GL_UNSIGNED_BYTE,
                   image->pixels_);
    } else {
      glBindTexture(GL_TEXTURE_2D, brushTexture_);
      glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, image->width_, image->height_,
                      GL_RGBA, GL_UNSIGNED_BYTE, image->pixels_);
    }
    glBindTexture(GL_TEXTURE_2D, 0);
    brushImage_ = image;
  } else {
    delete image;
  }
}

bool rest = true;
int Pixelator::processPushBufferInternal(float *buffer, int length) {
  glBindVertexArray(vao_);
  points = length / 2;
  if (pointsVbo_ == 0) {
    glGenBuffers(1, &pointsVbo_);
    glBindBuffer(GL_ARRAY_BUFFER, pointsVbo_);
    glBufferData(GL_ARRAY_BUFFER, length * sizeof(float), buffer, GL_DYNAMIC_DRAW);
    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(float), nullptr);
    glEnableVertexAttribArray(0);
    glBindVertexArray(0);
  } else {
    glBindBuffer(GL_ARRAY_BUFFER, pointsVbo_);
    glBufferData(GL_ARRAY_BUFFER, length * sizeof(float), buffer, GL_DYNAMIC_DRAW);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
  }

//  prePoint_ = currentPoint_;
//  if (rest) {
//    currentPoint_ = vec2(x / surfaceWidth_, y / surfaceHeight_);
//    rest = false;
//    return 0;
//  }
//  currentPoint_ = vec2(x * 1.0f / surfaceWidth_, y * 1.0f / surfaceHeight_);
//  if (prePoint_ == currentPoint_) {
//    return 0;
//  }
//  m_PointVector_.emplace_back(currentPoint_.x * 2.0 - 1.0);
//  m_PointVector_.emplace_back(currentPoint_.y * 2 - 1.0);
  return 0;
}

int Pixelator::refreshFrameInternal() {
  auto pixelaTexture = renderPixelator(imageTextureOverlay_, imageWidth_, imageHeight_);
  renderScreen(pixelaTexture);
  return 0;
}

int Pixelator::decodeImage(GLuint &texture, const char *path, int *width, int *height) {

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
  glActiveTexture(0);
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

GLuint Pixelator::rendImage(GLuint texture, int width, int height) {
  if (frameBuffer_ == nullptr) {
    frameBuffer_ = new FrameBuffer();
    frameBuffer_->createFrameBuffer(width, height);
  }
  if (program1_ == 0) {
    program1_ = Program::CreateProgram(DEFAULT_VERTEX_SHADER, DEFAULT_FRAGMENT_SHADER);
  }
  int outputWidth = width;
  int outputHeight = height;

  GL_CHECK(glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer_->getFrameBuffer()));
  if (width % 2 != 0 || height % 2 != 0) {
    GL_CHECK(glPixelStorei(GL_UNPACK_ALIGNMENT, 1));
  }

  GL_CHECK(glViewport(0, 0, outputWidth, outputHeight));
  GL_CHECK(glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT));
  GL_CHECK(glUseProgram(program1_));
  auto positionLoction = glGetAttribLocation(program1_, "position");
  GL_CHECK(glEnableVertexAttribArray(positionLoction))
  GL_CHECK(glVertexAttribPointer(positionLoction, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat),
                                 DEFAULT_VERTEX_COORDINATE))
  auto textureLocation = glGetAttribLocation(program1_, "inputTextureCoordinate");
  GL_CHECK(glEnableVertexAttribArray(textureLocation));
  GL_CHECK(glVertexAttribPointer(textureLocation, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat),
                                 TEXTURE_COORDINATE_FLIP_UP_DOWN))
  GL_CHECK(glActiveTexture(0));
  GL_CHECK(glBindTexture(GL_TEXTURE_2D, imageTexture_));
  auto inputTextureLocation = glGetUniformLocation(program1_, "inputImageTexture");
  GL_CHECK(glUniform1i(inputTextureLocation, 0))
  GL_CHECK(glDrawArrays(GL_TRIANGLE_STRIP, 0, 4))
  GL_CHECK(glDisableVertexAttribArray(positionLoction))
  GL_CHECK(glDisableVertexAttribArray(textureLocation))
  GL_CHECK(glBindTexture(GL_TEXTURE_2D, 0))

  GL_CHECK(glBindFramebuffer(GL_FRAMEBUFFER, 0));
  return frameBuffer_->getTexture();
}

GLuint Pixelator::renderPixelator(GLuint texture, int width, int height) {
  if (pixelateFrameBuffer_ == nullptr) {
    pixelateFrameBuffer_ = new FrameBuffer();
    pixelateFrameBuffer_->createFrameBuffer(width, height);
  }
  if (program3_ == 0) {
    program3_ = Program::CreateProgram(PIXELATE_VERTEX_SHADER, PIXELATE_RECT_FRAGMENT_SHADER);
  }

  if (vao_ == 0) {
    glGenVertexArrays(1, &vao_);
  }

  int outputWidth = width;
  int outputHeight = height;

  GL_CHECK(glBindFramebuffer(GL_FRAMEBUFFER, pixelateFrameBuffer_->getFrameBuffer()));
  if (width % 2 != 0 || height % 2 != 0) {
    GL_CHECK(glPixelStorei(GL_UNPACK_ALIGNMENT, 1));
  }

  GL_CHECK(glViewport(0, 0, outputWidth, outputHeight));
  //GL_CHECK(glClear(GL_COLOR_BUFFER_BIT | GL_STENCIL_BUFFER_BIT));
  glEnable(GL_BLEND);
  glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
  glBlendEquation(GL_FUNC_ADD);

  GL_CHECK(glUseProgram(program3_));
  GL_CHECK(glEnable(GL_STENCIL_TEST))
  GL_CHECK(glStencilFunc(GL_NOTEQUAL, 1, 0xFF))
  GL_CHECK(glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE))
  GL_CHECK(glStencilMask(0xFF))

  GL_CHECK(glActiveTexture((GL_TEXTURE1)))
  GL_CHECK(glBindTexture(GL_TEXTURE_2D, brushTexture_))
  auto brushTextureLocation = glGetUniformLocation(program3_, "brushTexture");
  GL_CHECK(glUniform1i(brushTextureLocation, 1))
  GL_CHECK(glActiveTexture(GL_TEXTURE0))
  GL_CHECK(glBindTexture(GL_TEXTURE_2D, texture));
  auto inputTextureLocation = glGetUniformLocation(program3_, "inputImageTexture");
  GL_CHECK(glUniform1i(inputTextureLocation, 0))
  auto textureSizeLocation = glGetUniformLocation(program3_, "textureSize");
  float textureSize[] = {(float) width, (float) height};
  GL_CHECK(glUniform2fv(textureSizeLocation, 1, textureSize))
  auto rectSizeLocation = glGetUniformLocation(program3_, "rectSize");
  float rectSize[] = {20, 20};
  GL_CHECK(glUniform2fv(rectSizeLocation, 1, rectSize))

  GL_CHECK(glBindVertexArray(vao_))
  GL_CHECK(glDrawArrays(GL_POINTS, 0, points))
  GL_CHECK(glBindVertexArray(0))

  GL_CHECK(glBindTexture(GL_TEXTURE_2D, 0))

  GL_CHECK(glBindFramebuffer(GL_FRAMEBUFFER, 0))
  glDisable(GL_BLEND);
  GL_CHECK(glDisable(GL_STENCIL_TEST))
  return pixelateFrameBuffer_->getTexture();

}

void Pixelator::renderScreen(GLuint texture) {
  eglCore_->makeCurrent(renderSurface_);
  if (program2_ == 0) {
    program2_ = Program::CreateProgram(DEFAULT_VERTEX_SHADER, DEFAULT_FRAGMENT_SHADER);
  }

  GL_CHECK(glEnable(GL_BLEND))
  GL_CHECK(glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA))
  GL_CHECK(glBlendEquation(GL_FUNC_ADD))
  int outputWidth = surfaceWidth_;
  int outputHeight = surfaceHeight_;

  if (outputHeight % 2 != 0 || outputWidth % 2 != 0) {
    GL_CHECK(glPixelStorei(GL_UNPACK_ALIGNMENT, 1));
  }
  GL_CHECK(glViewport(0, 0, outputWidth, outputHeight));
  GL_CHECK(glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT));

  renderScreenTexture(imageTexture_);
  renderScreenTexture(texture);

  GL_CHECK(glDisable(GL_BLEND))

  eglCore_->swapBuffers(renderSurface_);
}

void Pixelator::renderScreenTexture(GLuint texture) {

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

void Pixelator::callJavaEGLContextCreate() {
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

void Pixelator::callJavaEGLWindowCreate() {
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
