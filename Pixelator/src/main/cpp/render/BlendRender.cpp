//
// Created by shell m1 on 2023/5/21.
//

#include "BlendRender.h"
#include "JNIEnvironment.h"
#include "Local.h"
#include <android/bitmap.h>

BlendRender::BlendRender() : frameBuffer_(nullptr), buffer_(nullptr) {
  frameBuffer_ = new FrameBuffer();
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

  drawTexture(textureId, false);
  drawTexture(maskTexture, true);
  if (maskTexture2 != GL_NONE) {
    drawTexture(maskTexture2, true);
  }
  if (buffer_ == nullptr) {
    buffer_ = new uint8_t[width * height * 4];
  }
  GL_CHECK(glDisable(GL_BLEND))
  GL_CHECK(glBindFramebuffer(GL_FRAMEBUFFER, GL_NONE))

  return frameBuffer_->getTexture();
}

void BlendRender::drawTexture(GLuint textureId, bool revert) {
  auto textureCoordinate = DEFAULT_TEXTURE_COORDINATE;
  if (revert) {
    textureCoordinate = DEFAULT_TEXTURE_COORDINATE_FLIP_DOWN_UP;
  }
  GL_CHECK(glUseProgram(program_))
  auto positionLoction = glGetAttribLocation(program_, "position");
  GL_CHECK(glEnableVertexAttribArray(positionLoction))
  //因为绘制的时候是以左上角为原点为基准来定的绘制点，但是合成的时候是opengl默认的左下角为原点，所以要上下反转
  GL_CHECK(glVertexAttribPointer(positionLoction, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat),
                                 DEFAULT_VERTEX_COORDINATE))
  auto textureLocation = glGetAttribLocation(program_, "inputTextureCoordinate");
  GL_CHECK(glEnableVertexAttribArray(textureLocation))
  GL_CHECK(glVertexAttribPointer(textureLocation, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat),
                                 textureCoordinate))

  GL_CHECK(glActiveTexture(GL_TEXTURE0));
  GL_CHECK(glBindTexture(GL_TEXTURE_2D, textureId));
  auto inputTextureLocation = glGetUniformLocation(program_, "inputImageTexture");
  GL_CHECK(glUniform1i(inputTextureLocation, 0))
  GL_CHECK(glDrawArrays(GL_TRIANGLE_STRIP, 0, 4))

  GL_CHECK(glDisableVertexAttribArray(positionLoction))
  GL_CHECK(glDisableVertexAttribArray(textureLocation))
  GL_CHECK(glBindTexture(GL_TEXTURE_2D, 0))
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