//
// Created by shell m1 on 2023/7/14.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_FILTER_MOSAICFILTER_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_FILTER_MOSAICFILTER_H_

#include "BaseFilter.h"

class MosaicFilter : public BaseFilter {

 private:
  std::string onBuildVertexShader() override;
  std::string onBuildFragmentShader() override;
  void onPrepareProgram(GLuint program) override;
  void onUpdateParams(const FilterSource *source, const FilterTarget *target) override;

 private:
  GLint textureSizeHandler = -1;
  GLint rectSizeHandler = -1;
};

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_FILTER_MOSAICFILTER_H_
