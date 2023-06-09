//
// Created by shell m1 on 2023/6/7.
//

#include "DeeplabMaskRender.h"
#include "JNIEnvironment.h"
#include "Local.h"
#include <jni.h>
#include <android/bitmap.h>
#include <memory>

DeeplabMaskRender::DeeplabMaskRender(jobject object) : buffer_(nullptr) {
  frame_buffer_ = new FrameBuffer();
  program_ = Program::CreateProgram(DEFAULT_VERTEX_SHADER, DEEPLAB_FRAGMENT_SHADER);
  pixelator_.reset(JNIEnvironment::Current(), object);
}

DeeplabMaskRender::~DeeplabMaskRender() {
  if (frame_buffer_ != nullptr) {
    delete frame_buffer_;
    frame_buffer_ = nullptr;
  }
  if (program_ > 0) {
    glDeleteProgram(program_);
    program_ = 0;
  }

  delete[] buffer_;
  buffer_ = nullptr;
}

void DeeplabMaskRender::draw(GLuint textureId, int width, int height) {
  frame_buffer_->createFrameBuffer(width, height);
  glBindFramebuffer(GL_FRAMEBUFFER, frame_buffer_->getFrameBuffer());
  GL_CHECK(glUseProgram(program_))
  GL_CHECK(glViewport(0, 0, width, height))
  auto positionLoction = glGetAttribLocation(program_, "position");
  GL_CHECK(glEnableVertexAttribArray(positionLoction))
  GL_CHECK(glVertexAttribPointer(positionLoction, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat),
                                 DEFAULT_VERTEX_COORDINATE))
  auto textureLocation = glGetAttribLocation(program_, "inputTextureCoordinate");
  GL_CHECK(glEnableVertexAttribArray(textureLocation))
  GL_CHECK(glVertexAttribPointer(textureLocation, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat),
                                 DEFAULT_TEXTURE_COORDINATE))

  GL_CHECK(glActiveTexture(GL_TEXTURE0));
  GL_CHECK(glBindTexture(GL_TEXTURE_2D, textureId))
  auto inputTextureLocation = glGetUniformLocation(program_, "inputImageTexture");
  GL_CHECK(glUniform1i(inputTextureLocation, 0))
  auto modeLocation = glGetUniformLocation(program_, "mode");
  GL_CHECK(glUniform1i(modeLocation, maskMode_))
  GL_CHECK(glDrawArrays(GL_TRIANGLE_STRIP, 0, 4))
  GL_CHECK(glDisableVertexAttribArray(positionLoction))
  GL_CHECK(glDisableVertexAttribArray(textureLocation))
  GL_CHECK(glBindTexture(GL_TEXTURE_2D, GL_NONE))
  glBindFramebuffer(GL_FRAMEBUFFER, GL_NONE);
  if (buffer_ == nullptr) {
    buffer_ = new uint8_t[width * height * 4];
  }
}

void DeeplabMaskRender::download() {
  glBindFramebuffer(GL_READ_FRAMEBUFFER, frame_buffer_->getFrameBuffer());
  glReadPixels(0, 0, frame_buffer_->getTextureWidth(), frame_buffer_->getTextureHeight(), GL_RGBA, GL_UNSIGNED_BYTE, buffer_);
  createBlendBitmap(frame_buffer_->getTextureWidth(), frame_buffer_->getTextureHeight());
  glBindFramebuffer(GL_READ_FRAMEBUFFER, GL_NONE);
}

void DeeplabMaskRender::createBlendBitmap(int width, int height) {
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
  jmethodID frameSavedMethodId = env->GetMethodID(sdkClass.get(), "onDeeplabMaskCreated", "(Landroid/graphics/Bitmap;)V");
  env->CallVoidMethod(pixelator_.get(), frameSavedMethodId, bitmap.get());
}

GLuint DeeplabMaskRender::getTexture() {
  return frame_buffer_->getTexture();
}

void DeeplabMaskRender::setMaskMode(int mode) {
  maskMode_ = mode;
}