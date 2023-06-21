//
// Created by shell m1 on 2023/6/20.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_UTILS_IMAGEDECODER_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_UTILS_IMAGEDECODER_H_

#include <GLES3/gl3.h>

class ImageDecoder {
 public:
  ImageDecoder();
  ~ImageDecoder();

  int decodeImage(GLuint &texture, const char *path, int *width, int *height);
};

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_UTILS_IMAGEDECODER_H_
