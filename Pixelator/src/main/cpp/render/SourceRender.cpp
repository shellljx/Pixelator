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
  program_ = Program::CreateProgram(DEFAULT_MATRIX_VERTEX_SHADER, DEFAULT_FRAGMENT_SHADER);
  vertexCoordinate_ = new float[8];
  memcpy(vertexCoordinate_, DEFAULT_VERTEX_COORDINATE, sizeof(float) * 8);
}

SourceRender::~SourceRender() {
  delete frameBuffer_;
  frameBuffer_ = nullptr;
  if (program_ > 0) {
    glDeleteProgram(program_);
    program_ = 0;
  }
  if (vertexCoordinate_ != nullptr) {
    delete[] vertexCoordinate_;
    vertexCoordinate_ = nullptr;
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

GLuint SourceRender::draw(GLuint textureId,
                          int width,
                          int height,
                          int screenWidth,
                          int screenHeight) {
  if (frameBuffer_ == nullptr) {
    return -1;
  }
  if (width != frameWidth_ || height != frameHeight_ || screenWidth != screenWidth_ ||
      screenHeight != screenHeight_) {
    frameWidth_ = width;
    frameHeight_ = height;
    screenWidth_ = screenWidth;
    screenHeight_ = screenHeight;
    cropVertexCoordinate();
    frameBuffer_->createFrameBuffer(static_cast<int>(fitWidth_), static_cast<int>(fitHeight_));
  }
  glm::mat4 projection = glm::ortho(0.f, fitWidth_,
                                    0.f, fitHeight_, 1.f, 100.f);
  glm::vec3 position = glm::vec3(0.f, 0.f, 10.f);
  glm::vec3 direction = glm::vec3(0.f, 0.f, 0.f);
  glm::vec3 up = glm::vec3(0.f, 1.f, 0.f);
  glm::mat4 viewMatrix = glm::lookAt(position, direction, up);
  auto matrix = glm::mat4(1);

  glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer_->getFrameBuffer());
  glViewport(0, 0, fitWidth_, fitHeight_);
  glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
  glClearColor(1.f, 1.f, 1.f, 1.f);
  glUseProgram(program_);

  auto positionLoc = glGetAttribLocation(program_, "position");
  glEnableVertexAttribArray(positionLoc);
  glVertexAttribPointer(positionLoc, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat),
                        vertexCoordinate_);
  auto textureCoordinateLoc = glGetAttribLocation(program_, "inputTextureCoordinate");
  glEnableVertexAttribArray(textureCoordinateLoc);
  glVertexAttribPointer(textureCoordinateLoc, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat),
                        DEFAULT_TEXTURE_COORDINATE);

  matrix = projection * viewMatrix * matrix;
  auto mvpLoc = glGetUniformLocation(program_, "mvp");
  glUniformMatrix4fv(mvpLoc, 1, GL_FALSE, glm::value_ptr(matrix));

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

GLuint SourceRender::getTexture() {
  if (frameBuffer_ != nullptr) {
    return frameBuffer_->getTexture();
  }
  return -1;
}

void SourceRender::cropVertexCoordinate() {
  float screenRatio = static_cast<float>(screenWidth_) / static_cast<float>(screenHeight_);
  float frameRatio = static_cast<float>(frameWidth_) / static_cast<float>(frameHeight_);
  float widthScale = 1.f;
  float heightScale = 1.f;
  if (frameRatio > screenRatio) {
    //frame比屏幕宽
    heightScale = screenRatio / frameRatio;
  } else {
    //frame比屏幕窄
    widthScale = frameRatio / screenRatio;
  }
  fitHeight_ = static_cast<float>(screenHeight_) * heightScale;
  fitWidth_ = static_cast<float>(screenWidth_) * widthScale;

  if (vertexCoordinate_ != nullptr) {
    vertexCoordinate_[0] = 0.f;
    vertexCoordinate_[1] = fitHeight_;
    vertexCoordinate_[2] = fitWidth_;
    vertexCoordinate_[3] = fitHeight_;
    vertexCoordinate_[4] = 0.f;
    vertexCoordinate_[5] = 0.f;
    vertexCoordinate_[6] = fitWidth_;
    vertexCoordinate_[7] = 0.f;
  }
}
