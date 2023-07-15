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

glm::mat4 getCenterInsideMatrix(int screenWidth, int screenHeight, int width, int height, int bottomOffset) {
  auto viewPortHeight = screenHeight - bottomOffset;
  float screenRatio = static_cast<float>(screenWidth) / static_cast<float>(viewPortHeight);
  float frameRatio = static_cast<float>(width) / static_cast<float>(height);
  float x = 0.f;
  float y = 0.f;
  float scale;
  if (frameRatio > screenRatio) {
    //frame比屏幕宽
    scale = screenWidth * 1.f / width;
    y = (viewPortHeight - height * scale) / 2.f;
  } else {
    //frame比屏幕窄
    scale = viewPortHeight * 1.f / height;
    x = (screenWidth - width * scale) / 2.f;
  }
  auto matrix = glm::mat4(1);
  matrix = glm::translate(matrix, glm::vec3(x, y, 0.f));
  matrix = glm::scale(matrix, glm::vec3(scale, scale, 1.f));
  return matrix;
}

void getVertexCoordinate(int width, int height, float *array) {
  if (array != nullptr) {
    array[0] = 0.f;
    array[1] = height;
    array[2] = width;
    array[3] = height;
    array[4] = 0.f;
    array[5] = 0.f;
    array[6] = width;
    array[7] = 0.f;
  }
}

void textureCenterCrop(int width, int height, int targetWidth, int targetHeight, float *array) {
  if (array != nullptr) {
    float targetRatio = targetWidth * 1.f / targetHeight;
    float ratio = width * 1.f / height;

    float finalWidth = 1.f;
    float finalHeight = 1.f;
    if (ratio > targetRatio) {
      finalWidth = height * 1.f / targetHeight * targetWidth / width;
    } else if (ratio < targetRatio) {
      finalHeight = width * 1.f / targetWidth * targetHeight / height;
    }
    array[0] = (1 - finalWidth) / 2;
    array[1] = (1 - finalHeight) / 2;
    array[2] = (1 - finalWidth) / 2 + finalWidth;
    array[3] = (1 - finalHeight) / 2;
    array[4] = (1 - finalWidth) / 2;
    array[5] = (1 - finalHeight) / 2 + finalHeight;
    array[6] = (1 - finalWidth) / 2 + finalWidth;
    array[7] = (1 - finalHeight) / 2 + finalHeight;
  }
}