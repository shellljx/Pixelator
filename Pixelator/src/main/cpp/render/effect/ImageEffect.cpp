//
// Created by shell m1 on 2023/6/19.
//

#include "ImageEffect.h"
#include "ImageDecoder.h"

ImageEffect::ImageEffect() : BaseEffectRender(DEFAULT_VERTEX_SHADER, DEFAULT_FRAGMENT_SHADER) {
  textureCoordinate_ = new float[8];
  memcpy(textureCoordinate_, DEFAULT_TEXTURE_COORDINATE, sizeof(float) * 8);
}

ImageEffect::~ImageEffect() {
  if (imageTexture_ > 0) {
    glDeleteTextures(1, &imageTexture_);
    imageTexture_ = 0;
  }
  if (textureCoordinate_ != nullptr) {
    delete[] textureCoordinate_;
    textureCoordinate_ = nullptr;
  }
}

GLuint ImageEffect::draw(GLuint textureId, int width, int height) {
  //这里传过来的 textureId 没有用，width，height 是 source 的长宽，图片切换长宽发生变化更新 framebuffer 的长宽
  frameBuffer_->createFrameBuffer(width, height);
  //裁剪特效图的纹理坐标，绘制到和 source 图长宽一样的纹理上
  cropImageTextureCoordinate(width, height);
  glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer_->getFrameBuffer());
  glViewport(0, 0, width, height);
  glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
  glUseProgram(program_);

  auto positionLoc = glGetAttribLocation(program_, "position");
  glEnableVertexAttribArray(positionLoc);
  glVertexAttribPointer(positionLoc, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat),
                        DEFAULT_VERTEX_COORDINATE_FLIP_DOWN_UP);
  auto textureCoordinateLoc = glGetAttribLocation(program_, "inputTextureCoordinate");
  glEnableVertexAttribArray(textureCoordinateLoc);
  glVertexAttribPointer(textureCoordinateLoc, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat),
                        textureCoordinate_);
  auto inputTextureLoc = glGetUniformLocation(program_, "inputImageTexture");
  glActiveTexture(GL_TEXTURE0);
  glBindTexture(GL_TEXTURE_2D, imageTexture_);
  glUniform1i(inputTextureLoc, 0);
  glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
  glDisableVertexAttribArray(positionLoc);
  glDisableVertexAttribArray(textureCoordinateLoc);
  glBindTexture(GL_TEXTURE_2D, GL_NONE);
  glBindFramebuffer(GL_FRAMEBUFFER, GL_NONE);

  return frameBuffer_->getTexture();
}

int ImageEffect::updateConfig(Json::Value &config) {
  if (config["url"].isNull()) {
    LOGE("image effect not have image url config");
    return -1;
  }
  std::string url = config["url"].asCString();
  ImageDecoder imageDecoder;
  auto ret = imageDecoder.decodeImage(imageTexture_, url.c_str(), &width_, &height_);
  if (ret != 0) {
    LOGE("image effect decode image error %d", ret);
    return ret;
  }
  return 0;
}

void ImageEffect::cropImageTextureCoordinate(int targetWidth, int targetHeight) {
  float targetRatio = targetWidth * 1.f / targetHeight;
  float ratio = width_ * 1.f / height_;

  float finalWidth = 1.f;
  float finalHeight = 1.f;
  if (ratio > targetRatio) {
    finalWidth = height_ * 1.f / targetHeight * targetWidth / width_;
  } else if (ratio < targetRatio) {
    finalHeight = width_ * 1.f / targetWidth * targetHeight / height_;
  }
  textureCoordinate_[0] = (1 - finalWidth) / 2;
  textureCoordinate_[1] = (1 - finalHeight) / 2;
  textureCoordinate_[2] = (1 - finalWidth) / 2 + finalWidth;
  textureCoordinate_[3] = (1 - finalHeight) / 2;
  textureCoordinate_[4] = (1 - finalWidth) / 2;
  textureCoordinate_[5] = (1 - finalHeight) / 2 + finalHeight;
  textureCoordinate_[6] = (1 - finalWidth) / 2 + finalWidth;
  textureCoordinate_[7] = (1 - finalHeight) / 2 + finalHeight;
}