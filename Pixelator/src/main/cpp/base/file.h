//
// Created by shell m1 on 2023/7/15.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_BASE_FILE_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_BASE_FILE_H_

#include <string>
#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <GLES3/gl3.h>
#include <GLES3/gl3ext.h>
#include "glm.hpp"
#include <vector>

typedef enum {
  TypeMosaic = 0,
  TypeImage,
} EffectType;

typedef enum {
  Eraser = 0,
  Paint
} PaintMode;

typedef enum {
  Graffiti = 0,
  Rect
} PaintType;

struct RenderContext {
  GLuint maskTexture = 0;
  GLuint brushTexture = 0;
  int maskMode = 0;
  int paintSize = 0;
  int paintMode = Paint;
  int paintType = Graffiti;
  ~RenderContext() {
    if (maskTexture > 0) {
      glDeleteTextures(1, &maskTexture);
      maskTexture = 0;
    }
    if (brushTexture > 0) {
      glDeleteTextures(1, &brushTexture);
      brushTexture = 0;
    }
  }
};

struct UndoRedoContext {
  int effectType = -1;
  std::string srcPath;
};

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
  ~ImageEffect();
  std::string getSrcPath();
 private:
  std::string srcPath;
};

class MosaicEffect : public Effect {
 public:
  MosaicEffect(EffectType type, int rectSize);
  ~MosaicEffect() = default;
  int getRectSize() const;
 private:
  int rectSize;
};

struct DrawRecord {
  float *data = nullptr;
  int length = 0;
  glm::mat4 matrix;
  int effectType = 0;
  int paintType = 0;
  int paintSize = 0;
  float mosaicSize = 0.f;
  int paintMode = 1;
  int maskMode = 0;
  std::string srcPath;
  ~DrawRecord() {
    if (data != nullptr) {
      delete[] data;
      data = nullptr;
    }
  }
};

struct ImageTexture {
  std::string path;
  GLuint texture = 0;
  int width = 0;
  int height = 0;

  ~ImageTexture() {
    if (texture > 0) {
      glDeleteTextures(1, &texture);
      texture = 0;
    }
  }
};

class ImageCache {
 public:
  explicit ImageCache() = default;
  ~ImageCache() = default;
  std::shared_ptr<ImageTexture> add(const char *path, GLuint texture, int width, int height);
  std::shared_ptr<ImageTexture> get(const std::string &path);
 private:
  std::vector<std::shared_ptr<ImageTexture>> cache;
};
class DrawOp {
 public:
  DrawOp(std::shared_ptr<Effect> effect);
  virtual ~DrawOp() = default;
  void draw();
 protected:
  std::shared_ptr<Effect> cacheEffect = nullptr;
};

class GraffitiDrawOp : public DrawOp {
 public:
  GraffitiDrawOp(std::shared_ptr<Effect> effect);
  ~GraffitiDrawOp() = default;
 private:
};
#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_BASE_FILE_H_
