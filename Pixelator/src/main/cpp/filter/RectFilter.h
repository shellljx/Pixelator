//
// Created by shell m1 on 2023/7/13.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_FILTER_RECTFILTER_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_FILTER_RECTFILTER_H_

#include "BaseFilter.h"

class RectFilter : public BaseFilter {

 public:
  void updatePoint(float startX, float startY, float endX, float endY);
  void updateMaskMode(int mode);
  void updateMaskTexture(GLuint texture);
 private:
  std::string onBuildVertexShader() override;
  std::string onBuildFragmentShader() override;
  void onPrepareProgram(GLuint program) override;
  void onUpdateParams(const FilterSource *source, const FilterTarget *target) override;
 private:
  GLint textureSizeHandler = -1;
  GLint startPointHandler = -1;
  GLint endPointHandler = -1;
  GLint maskModeHandler = -1;
  GLint maskHandler = -1;

  GLuint maskTexture = 0;
  int maskMode = 0;
  float startx = 0.f;
  float starty = 0.f;
  float endx = 0.f;
  float endy = 0.f;
};

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_FILTER_RECTFILTER_H_
