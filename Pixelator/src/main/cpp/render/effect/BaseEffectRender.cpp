//
// Created by shell m1 on 2023/6/20.
//

#include "BaseEffectRender.h"

BaseEffectRender::BaseEffectRender(const char *vertexShader, const char *fragShader) {
  frameBuffer_ = new FrameBuffer();
  program_ = Program::CreateProgram(vertexShader, fragShader);
}

BaseEffectRender::~BaseEffectRender() {
  delete frameBuffer_;
  frameBuffer_ = nullptr;
  if (program_ > 0) {
    glDeleteProgram(program_);
    program_ = 0;
  }
}

GLuint BaseEffectRender::getTexture() {
  return frameBuffer_->getTexture();
}