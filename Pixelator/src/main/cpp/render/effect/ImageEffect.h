//
// Created by shell m1 on 2023/6/19.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_RENDER_IMAGEEFFECT_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_RENDER_IMAGEEFFECT_H_

#include <GLES3/gl3.h>
#include "FrameBuffer.h"
#include "BaseEffectRender.h"

class ImageEffect : public BaseEffectRender {
 public:
  ImageEffect();
  ~ImageEffect();
  /**
 * 绘制一个纹理到 framebuffer
 * @param textureId 纹理 id
 * @param width 纹理宽
 * @param height 纹理高
 * @return 绘制之后的纹理
 */
  GLuint draw(GLuint textureId, int width, int height) override;

  int updateConfig(Json::Value &config) override;

 private:
  void cropImageTextureCoordinate(int targetWidth, int targetHeight);

 private:
  GLuint imageTexture_ = 0;
  float *textureCoordinate_;
  int width_ = 0;
  int height_ = 0;
};

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_RENDER_IMAGEEFFECT_H_
