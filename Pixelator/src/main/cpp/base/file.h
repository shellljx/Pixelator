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
  int effectType = 0;
  int maskMode = 0;
  int paintSize = 0;
  int mosaicSize = 120;
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
  ImageEffect(EffectType type, const char *path, GLuint texture, int width, int height);
  ~ImageEffect();
  std::string getSrcPath();
  GLuint getTexture() const;
  int getWidth() const;
  int getHeight() const;
 private:
  std::string srcPath;
  GLuint srcTexture;
  int width = 0;
  int height = 0;
};

class MosaicEffect : public Effect {
 public:
  MosaicEffect(EffectType type, int rectSize);
  ~MosaicEffect() = default;
  int getRectSize();
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

class ImageCache {
 public:
  explicit ImageCache() = default;
  ~ImageCache() = default;
  void add(std::shared_ptr<Effect> effect);
  std::shared_ptr<ImageEffect> get(const std::string &path);
 private:
  std::vector<std::shared_ptr<Effect>> cache;
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
