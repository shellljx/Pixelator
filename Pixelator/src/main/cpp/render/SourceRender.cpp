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
  program_ = Program::CreateProgram(DEFAULT_VERTEX_SHADER, DEFAULT_FRAGMENT_SHADER);
}

SourceRender::~SourceRender() {
  delete frameBuffer_;
  frameBuffer_ = nullptr;
  if (program_ > 0) {
    glDeleteProgram(program_);
    program_ = 0;
  }
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
  float *textureCoordinate = DEFAULT_TEXTURE_COORDINATE;
  if (rotate % 360 == 90) {
    frameWidth = height;
    frameHeight = width;
    textureCoordinate = TEXTURE_COORDINATE_270_FLIP_UP_DOWN;
  } else if (rotate % 360 == 180) {
    textureCoordinate = TEXTURE_COORDINATE_180_FLIP_UP_DOWN;
  } else if (rotate % 360 == 270) {
    frameWidth = height;
    frameHeight = width;
  }
  frameBuffer_->createFrameBuffer(frameWidth, frameHeight);

  glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer_->getFrameBuffer());
  glViewport(0, 0, frameWidth, frameHeight);
  glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
  glUseProgram(program_);

  auto positionLoc = glGetAttribLocation(program_, "position");
  glEnableVertexAttribArray(positionLoc);
  glVertexAttribPointer(positionLoc, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat),
                        DEFAULT_VERTEX_COORDINATE);
  auto textureCoordinateLoc = glGetAttribLocation(program_, "inputTextureCoordinate");
  glEnableVertexAttribArray(textureCoordinateLoc);
  glVertexAttribPointer(textureCoordinateLoc, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat),
                        textureCoordinate);

  auto inputTextureLoc = glGetUniformLocation(program_, "inputImageTexture");
  glActiveTexture(GL_TEXTURE0);
  glBindTexture(GL_TEXTURE_2D, textureId);
  glUniform1i(inputTextureLoc, 0);
  glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
  glDisableVertexAttribArray(positionLoc);
  glDisableVertexAttribArray(textureCoordinateLoc);
  glBindTexture(GL_TEXTURE_2D, GL_NONE);
  glBindFramebuffer(GL_FRAMEBUFFER, GL_NONE);
  PrintGLError();
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
