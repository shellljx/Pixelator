//
// Created by 李金祥 on 2023/4/23.
//

#include "ScreenRender.h"
#include <memory>

ScreenRender::ScreenRender() : frameBuffer_(nullptr), vertexCoordinate_(nullptr) {
  frameBuffer_ = new FrameBuffer();
  program_ = Program::CreateProgram(DEFAULT_MATRIX_VERTEX_SHADER, DEFAULT_FRAGMENT_SHADER);
  vertexCoordinate_ = new float[8];
  memcpy(vertexCoordinate_, DEFAULT_VERTEX_COORDINATE, sizeof(float) * 8);
}

ScreenRender::~ScreenRender() {
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

void ScreenRender::initMatrix(int screenWidth, int screenHeight, int textureWidth, int textureHeight) {
  projectionMatrix_ = glm::ortho(0.f, static_cast<float>(screenWidth),
                                    static_cast<float>(screenHeight), 0.f, 1.f, 100.f);
  glm::vec3 position = glm::vec3(0.f, 0.f, 10.f);
  glm::vec3 direction = glm::vec3(0.f, 0.f, 0.f);
  glm::vec3 up = glm::vec3(0.f, 1.f, 0.f);
  viewMatrix_ = glm::lookAt(position, direction, up);

  float screenRatio = static_cast<float>(screenWidth) / static_cast<float>(screenHeight);
  float frameRatio = static_cast<float>(textureWidth) / static_cast<float>(textureHeight);
  float x = 0.f;
  float y = 0.f;
  if (frameRatio > screenRatio) {
    //frame比屏幕宽
    scale_ = screenWidth * 1.f / textureWidth;
    y = (screenHeight - textureHeight * scale_) / 2.f;
  } else {
    //frame比屏幕窄
    scale_ = screenHeight * 1.f / textureHeight;
    x = (screenWidth - textureWidth * scale_) / 2.f;
  }
  modelMatrix_ = glm::translate(modelMatrix_, glm::vec3(x, y, 0.f));
  modelMatrix_ = glm::scale(modelMatrix_, glm::vec3(scale_, scale_, 1.f));
}

GLuint ScreenRender::draw(GLuint textureId, GLuint maskTexture, int width, int height, int screenWidth, int screenHeight) {
  if (frameBuffer_ == nullptr) {
    return -1;
  }
  //cropVertexCoordinate(width, height, screenWidth, screenHeight, &fitWidth_, &fitHeight_);

  if (vertexCoordinate_ != nullptr) {
    vertexCoordinate_[0] = 0.f;
    vertexCoordinate_[1] = height;
    vertexCoordinate_[2] = width;
    vertexCoordinate_[3] = height;
    vertexCoordinate_[4] = 0.f;
    vertexCoordinate_[5] = 0.f;
    vertexCoordinate_[6] = width;
    vertexCoordinate_[7] = 0.f;
  }

  GL_CHECK(glEnable(GL_BLEND))
  GL_CHECK(glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA))
  GL_CHECK(glBlendEquation(GL_FUNC_ADD))
  if (screenWidth % 2 != 0 || screenHeight % 2 != 0) {
    GL_CHECK(glPixelStorei(GL_UNPACK_ALIGNMENT, 1));
  }
  GL_CHECK(glViewport(0, 0, screenWidth, screenHeight));
  GL_CHECK(glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT));

    drawTexture(textureId, width, height, screenWidth, screenHeight);
  drawTexture(maskTexture, width, height, screenWidth, screenHeight);

  GL_CHECK(glDisable(GL_BLEND))

  return frameBuffer_->getTexture();
}

void ScreenRender::drawTexture(GLuint textureId, int width, int height, int screenWidth, int screenHeight) {
  GL_CHECK(glUseProgram(program_))
  auto positionLoction = glGetAttribLocation(program_, "position");
  GL_CHECK(glEnableVertexAttribArray(positionLoction))
  GL_CHECK(glVertexAttribPointer(positionLoction, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat),
                                 vertexCoordinate_))
  auto textureLocation = glGetAttribLocation(program_, "inputTextureCoordinate");
  GL_CHECK(glEnableVertexAttribArray(textureLocation))
  GL_CHECK(glVertexAttribPointer(textureLocation, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat),
                                 DEFAULT_TEXTURE_COORDINATE))
  auto matrix = projectionMatrix_ * viewMatrix_ * transformMatrix_  * modelMatrix_;
  auto mvpLoc = glGetUniformLocation(program_, "mvp");
  glUniformMatrix4fv(mvpLoc, 1, GL_FALSE, glm::value_ptr(matrix));

  GL_CHECK(glActiveTexture(GL_TEXTURE0));
  GL_CHECK(glBindTexture(GL_TEXTURE_2D, textureId));
  auto inputTextureLocation = glGetUniformLocation(program_, "inputImageTexture");
  GL_CHECK(glUniform1i(inputTextureLocation, 0))
  GL_CHECK(glDrawArrays(GL_TRIANGLE_STRIP, 0, 4))
  GL_CHECK(glDisableVertexAttribArray(positionLoction))
  GL_CHECK(glDisableVertexAttribArray(textureLocation))
  GL_CHECK(glBindTexture(GL_TEXTURE_2D, 0))
}

GLuint ScreenRender::getTexture() {
  return frameBuffer_->getTexture();
}

void ScreenRender::translate(float scale, float pivotX, float pivotY, float angle, float translateX, float translateY) {
  scale_ = scale;
  pivotX_ = pivotX;
  pivotY_ = pivotY;
  angle_ = angle;
  translateX_ = translateX;
  translateY_ = translateY;
}

void ScreenRender::setMatrix(glm::mat4 matrix) {
  transformMatrix_ = matrix;
}

void ScreenRender::cropVertexCoordinate(int frameWidth, int frameHeight, int screenWidth, int screenHeight, int *fitWidth, int *fitHeight) {
  float screenRatio = static_cast<float>(screenWidth) / static_cast<float>(screenHeight);
  float frameRatio = static_cast<float>(frameWidth) / static_cast<float>(frameHeight);
  float widthScale = 1.f;
  float heightScale = 1.f;
  if (frameRatio > screenRatio) {
    //frame比屏幕宽
    heightScale = screenRatio / frameRatio;
  } else {
    //frame比屏幕窄
    widthScale = frameRatio / screenRatio;
  }
  *fitHeight = static_cast<int>(screenHeight * heightScale);
  *fitWidth = static_cast<int>(screenWidth * widthScale);
  x_ = (screenWidth - *fitWidth) / 2.f;
  y_ = (screenHeight - *fitHeight) / 2.f;

  if (vertexCoordinate_ != nullptr) {
    vertexCoordinate_[0] = x_;
    vertexCoordinate_[1] = *fitHeight + y_;
    vertexCoordinate_[2] = *fitWidth + x_;
    vertexCoordinate_[3] = *fitHeight + y_;
    vertexCoordinate_[4] = x_;
    vertexCoordinate_[5] = y_;
    vertexCoordinate_[6] = *fitWidth + x_;
    vertexCoordinate_[7] = y_;
  }
}