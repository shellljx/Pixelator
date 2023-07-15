//
// Created by shell m1 on 2023/7/15.
//
#include "file.h"
ImageEffect::ImageEffect(EffectType type, const char *path) : Effect(type), srcPath(path) {
}

std::string ImageEffect::getSrcPath() {
  return srcPath;
}