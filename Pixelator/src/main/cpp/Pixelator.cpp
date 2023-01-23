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
  if (imageTexture_ > 0) {
    glDeleteTextures(1, &imageTexture_);
    imageTexture_ = 0;
  }
  delete frameBuffer_;
  frameBuffer_ = nullptr;

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
  int width = 0;
  int height = 0;
  auto ret = decodeImage(path, &width, &height);
  auto frameTexture = rendImage(imageTexture_, width, height);
  //surface width, surface height
  renderScreen(frameTexture);
  return 0;
}

int Pixelator::decodeImage(const char *path, int *width, int *height) {

  int channel = 0;
  auto data = stbi_load(path, width, height, &channel, STBI_rgb_alpha);
  if (*width == 0 || *height == 0 || data == nullptr) {
    LOGE("decode image error");
    return -1;
  }
  if (imageTexture_ == 0) {
    glGenTextures(1, &imageTexture_);
  }
  if (*width % 2 != 0 || *height % 2 != 0) {
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
  }
  glActiveTexture(0);
  glBindTexture(GL_TEXTURE_2D, imageTexture_);
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
  GL_CHECK(glVertexAttribPointer(positionLoction, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat), DEFAULT_VERTEX_COORDINATE))
  auto textureLocation = glGetAttribLocation(program1_, "inputTextureCoordinate");
  GL_CHECK(glEnableVertexAttribArray(textureLocation));
  GL_CHECK(glVertexAttribPointer(textureLocation, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat), TEXTURE_COORDINATE_FLIP_UP_DOWN))
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

void Pixelator::renderScreen(GLuint texture) {
  eglCore_->makeCurrent(renderSurface_);
  if (program2_ == 0) {
    program2_ = Program::CreateProgram(DEFAULT_VERTEX_SHADER, DEFAULT_FRAGMENT_SHADER);
  }

  int outputWidth = surfaceWidth_;
  int outputHeight = surfaceHeight_;

  if (outputHeight % 2 != 0 || outputWidth % 2 != 0) {
    GL_CHECK(glPixelStorei(GL_UNPACK_ALIGNMENT, 1));
  }

  GL_CHECK(glViewport(0, 0, outputWidth, outputHeight));
  GL_CHECK(glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT));
  GL_CHECK(glUseProgram(program2_));
  auto positionLoction = glGetAttribLocation(program2_, "position");
  GL_CHECK(glEnableVertexAttribArray(positionLoction))
  GL_CHECK(glVertexAttribPointer(positionLoction, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat), DEFAULT_VERTEX_COORDINATE))
  auto textureLocation = glGetAttribLocation(program2_, "inputTextureCoordinate");
  GL_CHECK(glEnableVertexAttribArray(textureLocation));
  GL_CHECK(glVertexAttribPointer(textureLocation, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat), TEXTURE_COORDINATE_FLIP_UP_DOWN))
  GL_CHECK(glActiveTexture(0));
  GL_CHECK(glBindTexture(GL_TEXTURE_2D, texture));
  auto inputTextureLocation = glGetUniformLocation(program2_, "inputImageTexture");
  GL_CHECK(glUniform1i(inputTextureLocation, 0))
  GL_CHECK(glDrawArrays(GL_TRIANGLE_STRIP, 0, 4))
  GL_CHECK(glDisableVertexAttribArray(positionLoction))
  GL_CHECK(glDisableVertexAttribArray(textureLocation))
  GL_CHECK(glBindTexture(GL_TEXTURE_2D, 0))

  eglCore_->swapBuffers(renderSurface_);
}

void Pixelator::callJavaEGLContextCreate() {
  if (pixelator_.empty()) {
    return;
  }
  JNIEnv *env = JNIEnvironment::Current();
  if (env != nullptr) {
    Local<jclass> jclass = {env, env->GetObjectClass(pixelator_.get())};
    jmethodID eglContextCreateMethodId = env->GetMethodID(jclass.get(), "onEGLContextCreate", "()V");
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
    jmethodID eglWindowCreateMethodId = env->GetMethodID(jclass.get(), "onEGLWindowCreate", "()V");
    if (eglWindowCreateMethodId != nullptr) {
      env->CallVoidMethod(pixelator_.get(), eglWindowCreateMethodId);
    }
  }
}
