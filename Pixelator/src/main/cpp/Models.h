//
// Created by shell m1 on 2023/6/3.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_MODELS_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_MODELS_H_

#include <detail/type_mat.hpp>
#include <detail/type_mat4x4.hpp>
#include <string>

typedef struct LineData {
  float *data = nullptr;
  int length = 0;
  glm::mat4 matrix = glm::mat4(1);
  int paintSize = 0;
  int paintMode = 1;
  std::string effectConfig;
} LineModel;

typedef enum {
  Eraser = 0,
  Paint
} PaintMode;

typedef enum {
  Graffiti = 0,
  Rect
} PaintType;
#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_MODELS_H_
