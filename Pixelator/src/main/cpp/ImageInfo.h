//
// Created by 李金祥 on 2023/1/29.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_IMAGEINFO_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_IMAGEINFO_H_

class ImageInfo {
 public:
  ImageInfo(int width, int height, unsigned char *pixels) : width_(width), height_(height), pixels_(pixels) {}
  ~ImageInfo() {
    if (pixels_ != nullptr) {
      delete[] pixels_;
      pixels_ = nullptr;
    }
  };

 private:
  int width_ = 0;
  int height_ = 0;
  unsigned char *pixels_ = nullptr;
};

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_IMAGEINFO_H_
