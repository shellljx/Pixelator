//
// Created by shell m1 on 2023/5/26.
//

#include "MiniScreenRender.h"

MiniScreenRender::MiniScreenRender(EGLCore *eglCore) : nativeWindow_(nullptr),
                                                       eglSurface_(EGL_NO_SURFACE) {
  program_ = Program::CreateProgram(DEFAULT_MATRIX_VERTEX_SHADER, DEFAULT_FRAGMENT_SHADER);
}

MiniScreenRender::~MiniScreenRender() {
  if (program_ > 0) {
    glDeleteProgram(program_);
    program_ = 0;
  }
}

void MiniScreenRender::createEglSurface(EGLCore *eglCore, ANativeWindow *window) {
  nativeWindow_ = window;
  eglSurface_ = eglCore->createWindowSurface(window);
}

void MiniScreenRender::surfaceChanged(int width, int height) {
  surfaceWidth_ = width;
  surfaceHeight_ = height;
}

void MiniScreenRender::draw(GLuint texture, EGLCore *eglCore, int textureWidth, int textureHeight) {
  if (eglCore == nullptr || eglSurface_ == EGL_NO_SURFACE) {
    return;
  }
  eglCore->makeCurrent(eglSurface_);
  glViewport(0, 0, surfaceWidth_, surfaceHeight_);
  glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
  glUseProgram(program_);
  glm::mat4 projection = glm::ortho(0.f, surfaceWidth_ * 1.f,
                                    surfaceHeight_ * 1.f, 0.f, 1.f, 100.f);
  glm::vec3 position = glm::vec3(0.f, 0.f, 10.f);
  glm::vec3 direction = glm::vec3(0.f, 0.f, 0.f);
  glm::vec3 up = glm::vec3(0.f, 1.f, 0.f);
  glm::mat4 viewMatrix = glm::lookAt(position, direction, up);
  auto positionLoc = glGetAttribLocation(program_, "position");
  glEnableVertexAttribArray(positionLoc);
  float vertexCoordinate_[8] = {0.f, 0.f, textureWidth * 1.f, 0.f, 0.f, textureHeight * 1.f,
                                textureWidth * 1.f, textureHeight * 1.f};
  glVertexAttribPointer(positionLoc, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat),
                        vertexCoordinate_);
  auto textureCoordinateLoc = glGetAttribLocation(program_, "inputTextureCoordinate");
  glEnableVertexAttribArray(textureCoordinateLoc);
  glVertexAttribPointer(textureCoordinateLoc, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat),
                        DEFAULT_TEXTURE_COORDINATE);
  auto mvpLoc = glGetUniformLocation(program_, "mvp");
  auto model = glm::mat4(1);
  model = glm::translate(model, glm::vec3(-x_, -y_, 0.f));
  model = glm::translate(model, glm::vec3(surfaceWidth_ / 2.f, surfaceHeight_ / 2.f, 0.f));
  auto mvp = projection * viewMatrix * model * transformMatrix_;
  glUniformMatrix4fv(mvpLoc, 1, GL_FALSE, glm::value_ptr(mvp));

  auto inputTextureLoc = glGetUniformLocation(program_, "inputImageTexture");
  glActiveTexture(GL_TEXTURE0);
  glBindTexture(GL_TEXTURE_2D, texture);
  glUniform1i(inputTextureLoc, 0);
  glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
  glDisableVertexAttribArray(positionLoc);
  glDisableVertexAttribArray(textureCoordinateLoc);
  glBindTexture(GL_TEXTURE_2D, GL_NONE);
  eglCore->swapBuffers(eglSurface_);
  eglCore->makeCurrent(EGL_NO_SURFACE);
}

void MiniScreenRender::destroyEglSurface(EGLCore *eglCore) {
  if (nativeWindow_ != nullptr) {
    ANativeWindow_release(nativeWindow_);
    nativeWindow_ = nullptr;
  }
  if (eglCore != nullptr && eglSurface_ != EGL_NO_SURFACE) {
    eglCore->makeCurrent(eglSurface_);
    eglCore->releaseSurface(eglSurface_);
    eglCore->makeCurrent(EGL_NO_SURFACE);
    eglSurface_ = EGL_NO_SURFACE;
  }
}

void MiniScreenRender::tranlate(float x, float y) {
  auto spaceX = surfaceWidth_ / 2.f;
  auto spaceY = surfaceHeight_ / 2.f;
  if (x - left_ < spaceX) {
    x_ = spaceX + left_;
  } else if (right_ - x < spaceX) {
    x_ = right_ - spaceX;
  } else {
    x_ = x;
  }
  if (y - top_ < spaceY) {
    y_ = spaceY + top_;
  } else if (bottom_ - y < spaceY) {
    y_ = bottom_ - spaceY;
  } else {
    y_ = y;
  }
}
void MiniScreenRender::setBounds(float left, float top, float right, float bottom) {
  left_ = left;
  top_ = top;
  right_ = right;
  bottom_ = bottom;
}
