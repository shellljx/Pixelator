//
// Created by shell m1 on 2023/7/12.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_FILTER_GRAFFITIFILTER_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_FILTER_GRAFFITIFILTER_H_

#include "BaseFilter.h"
#include "ImageInfo.h"

class GraffitiFilter : public BaseFilter {
 public:
  ~GraffitiFilter();
  void updateBrush(GLuint texture);
  void updateMask(GLuint texture);
  void updatePoints(float *buffer, int length);
  void updatePaintSize(float size);
  void updateMaskMode(int mode);
  void updatePaintMode(int mode);

 private:
  std::string onBuildVertexShader() override;
  std::string onBuildFragmentShader() override;
  void onPrepareProgram(GLuint program) override;
  void onPreDraw() override;
  void onUpdateParams(const FilterSource *source, const FilterTarget *target) override;
  void onRunDrawTask() override;
  void onAfterDraw() override;

 private:
  GLuint brushTexture = 0;
  GLuint maskTexture = 0;
  GLuint vao = 0;
  GLuint pointsVbo = 0;
  GLint pointSizeHandler = -1;
  GLint textureSizeHandler = -1;
  GLint brushTextureHandler = -1;
  GLint maskTextureHandler = -1;
  GLint maskModeHandler = -1;
  int pointsCount = 0;
  float paintSize = 0.f;
  int maskMode = 0;
  int paintMode = 0;
};

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_FILTER_GRAFFITIFILTER_H_
