//
// Created by 李金祥 on 2023/4/23.
//

#include "PixelationRender.h"
#include "Log.h"

PixelationRender::PixelationRender() {
  frameBuffer_ = new FrameBuffer();
  program_ = Program::CreateProgram(DEFAULT_VERTEX_SHADER, PIXELATE_RECT_FRAGMENT_SHADER);
}

PixelationRender::~PixelationRender() {
  delete frameBuffer_;
  frameBuffer_ = nullptr;
  if (program_ > 0) {
    glDeleteProgram(program_);
    program_ = 0;
  }
}

GLuint PixelationRender::draw(GLuint textureId, int width, int height) {
  if (frameBuffer_ == nullptr) {
    LOGE("%s frame buffer is null", __func__);
    return -1;
  }
  frameBuffer_->createFrameBuffer(width, height);
  glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer_->getFrameBuffer());

  glViewport(0, 0, width, height);
  glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
  glUseProgram(program_);
  auto positionLoc = glGetAttribLocation(program_, "position");
  glEnableVertexAttribArray(positionLoc);
  glVertexAttribPointer(positionLoc,
                        2,
                        GL_FLOAT,
                        GL_FALSE,
                        2 * sizeof(GLfloat),
                        DEFAULT_VERTEX_COORDINATE);
  auto textureLoc = glGetAttribLocation(program_, "inputTextureCoordinate");
  glEnableVertexAttribArray(textureLoc);
  glVertexAttribPointer(textureLoc,
                        2,
                        GL_FLOAT,
                        GL_FALSE,
                        2 * sizeof(GLfloat),
                        DEFAULT_TEXTURE_COORDINATE);
  //更新纹理长宽
  auto textureSizeLoc = glGetUniformLocation(program_, "textureSize");
  float textureSize[] = {(float) width, (float) height};
  glUniform2fv(textureSizeLoc, 1, textureSize);
  //更新马赛克方块长宽
  auto rectSizeLoc = glGetUniformLocation(program_, "rectSize");
  float rectSize[] = {(float) 20, (float) 20};
  glUniform2fv(rectSizeLoc, 1, rectSize);
  //更新纹理单元0的纹理
  glActiveTexture(GL_TEXTURE0);
  glBindTexture(GL_TEXTURE_2D, textureId);
  auto inputTextureLoc = glGetUniformLocation(program_, "inputImageTexture");
  glUniform1i(inputTextureLoc, 0);
  glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
  glDisableVertexAttribArray(positionLoc);
  glDisableVertexAttribArray(textureLoc);
  glBindTexture(GL_TEXTURE_2D, 0);
  glBindFramebuffer(GL_FRAMEBUFFER, 0);
  return frameBuffer_->getTexture();
}

GLuint PixelationRender::getTexture() {
  if (frameBuffer_ == nullptr) {
    return -1;
  }
  return frameBuffer_->getTexture();
}