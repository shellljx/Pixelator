//
// Created by 李金祥 on 2023/4/23.
//

#ifndef PIXELATE_SCREENRENDER_H
#define PIXELATE_SCREENRENDER_H
#include <GLES3/gl3.h>
#include "glm/glm.hpp"
#include "glm/gtc/type_ptr.hpp"
#include "glm/gtc/matrix_transform.hpp"
#include "FrameBuffer.h"

class ScreenRender {
 public:
  ScreenRender();
  ~ScreenRender();

  /**
 * 绘制一个纹理到 framebuffer
 * @param textureId 纹理 id
 * @param width 纹理宽
 * @param height 纹理高
 * @return 绘制之后的纹理
 */
  GLuint draw(GLuint textureId, GLuint maskTexture, int width, int height, int screenWidth, int screenHeight);

  void drawTexture(GLuint textureId, int width, int height, int screenWidth, int screenHeight);

  GLuint getTexture();

  glm::mat4 getMatrix() {
    return matrix_;
  }

  int getFitWidth() {
    return fitWidth_;
  }

  int getFitHeight() {
    return fitHeight_;
  }

  int getX() {
    return x_;
  }

  int getY() {
    return y_;
  }

  void translate(float scale, float pivotX, float pivotY, float angle, float translateX, float translateY);
  void setMatrix(glm::mat4 matrix);
 private:
  void cropVertexCoordinate(int frameWidth, int frameHeight, int screenWidth, int screenHeight, int *fitWidth, int *fitHeight);
 private:
  GLuint program_ = 0;
  float scale_ = 1.f;
  float pivotX_ = 0.f;
  float pivotY_ = 0.f;
  float angle_ = 0.f;
  float translateX_ = 0.f;
  float translateY_ = 0.f;
  int fitWidth_ = 0;
  int fitHeight_ = 0;
  int x_ = 0;
  int y_ = 0;
  FrameBuffer *frameBuffer_;
  float *vertexCoordinate_;
  glm::mat4 matrix_ = glm::mat4(1);
};

#endif //PIXELATE_SCREENRENDER_H
