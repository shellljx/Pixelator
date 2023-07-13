//
// Created by shell m1 on 2023/7/11.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_FILTER_BASEFILTER_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_FILTER_BASEFILTER_H_

#include "Filter.h"
#include "GLUtils.h"

class BaseFilter : public Filter {
 public:
  virtual ~BaseFilter();
  bool initialize() override;
  void draw(const FilterSource *source, const FilterTarget *target) override;
  void enableBlend(GLenum sfactor, GLenum dfactor);
  void disableBlend();
 protected:
  GLuint program = 0;

  virtual std::string onBuildVertexShader();
  virtual std::string onBuildFragmentShader();
  virtual void onPrepareProgram(GLuint program);
  virtual void onPreDraw();
  virtual void onUpdateParams(const FilterSource *source, const FilterTarget *target);
  virtual void onRunDrawTask();
  virtual void onAfterDraw();

 private:
  bool isInited = false;
  GLint positionHandler = -1;
  int textureCoordinateHandler = -1;
  int mvpMatrixHandler = -1;
};

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_FILTER_BASEFILTER_H_
