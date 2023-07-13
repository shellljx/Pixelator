//
// Created by shell m1 on 2023/7/12.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_FILTER_MATRIXFILTER_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_FILTER_MATRIXFILTER_H_

#include "BaseFilter.h"

class MatrixFilter : public BaseFilter {
 public:
  ~MatrixFilter() = default;
 private:
  std::string onBuildVertexShader() override;
};

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_FILTER_MATRIXFILTER_H_
