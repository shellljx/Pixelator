//
// Created by shell m1 on 2023/6/20.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_RENDER_EFFECT_BASEEFFECTRENDER_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_RENDER_EFFECT_BASEEFFECTRENDER_H_

#include <GLES3/gl3.h>
#include "json/json.h"
#include "FrameBuffer.h"

class BaseEffectRender {
 public:
  explicit BaseEffectRender(const char *vertexShader, const char *fragShader);
  virtual ~BaseEffectRender();
  /**
 * 绘制一个纹理到 framebuffer
 * @param textureId 纹理 id
 * @param width 纹理宽
 * @param height 纹理高
 * @return 绘制之后的纹理
 */
  virtual GLuint draw(GLuint textureId, int width, int height) = 0;

  virtual int updateConfig(Json::Value &config) = 0;

  GLuint getTexture();

 protected:
  GLuint program_ = 0;
  FrameBuffer *frameBuffer_ = nullptr;
};

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_RENDER_EFFECT_BASEEFFECTRENDER_H_
