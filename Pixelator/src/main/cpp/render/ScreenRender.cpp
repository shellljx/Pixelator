//
// Created by 李金祥 on 2023/4/23.
//

#include "ScreenRender.h"
#include <memory>
#include "Program.h"
#include "filter/MatrixFilter.h"

ScreenRender::ScreenRender() : vertexCoordinate_(nullptr) {
  filter_ = new MatrixFilter();
  program_ = Program::CreateProgram(DEFAULT_MATRIX_VERTEX_SHADER, DEFAULT_FRAGMENT_SHADER);
  vertexCoordinate_ = new float[8];
  memcpy(vertexCoordinate_, DEFAULT_VERTEX_COORDINATE, sizeof(float) * 8);
}

ScreenRender::~ScreenRender() {
  if (program_ > 0) {
    glDeleteProgram(program_);
    program_ = 0;
  }
  if (vertexCoordinate_ != nullptr) {
    delete[] vertexCoordinate_;
    vertexCoordinate_ = nullptr;
  }
}

void ScreenRender::initMatrix(int screenWidth,
                              int screenHeight,
                              int textureWidth,
                              int textureHeight) {
  //这个投影矩阵反转是因为双指手势和view坐标系一致
  projectionMatrix_ = glm::ortho(0.f, static_cast<float>(screenWidth),
                                 static_cast<float>(screenHeight), 0.f, 1.f, 100.f);
  glm::vec3 position = glm::vec3(0.f, 0.f, 10.f);
  glm::vec3 direction = glm::vec3(0.f, 0.f, 0.f);
  glm::vec3 up = glm::vec3(0.f, 1.f, 0.f);
  viewMatrix_ = glm::lookAt(position, direction, up);
  updateModelMatrix(screenWidth, screenHeight, textureWidth, textureHeight);
}

void ScreenRender::draw(GLuint textureId,
                        int width,
                        int height,
                        int screenWidth,
                        int screenHeight) {

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

  if (screenWidth % 2 != 0 || screenHeight % 2 != 0) {
    GL_CHECK(glPixelStorei(GL_UNPACK_ALIGNMENT, 1));
  }
  auto matrix = projectionMatrix_ * viewMatrix_ * transformMatrix_ * modelMatrix_;
  filter_->initialize();
  FilterSource source = {textureId, DEFAULT_TEXTURE_COORDINATE_FLIP_DOWN_UP, width, height};
  FilterTarget target = {nullptr, matrix, vertexCoordinate_, screenWidth, screenHeight};
  filter_->draw(&source, &target);
}

void ScreenRender::translate(float scale) {
  scale_ = scale;
}

void ScreenRender::setTransformMatrix(glm::mat4 matrix) {
  transformMatrix_ = matrix;
}

void ScreenRender::updateModelMatrix(int screenWidth,
                                     int screenHeight,
                                     int textureWidth,
                                     int textureHeight) {

  auto viewPortHeight = screenHeight - offset_;
  float screenRatio = static_cast<float>(screenWidth) / static_cast<float>(viewPortHeight);
  float frameRatio = static_cast<float>(textureWidth) / static_cast<float>(textureHeight);
  float x = 0.f;
  float y = 0.f;
  if (frameRatio > screenRatio) {
    //frame比屏幕宽
    scale_ = screenWidth * 1.f / textureWidth;
    y = (viewPortHeight - textureHeight * scale_) / 2.f;
  } else {
    //frame比屏幕窄
    scale_ = viewPortHeight * 1.f / textureHeight;
    x = (screenWidth - textureWidth * scale_) / 2.f;
  }
  modelMatrix_ = glm::mat4(1);
  modelMatrix_ = glm::translate(modelMatrix_, glm::vec3(x, y, 0.f));
  modelMatrix_ = glm::scale(modelMatrix_, glm::vec3(scale_, scale_, 1.f));
}
void ScreenRender::updateViewPort(int offset) {
  offset_ = offset;
}
