//
// Created by 李金祥 on 2023/4/23.
//

#ifndef PIXELATE_PIXELATIONRENDER_H
#define PIXELATE_PIXELATIONRENDER_H

#include <GLES3/gl3.h>
#include "FrameBuffer.h"
#include "BaseEffectRender.h"

class PixelationRender : public BaseEffectRender {
 public:
  PixelationRender();

  ~PixelationRender();

  /**
   * 绘制一个纹理到 framebuffer
   * @param textureId 纹理 id
   * @param width 纹理宽
   * @param height 纹理高
   * @return 绘制之后的纹理
   */
  GLuint draw(GLuint textureId, int width, int height) override;

  int updateConfig(Json::Value &config) override;
};

#endif //PIXELATE_PIXELATIONRENDER_H
