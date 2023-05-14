//
// Created by 李金祥 on 2023/4/23.
//

#include "ScreenRender.h"
#include <memory>
#include "glm/glm.hpp"
#include "glm/gtc/type_ptr.hpp"
#include "glm/gtc/matrix_transform.hpp"

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

GLuint ScreenRender::draw(GLuint textureId, GLuint maskTexture, int width, int height, int screenWidth, int screenHeight) {
  if (frameBuffer_ == nullptr) {
    return -1;
  }
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
  glm::mat4 projection = glm::ortho(0.f, static_cast<float>(screenWidth),
                                    0.f, static_cast<float>(screenHeight), 1.f, 100.f);
  glm::vec3 position = glm::vec3(0.f, 0.f, 10.f);
  glm::vec3 direction = glm::vec3(0.f, 0.f, 0.f);
  glm::vec3 up = glm::vec3(0.f, 1.f, 0.f);
  glm::mat4 viewMatrix = glm::lookAt(position, direction, up);
  auto matrix = glm::mat4(1);
  float x = (screenWidth - width) / 2.f;
  float y = (screenHeight - height) / 2.f;
  matrix = glm::translate(matrix, glm::vec3(100.f, 100.f, 0.f));
  matrix = glm::translate(matrix, glm::vec3(x, y, 0.f));
  matrix = glm::translate(matrix, glm::vec3(width / 2.f, height / 2.f, 0.f));
  matrix = glm::scale(matrix, glm::vec3(2.5f, 2.5f, 1.f));
  matrix = glm::translate(matrix, glm::vec3(-width / 2.f, -height / 2.f, 0.f));
  GL_CHECK(glUseProgram(program_))
  auto positionLoction = glGetAttribLocation(program_, "position");
  GL_CHECK(glEnableVertexAttribArray(positionLoction))
  GL_CHECK(glVertexAttribPointer(positionLoction, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat),
                                 vertexCoordinate_))
  auto textureLocation = glGetAttribLocation(program_, "inputTextureCoordinate");
  GL_CHECK(glEnableVertexAttribArray(textureLocation))
  GL_CHECK(glVertexAttribPointer(textureLocation, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat),
                                 TEXTURE_COORDINATE_FLIP_UP_DOWN))
  matrix = projection * viewMatrix * matrix;
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