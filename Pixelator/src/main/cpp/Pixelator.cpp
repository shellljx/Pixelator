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

void Pixelator::onTouchEvent(float x, float y) {
  auto msg = new thread::Message();
  msg->what = PixelateMessage::kTouchEvent;
  msg->arg3 = x;
  msg->arg4 = y;
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
    case PixelateMessage::kTouchEvent: {
      float x = msg->arg3;
      float y = msg->arg4;
      processTouchEventInternal(x, y);
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
  auto ret = decodeImage(path, &imageWidth_, &imageHeight_);
  auto frameTexture = rendImage(imageTexture_, imageWidth_, imageHeight_);
  //auto pixelaTexture = renderPixelator(frameTexture, imageWidth_, imageHeight_);
  renderScreen(frameTexture);
  return 0;
}
bool rest = true;
int Pixelator::processTouchEventInternal(float x, float y) {
  prePoint_ = currentPoint_;
  if (rest) {
    currentPoint_ = vec2(x / surfaceWidth_, y / surfaceHeight_);
    rest = false;
    return 0;
  }
  currentPoint_ = vec2(x * 1.0f / surfaceWidth_, y * 1.0f / surfaceHeight_);
  if (prePoint_ == currentPoint_) {
    return 0;
  }
  m_PointVector_.emplace_back(prePoint_.x, prePoint_.y, currentPoint_.x, currentPoint_.y);
  return 0;
}

int Pixelator::refreshFrameInternal() {
  auto pixelaTexture = renderPixelator(imageTexture_, imageWidth_, imageHeight_);
  renderScreen(pixelaTexture);
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
    program3_ = Program::CreateProgram(DEFAULT_VERTEX_SHADER, PIXELATE_RECT_FRAGMENT_SHADER);
  }

  int outputWidth = width;
  int outputHeight = height;

  GL_CHECK(glBindFramebuffer(GL_FRAMEBUFFER, pixelateFrameBuffer_->getFrameBuffer()));
  if (width % 2 != 0 || height % 2 != 0) {
    GL_CHECK(glPixelStorei(GL_UNPACK_ALIGNMENT, 1));
  }

  GL_CHECK(glViewport(0, 0, outputWidth, outputHeight));
  GL_CHECK(glClear(GL_COLOR_BUFFER_BIT | GL_STENCIL_BUFFER_BIT));
  GL_CHECK(glUseProgram(program3_));
  GL_CHECK(glEnable(GL_STENCIL_TEST))
  GL_CHECK(glStencilFunc(GL_NOTEQUAL, 1, 0xFF))
  GL_CHECK(glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE))
  GL_CHECK(glStencilMask(0xFF))


  GL_CHECK(glActiveTexture(0))
  GL_CHECK(glBindTexture(GL_TEXTURE_2D, texture));
  auto inputTextureLocation = glGetUniformLocation(program3_, "inputImageTexture");
  GL_CHECK(glUniform1i(inputTextureLocation, 0))
  auto textureSizeLocation = glGetUniformLocation(program3_, "textureSize");
  float textureSize[] = {(float) width, (float) height};
  GL_CHECK(glUniform2fv(textureSizeLocation, 1, textureSize))
  auto rectSizeLocation = glGetUniformLocation(program3_, "rectSize");
  float rectSize[] = {20, 20};
  GL_CHECK(glUniform2fv(rectSizeLocation, 1, rectSize))

  auto positionLoction = glGetAttribLocation(program3_, "position");
  GL_CHECK(glEnableVertexAttribArray(positionLoction))
  auto textureLocation = glGetAttribLocation(program3_, "inputTextureCoordinate");
  GL_CHECK(glEnableVertexAttribArray(textureLocation));

  for (int i = 0; i < m_PointVector_.size(); i++) {
    vec4 point = m_PointVector_[i];
    calculateMesh(vec2(point.x, point.y), vec2(point.z, point.w));
    GL_CHECK(glVertexAttribPointer(positionLoction, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat),
                                   m_pVtxCoords))
    GL_CHECK(glVertexAttribPointer(textureLocation, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat),
                                   m_pTexCoords))
    GL_CHECK(glDrawArrays(GL_TRIANGLES, 0, TRIANGLE_NUM * 3))
  }

  GL_CHECK(glDisableVertexAttribArray(positionLoction))
  GL_CHECK(glDisableVertexAttribArray(textureLocation))
  GL_CHECK(glBindTexture(GL_TEXTURE_2D, 0))

  GL_CHECK(glBindFramebuffer(GL_FRAMEBUFFER, 0));
  GL_CHECK(glDisable(GL_STENCIL_TEST))
  return pixelateFrameBuffer_->getTexture();

}

static glm::vec2 texCoordToVertexCoord(glm::vec2 &texCoord) {
  return glm::vec2(2 * texCoord.x - 1, 2 * texCoord.y - 1);
}

