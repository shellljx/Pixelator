//
// Created by shell m1 on 2023/5/21.
//

#include "BlendRender.h"
#include "JNIEnvironment.h"
#include "Local.h"
#include <android/bitmap.h>

BlendRender::BlendRender(jobject object) : frameBuffer_(nullptr), buffer_(nullptr) {
  frameBuffer_ = new FrameBuffer();
  program_ = Program::CreateProgram(DEFAULT_VERTEX_SHADER, DEFAULT_FRAGMENT_SHADER);
  pixelator_.reset(JNIEnvironment::Current(), object);
}

BlendRender::~BlendRender() {
  delete frameBuffer_;
  frameBuffer_ = nullptr;
  if (program_ != 0) {
    glDeleteProgram(program_);
    program_ = 0;
  }
  delete[] buffer_;
  buffer_ = nullptr;
}

GLuint BlendRender::draw(GLuint textureId, GLuint maskTexture, int width, int height) {
  frameBuffer_->createFrameBuffer(width, height);
  glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer_->getFrameBuffer());
  GL_CHECK(glEnable(GL_BLEND))
  GL_CHECK(glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA))
  GL_CHECK(glBlendEquation(GL_FUNC_ADD))
  if (width % 2 != 0 || height % 2 != 0) {
    GL_CHECK(glPixelStorei(GL_UNPACK_ALIGNMENT, 1));
  }
  GL_CHECK(glViewport(0, 0, width, height));
  GL_CHECK(glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT));
  GL_CHECK(glClearColor(0.f, 0.f, 0.f, 0.f))

  drawTexture(textureId, width, height);
  drawTexture(maskTexture, width, height);

  if (buffer_ == nullptr) {
    buffer_ = new uint8_t[width * height * 4];
  }
  GL_CHECK(glDisable(GL_BLEND))
  GL_CHECK(glBindFramebuffer(GL_FRAMEBUFFER, 0))

  return frameBuffer_->getTexture();
}

void BlendRender::drawTexture(GLuint textureId, int width, int height) {
  GL_CHECK(glUseProgram(program_))
  auto positionLoction = glGetAttribLocation(program_, "position");
  GL_CHECK(glEnableVertexAttribArray(positionLoction))
  //因为绘制的时候是以左上角为原点为基准来定的绘制点，但是合成的时候是opengl默认的左下角为原点，所以要上下反转
  GL_CHECK(glVertexAttribPointer(positionLoction, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat),
                                 DEFAULT_VERTEX_COORDINATE))
  auto textureLocation = glGetAttribLocation(program_, "inputTextureCoordinate");
  GL_CHECK(glEnableVertexAttribArray(textureLocation))
  GL_CHECK(glVertexAttribPointer(textureLocation, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat),
                                 DEFAULT_TEXTURE_COORDINATE_FLIP_DOWN_UP))

  GL_CHECK(glActiveTexture(GL_TEXTURE0));
  GL_CHECK(glBindTexture(GL_TEXTURE_2D, textureId));
  auto inputTextureLocation = glGetUniformLocation(program_, "inputImageTexture");
  GL_CHECK(glUniform1i(inputTextureLocation, 0))
  GL_CHECK(glDrawArrays(GL_TRIANGLE_STRIP, 0, 4))

  GL_CHECK(glDisableVertexAttribArray(positionLoction))
  GL_CHECK(glDisableVertexAttribArray(textureLocation))
  GL_CHECK(glBindTexture(GL_TEXTURE_2D, 0))
}

void BlendRender::createBlendBitmap(int width, int height) {
  JNIEnv *env = JNIEnvironment::Current();
  if (env == nullptr) {
    return;
  }
  Local<jclass> bitmapClass = {env, env->FindClass("android/graphics/Bitmap")};
  const char *bitmapCreateMethod = "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;";
  jmethodID bitmapCreateMethodId = env->GetStaticMethodID(bitmapClass.get(), "createBitmap", bitmapCreateMethod);

  jstring configName = env->NewStringUTF("ARGB_8888");
  Local<jclass> configClass = {env, env->FindClass("android/graphics/Bitmap$Config")};
  const char *bitmapConfigSignature = "(Ljava/lang/String;)Landroid/graphics/Bitmap$Config;";
  jmethodID bitmapConfigMethodId = env->GetStaticMethodID(configClass.get(), "valueOf", bitmapConfigSignature);
  Local<jobject> bitmapConfig = {env, env->CallStaticObjectMethod(configClass.get(), bitmapConfigMethodId, configName)};
  Local<jobject> bitmap = {env, env->CallStaticObjectMethod(bitmapClass.get(), bitmapCreateMethodId, width, height, bitmapConfig.get())};

  void *bitmapPixels;
  if ((AndroidBitmap_lockPixels(env, bitmap.get(), &bitmapPixels)) < 0) {
    return;
  }
  memcpy((uint8_t *) bitmapPixels, buffer_, width * height * 4);
  AndroidBitmap_unlockPixels(env, bitmap.get());

  Local<jclass> sdkClass = {env, env->GetObjectClass(pixelator_.get())};
  jmethodID frameSavedMethodId = env->GetMethodID(sdkClass.get(), "onFrameSaved", "(Landroid/graphics/Bitmap;)V");
  env->CallVoidMethod(pixelator_.get(), frameSavedMethodId, bitmap.get());
}

void BlendRender::save() {
  glBindFramebuffer(GL_READ_FRAMEBUFFER, frameBuffer_->getFrameBuffer());
  glReadPixels(0, 0, frameBuffer_->getTextureWidth(), frameBuffer_->getTextureHeight(), GL_RGBA, GL_UNSIGNED_BYTE, buffer_);
  createBlendBitmap(frameBuffer_->getTextureWidth(), frameBuffer_->getTextureHeight());
  glBindFramebuffer(GL_READ_FRAMEBUFFER, GL_NONE);
}

GLuint BlendRender::getTexture() {
  return frameBuffer_->getTexture();
}

int BlendRender::getWidth() {
  return frameBuffer_->getTextureWidth();
}

int BlendRender::getHeight() {
  return frameBuffer_->getTextureHeight();
}