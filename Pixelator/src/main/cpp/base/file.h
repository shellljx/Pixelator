//
// Created by shell m1 on 2023/7/15.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_BASE_FILE_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_BASE_FILE_H_
#include <string>
typedef enum {
  TypeMosaic = 0,
  TypeImage,
} EffectType;

class Effect {
 public:
  Effect(EffectType type);
  virtual ~Effect() = default;

  EffectType type() const;

 protected:
  EffectType effectType;
};

class ImageEffect : public Effect {
 public:
  ImageEffect(EffectType type, const char *path);
  ~ImageEffect() = default;
  std::string getSrcPath();
 private:
  std::string srcPath;
};

class MosaicEffect : public Effect {
 public:
  MosaicEffect(EffectType type, int rectSize);
  ~MosaicEffect() = default;
  int getRectSize();
 private:
  int rectSize;
};
#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_BASE_FILE_H_
