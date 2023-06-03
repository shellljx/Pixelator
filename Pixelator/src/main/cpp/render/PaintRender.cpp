//
// Created by 李金祥 on 2023/4/24.
//

#include "PaintRender.h"

PaintRender::PaintRender() {
  frame_buffer_ = new FrameBuffer();
  program_ = Program::CreateProgram(PIXELATE_VERTEX_SHADER, BRUSH_FRAGMENT_SHADER);
}

PaintRender::~PaintRender() {
  if (frame_buffer_ != nullptr) {
    delete frame_buffer_;
    frame_buffer_ = nullptr;
  }
  if (program_ > 0) {
    glDeleteProgram(program_);
    program_ = 0;
  }
  if (brushTexture_ > 0) {
    glDeleteTextures(1, &brushTexture_);
    brushTexture_ = 0;
  }
}

void PaintRender::setBrush(const ImageInfo *image) {
  if (image == nullptr) {
    return;
  }
  if (image->pixels_ != nullptr) {
    if (brushTexture_ == 0) {
      glGenTextures(1, &brushTexture_);
      glBindTexture(GL_TEXTURE_2D, brushTexture_);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
      glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

      glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, image->width_, image->height_, 0,
                   GL_RGBA, GL_UNSIGNED_BYTE,
                   image->pixels_);
    } else {
      glBindTexture(GL_TEXTURE_2D, brushTexture_);
      glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, image->width_, image->height_,
                      GL_RGBA, GL_UNSIGNED_BYTE, image->pixels_);
    }
    glBindTexture(GL_TEXTURE_2D, 0);
  }
}

int PaintRender::processPushBufferInternal(float *buffer, int length) {
  if (vao_ == 0) {
    glGenVertexArrays(1, &vao_);
  }
  glBindVertexArray(vao_);
  points = length / 2;
  if (pointsVbo_ == 0) {
    glGenBuffers(1, &pointsVbo_);
    glBindBuffer(GL_ARRAY_BUFFER, pointsVbo_);
    glBufferData(GL_ARRAY_BUFFER, length * sizeof(float), buffer, GL_DYNAMIC_DRAW);
    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(float), nullptr);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glEnableVertexAttribArray(0);
  } else {
    glBindBuffer(GL_ARRAY_BUFFER, pointsVbo_);
    glBufferData(GL_ARRAY_BUFFER, length * sizeof(float), buffer, GL_DYNAMIC_DRAW);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
  }
  glBindVertexArray(0);
  return 0;
}

GLuint PaintRender::draw(GLuint textureId, int width, int height, int screenWidth, int screenHeight) {
  frame_buffer_->createFrameBuffer(width, height);
  glBindFramebuffer(GL_FRAMEBUFFER, frame_buffer_->getFrameBuffer());

  glm::mat4 projection = glm::ortho(0.f, width * 1.f,
                                    height * 1.f, 0.f, 1.f, 100.f);
  glm::vec3 position = glm::vec3(0.f, 0.f, 10.f);
  glm::vec3 direction = glm::vec3(0.f, 0.f, 0.f);
  glm::vec3 up = glm::vec3(0.f, 1.f, 0.f);
  glm::mat4 viewMatrix = glm::lookAt(position, direction, up);
//  auto matrix = glm::mat4(1);
//  float x = (screenWidth - width) / 2.f;
//  float y = (screenHeight - height) / 2.f;
//  matrix = glm::translate(matrix, glm::vec3(-translateX_ * (1 / scale_), -translateY_ * (1 / scale_), 0.f));
//  matrix = glm::translate(matrix, glm::vec3(-x * (1 / scale_), -y * (1 / scale_), 0.f));
//  matrix = glm::translate(matrix, glm::vec3(width / 2.f, height / 2.f, 0.f));
//  matrix = glm::scale(matrix, glm::vec3(1.f / scale_, 1.f / scale_, 1.f));
//  matrix = glm::translate(matrix, glm::vec3(-width / 2.f, -height / 2.f, 0.f));
  glViewport(0, 0, width, height);
  glEnable(GL_BLEND);
  glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
  glBlendEquation(GL_FUNC_ADD);
  glUseProgram(program_);
  auto matrix = projection * viewMatrix * glm::inverse(matrix_);
  auto mvpLoc = glGetUniformLocation(program_, "mvp");
  glUniformMatrix4fv(mvpLoc, 1, GL_FALSE, glm::value_ptr(matrix));

  auto pointSizeLocation = glGetUniformLocation(program_, "pointSize");
  glUniform1f(pointSizeLocation, paintSize_ * 1.7f / scale_);

  auto textureSizeLocation = glGetUniformLocation(program_, "textureSize");
  float textureSize[] = {(float) width, (float) height};
  glUniform2fv(textureSizeLocation, 1, textureSize);
  //绑定笔刷纹理到纹理单元1
  glActiveTexture(GL_TEXTURE1);
  glBindTexture(GL_TEXTURE_2D, brushTexture_);
  auto brushTextureLoc = glGetUniformLocation(program_, "brushTexture");
  glUniform1i(brushTextureLoc, 1);
  //绑定输入纹理到纹理单元0
  glActiveTexture(GL_TEXTURE0);
  glBindTexture(GL_TEXTURE_2D, textureId);
  auto inputTextureLoc = glGetUniformLocation(program_, "inputImageTexture");
  glUniform1i(inputTextureLoc, 0);
  glBindVertexArray(vao_);
  glDrawArrays(GL_POINTS, 0, points);
  glBindVertexArray(0);
  glBindTexture(GL_TEXTURE_2D, 0);
  glBindFramebuffer(GL_FRAMEBUFFER, 0);
  glDisable(GL_BLEND);
  return frame_buffer_->getTexture();
}

GLuint PaintRender::getTexture() {
  return frame_buffer_->getTexture();
}

void PaintRender::translate(float scale) {
  scale_ = scale;
}
void PaintRender::setPaintSize(int paintSize) {
  paintSize_ = paintSize;
}
