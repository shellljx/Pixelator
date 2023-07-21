//
// Created by 李金祥 on 2022/11/12.
//

#include "FrameBuffer.h"
#include "Log.h"

FrameBuffer::FrameBuffer() : frameBufferId_(0), frameTextureId_(0), width_(0), height_(0) {
}

FrameBuffer::~FrameBuffer() {
  deleteFrameBuffer();
}

void FrameBuffer::createFrameBuffer(int width, int height) {
  if (width == width_ && height == height_) {
    return;
  }
  if (frameBufferId_ == 0) {
    GL_CHECK(glGenTextures(1, &frameTextureId_))
    GL_CHECK(glGenFramebuffers(1, &frameBufferId_))
  }

  GL_CHECK(glBindFramebuffer(GL_FRAMEBUFFER, frameBufferId_))
  GL_CHECK(glBindTexture(GL_TEXTURE_2D, frameTextureId_))
  GL_CHECK(glTexImage2D(GL_TEXTURE_2D,
                        0,
                        GL_RGBA,
                        width,
                        height,
                        0,
                        GL_RGBA,
                        GL_UNSIGNED_BYTE,
                        nullptr))
  GL_CHECK(glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE))
  GL_CHECK(glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE))
  GL_CHECK(glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR))
  GL_CHECK(glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR))
  GL_CHECK(glFramebufferTexture2D(GL_FRAMEBUFFER,
                                  GL_COLOR_ATTACHMENT0,
                                  GL_TEXTURE_2D,
                                  frameTextureId_,
                                  0))
  auto status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
  if (status != GL_FRAMEBUFFER_COMPLETE) {
    LOGE("frame buffer create error %d, %d", width, height);
  }
  GL_CHECK(glBindTexture(GL_TEXTURE_2D, 0))
  GL_CHECK(glBindFramebuffer(GL_FRAMEBUFFER, GL_NONE))
  width_ = width;
  height_ = height;
}

void FrameBuffer::clear() {
  if (frameBufferId_ > 0) {
    glBindFramebuffer(GL_FRAMEBUFFER, frameBufferId_);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glBindFramebuffer(GL_FRAMEBUFFER, GL_NONE);
  }
}

GLuint FrameBuffer::getTexture() {
  return frameTextureId_;
}

int FrameBuffer::getTextureWidth() {
  return width_;
}

int FrameBuffer::getTextureHeight() {
  return height_;
}

GLuint FrameBuffer::getFrameBuffer() const {
  return frameBufferId_;
}

void FrameBuffer::deleteFrameBuffer() {
  if (frameBufferId_ > 0) {
    GL_CHECK(glDeleteFramebuffers(1, &frameBufferId_))
    frameBufferId_ = 0;
  }

  if (frameTextureId_ > 0) {
    GL_CHECK(glDeleteTextures(1, &frameTextureId_));
    frameTextureId_ = 0;
  }
  width_ = 0;
  height_ = 0;
}

bool FrameBuffer::isCreated() {
  return frameBufferId_ > 0 && frameTextureId_ > 0;
}
