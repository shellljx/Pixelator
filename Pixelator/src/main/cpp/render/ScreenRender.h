//
// Created by 李金祥 on 2023/4/23.
//

#ifndef PIXELATE_SCREENRENDER_H
#define PIXELATE_SCREENRENDER_H
#include <GLES3/gl3.h>
#include "glm/glm.hpp"
#include "glm/gtc/type_ptr.hpp"
#include "glm/gtc/matrix_transform.hpp"
#include "filter/BaseFilter.h"

class ScreenRender {
 public:
  ScreenRender();
  ~ScreenRender();

  void initMatrix(int screenWidth, int screenHeight, int textureWidth, int textureHeight);
  /**
 * 绘制一个纹理到 framebuffer
 * @param textureId 纹理 id
 * @param width 纹理宽
 * @param height 纹理高
 * @return 绘制之后的纹理
 */
  void draw(GLuint textureId, int width, int height, int screenWidth, int screenHeight);

  glm::mat4 getTransformMatrix() {
    return transformMatrix_ * modelMatrix_;
  }

  glm::mat4 getModelMatrix() {
    return modelMatrix_;
  }

  void translate(float scale);
  void setTransformMatrix(glm::mat4 matrix);
  void updateViewPort(int offset);
  void updateModelMatrix(int screenWidth,
                         int screenHeight,
                         int textureWidth,
                         int textureHeight);
 private:
  GLuint program_ = 0;
  float scale_ = 1.f;
  int offset_ = 0;
  float *vertexCoordinate_;
  BaseFilter* filter_;
  glm::mat4 projectionMatrix_ = glm::mat4(1);
  glm::mat4 viewMatrix_ = glm::mat4(1);
  glm::mat4 modelMatrix_ = glm::mat4(1);
  glm::mat4 transformMatrix_ = glm::mat4(1);
};

#endif //PIXELATE_SCREENRENDER_H