void Pixelator::calculateMesh(vec2 pre, vec2 cur) {
  vec2 imgSize(imageWidth_, imageHeight_);
  vec2 p0 = pre * imgSize, p1 = cur * imgSize;
  vec2 v0, v1, v2, v3;
  float r = static_cast<float>(0.05 * imgSize.x);
  float x0 = p0.x, y0 = p0.y;
  float x1 = p1.x, y1 = p1.y;
  if (p0.y == p1.y) //1. 平行于 x 轴的
  {
    v0 = vec2(p0.x, p0.y - r) / imgSize;
    v1 = vec2(p0.x, p0.y + r) / imgSize;
    v2 = vec2(p1.x, p1.y - r) / imgSize;
    v3 = vec2(p1.x, p1.y + r) / imgSize;

  } else if (p0.x == p1.x) { //2. 平行于 y 轴的
    v0 = vec2(p0.x - r, p0.y) / imgSize;
    v1 = vec2(p0.x + r, p0.y) / imgSize;
    v2 = vec2(p1.x - r, p1.y) / imgSize;
    v3 = vec2(p1.x + r, p1.y) / imgSize;

  } else { //3. 其他 case

    bool xdirection = (x1 - x0) > 0;
    bool ydirection = (y1 - y0) > 0;
    if (xdirection != ydirection) {
      float xdt = abs(x0 - x1);
      float ydt = abs(y0 - y1);
      float line = sqrt(pow(xdt, 2) + pow(ydt, 2));
      float scale = r * 1.0f / line;
      float xdts = scale * ydt;
      float ydts = scale * xdt;
      v0 = vec2(x0 - xdts, y0 - ydts) / imgSize;
      v1 = vec2(x0 + xdts, y0 + ydts) / imgSize;
      v2 = vec2(x1 - xdts, y1 - ydts) / imgSize;
      v3 = vec2(x1 + xdts, y1 + ydts) / imgSize;
    } else {
      float xdt = abs(x0 - x1);
      float ydt = abs(y0 - y1);
      float line = sqrt(pow(xdt, 2) + pow(ydt, 2));
      float scale = r * 1.0f / line;
      float xdts = scale * ydt;
      float ydts = scale * xdt;
      v0 = vec2(x0 - xdts, y0 + ydts) / imgSize;
      v1 = vec2(x0 + xdts, y0 - ydts) / imgSize;
      v2 = vec2(x1 - xdts, y1 + ydts) / imgSize;
      v3 = vec2(x1 + xdts, y1 - ydts) / imgSize;
    }
  }

  // 矩形 3 个三角形
  m_pTexCoords[0] = v0;
  m_pTexCoords[1] = v1;
  m_pTexCoords[2] = v2;
  m_pTexCoords[3] = v0;
  m_pTexCoords[4] = v2;
  m_pTexCoords[5] = v3;
  m_pTexCoords[6] = v1;
  m_pTexCoords[7] = v2;
  m_pTexCoords[8] = v3;

  int index = 9;
  float step = 3.141592653589 / 10;
  // 2 个圆，一共 40 个三角形
  for (int i = 0; i < 20; ++i) {
    float x = r * cos(i * step);
    float y = r * sin(i * step);

    float x_ = r * cos((i + 1) * step);
    float y_ = r * sin((i + 1) * step);

    x += x0;
    y += y0;
    x_ += x0;
    y_ += y0;

    m_pTexCoords[index + 6 * i + 0] = vec2(x, y) / imgSize;
    m_pTexCoords[index + 6 * i + 1] = vec2(x_, y_) / imgSize;
    m_pTexCoords[index + 6 * i + 2] = vec2(x0, y0) / imgSize;

    x = r * cos(i * step);
    y = r * sin(i * step);

    x_ = r * cos((i + 1) * step);
    y_ = r * sin((i + 1) * step);

    x += x1;
    y += y1;
    x_ += x1;
    y_ += y1;

    m_pTexCoords[index + 6 * i + 3] = vec2(x, y) / imgSize;
    m_pTexCoords[index + 6 * i + 4] = vec2(x_, y_) / imgSize;
    m_pTexCoords[index + 6 * i + 5] = vec2(x1, y1) / imgSize;
  }

  for (int i = 0; i < TRIANGLE_NUM * 3; ++i) {
    m_pVtxCoords[i] = texCoordToVertexCoord(m_pTexCoords[i]);
  }
}

void Pixelator::renderScreen(GLuint texture) {
  eglCore_->makeCurrent(renderSurface_);
  if (program2_ == 0) {
    program2_ = Program::CreateProgram(DEFAULT_VERTEX_SHADER, DEFAULT_FRAGMENT_SHADER);
  }

  GL_CHECK(glEnable(GL_BLEND))
  GL_CHECK(glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA))
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
  GL_CHECK(glActiveTexture(0));
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
