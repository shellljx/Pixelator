//
// Created by shell m1 on 2023/5/21.
//

#include "BlendRender.h"
#include "JNIEnvironment.h"
#include "Local.h"
#include <android/bitmap.h>

BlendRender::BlendRender() : frameBuffer_(nullptr), buffer_(nullptr) {
  frameBuffer_ = new FrameBuffer();
  filter_ = new BaseFilter();
  program_ = Program::CreateProgram(DEFAULT_VERTEX_SHADER, DEFAULT_FRAGMENT_SHADER);
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

GLuint BlendRender::draw(GLuint textureId, GLuint maskTexture, GLuint maskTexture2, int width, int height) {
  frameBuffer_->createFrameBuffer(width, height);
  drawTexture(textureId, false, width, height);
  drawTexture(maskTexture, true, width, height);
  if (maskTexture2 != GL_NONE) {
    drawTexture(maskTexture2, true, width, height);
  }
  if (buffer_ == nullptr) {
    buffer_ = new uint8_t[width * height * 4];
  }
  return frameBuffer_->getTexture();
}

void BlendRender::drawTexture(GLuint textureId, bool revert, int width, int height) {
  auto textureCoordinate = DEFAULT_TEXTURE_COORDINATE;
  if (revert) {
    textureCoordinate = DEFAULT_TEXTURE_COORDINATE_FLIP_DOWN_UP;
  }
  filter_->initialize();
  filter_->enableBlend(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
  FilterSource source = {textureId, textureCoordinate, width, height};
  FilterTarget target = {frameBuffer_, {}, DEFAULT_VERTEX_COORDINATE, width, height, false};
  filter_->draw(&source, &target);
  filter_->disableBlend();
}

GLuint BlendRender::getFrameBuffer() {
  return frameBuffer_->getFrameBuffer();
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