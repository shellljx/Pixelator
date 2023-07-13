//
// Created by shell m1 on 2023/7/11.
//

#include "GLUtils.h"

void activeGLTexture(int unitIndex, GLenum target, GLuint texture) {
  glActiveTexture(GL_TEXTURE0 + unitIndex);
  glBindTexture(target, texture);
  glTexParameteri(target, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
  glTexParameteri(target, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
  glTexParameteri(target, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
  glTexParameteri(target, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
}

bool CheckGLError() {
  bool success = true;
  unsigned errorCode;
  while ((errorCode = glGetError()) != GL_NO_ERROR) {
    success = false;
    LOGE("glCheckError: %d", errorCode);
  }
  return success;
}