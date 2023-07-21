//
// Created by shell m1 on 2023/7/13.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_FILTER_RECTFILTER_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_FILTER_RECTFILTER_H_

#include "BaseFilter.h"

class RectFilter : public BaseFilter {

 public:
  void updatePoint(float startX, float startY, float endX, float endY);

 private:
  std::string onBuildVertexShader() override;
  std::string onBuildFragmentShader() override;
  void onPrepareProgram(GLuint program) override;
  void onUpdateParams(const FilterSource *source, const FilterTarget *target) override;
 private:
  GLint textureSizeHandler = -1;
  GLint startPointHandler = -1;
  GLint endPointHandler = -1;

  float startx = 0.f;
  float starty = 0.f;
  float endx = 0.f;
  float endy = 0.f;
};

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_FILTER_RECTFILTER_H_
