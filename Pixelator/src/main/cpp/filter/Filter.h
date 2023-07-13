//
// Created by shell m1 on 2023/7/11.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_FILTER_FILTER_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_FILTER_FILTER_H_

#include "EGLCore.h"
#include "FrameBuffer.h"
#include <string>
#include <glm.hpp>

struct FilterSource {
  GLuint texture = GL_NONE;
  float *coordinate = nullptr;
  int width;
  int height;
};
struct FilterTarget {
  FrameBuffer *frameBuffer = nullptr;
  glm::mat4 vertexMatrix = {};
  float *vertexs = nullptr;
  int width;
  int height;
  bool clear = true;
};

class Filter {
 public:
  virtual ~Filter() = default;

  virtual bool initialize() = 0;

  virtual void draw(const FilterSource *source, const FilterTarget *target) = 0;

  virtual bool needsMSAA() const {
    return false;
  }
};
#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_FILTER_FILTER_H_
