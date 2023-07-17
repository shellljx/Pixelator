//
// Created by shell m1 on 2023/6/20.
//

#include "ImageDecoder.h"
#include "ImageInfo.h"
#include "Log.h"
#include "BitmapUtils.h"
#define STB_IMAGE_IMPLEMENTATION
#include "stb_image.h"

ImageDecoder::ImageDecoder() {}

ImageDecoder::~ImageDecoder() {}

int ImageDecoder::decodeImage(GLuint &texture, const char *path, int *width, int *height) {

  ImageInfo *info = nullptr;
  auto ret = loadImageFromPath(path, &info);
  if (ret == 0 && info != nullptr) {
    *width = info->width_;
    *height = info->height_;
  } else {
    int channel = 0;
    auto data = stbi_load(path, width, height, &channel, STBI_rgb_alpha);
    if (*width == 0 || *height == 0 || data == nullptr) {
      LOGE("decode image error");
      delete[] data;
      return -1;
    }
    info = new ImageInfo(*width, *height, data);
  }
  if (texture > 0) {
    glDeleteTextures(1, &texture);
    texture = 0;
  }
  if (texture == 0) {
    glGenTextures(1, &texture);
  }
  if (*width % 2 != 0 || *height % 2 != 0) {
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
  }
  glActiveTexture(GL_TEXTURE0);
  glBindTexture(GL_TEXTURE_2D, texture);
  glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
  glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
  glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
  glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
  glTexImage2D(GL_TEXTURE_2D,
               0,
               GL_RGBA,
               *width,
               *height,
               0,
               GL_RGBA,
               GL_UNSIGNED_BYTE,
               info->pixels_);
  glBindTexture(GL_TEXTURE_2D, 0);
  delete info;
  return 0;
}