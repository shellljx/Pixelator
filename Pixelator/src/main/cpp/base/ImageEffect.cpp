//
// Created by shell m1 on 2023/7/15.
//
#include "file.h"
ImageEffect::ImageEffect(EffectType type, const char *path, GLuint texture, int width, int height) : Effect(type), srcPath(path), srcTexture(texture), width(width), height(height) {
}
ImageEffect::~ImageEffect() {
  if (srcTexture > 0) {
    glDeleteTextures(1, &srcTexture);
    srcTexture = 0;
  }
}
std::string ImageEffect::getSrcPath() {
  return srcPath;
}

GLuint ImageEffect::getTexture() const {
  return srcTexture;
}

int ImageEffect::getWidth() const {
  return width;
}

int ImageEffect::getHeight() const {
  return height;
}