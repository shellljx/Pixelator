//
// Created by shell on 2023/5/10.
//

#include "SourceRender.h"
#include "glm/glm.hpp"
#include "glm/gtc/type_ptr.hpp"
#include "glm/gtc/matrix_transform.hpp"
#include <memory>

SourceRender::SourceRender() {
  frameBuffer_ = new FrameBuffer();
  filter_ = new BaseFilter();
}

SourceRender::~SourceRender() {
  delete frameBuffer_;
  frameBuffer_ = nullptr;
  delete filter_;
  filter_ = nullptr;
}

void PrintGLError() {
  GLenum err;
  for (;;) {
    err = glGetError();
    if (err == GL_NO_ERROR) break;
    LOGE("lijinxiang egl error %d", err);
  }
}

GLuint SourceRender::draw(GLuint textureId, int width, int height, int rotate) {
  if (frameBuffer_ == nullptr) {
    return -1;
  }
  int frameWidth = width;
  int frameHeight = height;
  frameBuffer_->createFrameBuffer(frameWidth, frameHeight);
  filter_->initialize();
  FilterSource source = {textureId,DEFAULT_TEXTURE_COORDINATE, width, height};
  FilterTarget target = {frameBuffer_, {}, DEFAULT_VERTEX_COORDINATE, width, height};
  filter_->draw(&source, &target);
  return frameBuffer_->getTexture();
}

GLuint SourceRender::getFrameBuffer() {
  return frameBuffer_->getFrameBuffer();
}

GLuint SourceRender::getTexture() {
  if (frameBuffer_ != nullptr) {
    return frameBuffer_->getTexture();
  }
  return -1;
}

int SourceRender::getTextureWidth() {
  return frameBuffer_->getTextureWidth();
}

int SourceRender::getTextureHeight() {
  return frameBuffer_->getTextureHeight();
}
