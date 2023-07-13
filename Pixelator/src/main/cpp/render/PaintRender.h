//
// Created by 李金祥 on 2023/4/24.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_RENDER_PAINTRENDER_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_RENDER_PAINTRENDER_H_

#include <GLES3/gl3.h>
#include <stack>
#include "glm/glm.hpp"
#include "glm/gtc/type_ptr.hpp"
#include "glm/gtc/matrix_transform.hpp"
#include "FrameBuffer.h"
#include "ImageInfo.h"
#include "Models.h"
#include "filter/GraffitiFilter.h"
#include "filter/RectFilter.h"

class PaintRender {
 public:
  PaintRender();
  ~PaintRender();

  void setBrush(const ImageInfo *image);

  void setDeeplabMask(const ImageInfo *image);

  int processPushBufferInternal(float *buffer, int length);

  void setTouchStartPoint(float x, float y);

  void setCurrTouchPoint(float x, float y);
  /**
 * 绘制一个纹理到 framebuffer
 * @param textureId 纹理 id
 * @param width 纹理宽
 * @param height 纹理高
 * @return 绘制之后的纹理
 */
  GLuint draw(GLuint textureId, int width, int height);

  GLuint getTexture();

  GLuint getFrameBuffer();
  GLuint getRectTexture();
  void translate(float scale);
  void setMatrix(glm::mat4 matrix) {
    matrix_ = matrix;
  }

  void setPaintSize(int paintSize);
  int getPaintSize();
  void clear();
  void setPaintMode(int paintMode);
  int getPaintMode();
  void setDeeplabMaskMode(int mode);
  void stopTouch();
  void setPaintType(int type);
 private:
  void drawGraffiti(const glm::mat4 &matrix, GLuint textureId, int width, int height) const;
  void drawRect(const glm::mat4 &matrix, GLuint textureId, int width, int height);
  void finalApplyRect();
 private:
  FrameBuffer *frame_buffer_;
  FrameBuffer *rectFrameBuffer_;
  GLuint brushTexture_ = 0;
  GLuint maskTexture_ = 0;
  GLuint paintProgram_ = 0;
  GLuint blendProgram_ = 0;
  GLuint rectProgram_ = 0;
  GLuint vao_ = 0;
  GLuint pointsVbo_ = 0;
  int maskMode_ = 0;
  int points = 0;
  float scale_ = 1.f;
  int paintSize_ = 0.f;
  int paintMode_ = Paint;
  int paintType_ = Graffiti;
  float startTouchPointX_ = 0;
  float startTouchPointY_ = 0;
  float touchPointX_ = 0;
  float touchPointY_ = 0;
  glm::mat4 matrix_ = glm::mat4(1);
  GraffitiFilter *graffitiFilter;
  RectFilter *rectFilter;
  BaseFilter *blendFilter;
};

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_RENDER_PAINTRENDER_H_
