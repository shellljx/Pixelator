//
// Created by shell m1 on 2023/7/13.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_RENDER_RENDERER_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_RENDER_RENDERER_H_

#include "BaseFilter.h"
#include "RecordRenderer.h"
#include "GraffitiFilter.h"
#include "MosaicFilter.h"
#include "MatrixFilter.h"
#include "RectFilter.h"
#include "json/json.h"

class Renderer {
 public:
  explicit Renderer(RenderCallback *callback);
  ~Renderer();
  void setSurfaceChanged(int width, int height);
  void setMiniSurfaceChanged(int width, int height);
  void setBottomOffset(int offset);
  void setInputImage(GLuint texture, int width, int height);
  void setBrushImage(ImageInfo *image);
  void setMaskImage(ImageInfo *image);
  void setPaintType(int type);
  void setPaintMode(int mode);
  void setPaintSize(int size);
  void setMaskMode(int mode);
  void setCanvasHide(bool hide);
  void setEffect(Json::Value &root);
  void setTransformMatrix(const float *buffer);
  void startTouch(float x, float y);
  bool updateTouchBuffer(float *buffer, int length, float x, float y);
  void stopTouch();
  void undo();
  void redo();
  void drawScreen();
  void drawMiniScreen();
  FrameBuffer *getBlendFrameBuffer() const;
 private:
  void drawSourceTexture(GLuint texture, int width, int height);
  void drawPaint();
  void drawGraffitiPaint();
  void drawRectPaint();
  void drawMosaicEffect();
  void drawImageEffect(GLuint texture, int width, int height);
  void drawBlend();
  void blendTexture(GLuint texture, bool revert);
  void clearPaintCache();
  void notifyTransformChanged(bool reset = false);
  void notifyInitBoundChanged();
 private:
  RenderCallback *renderCallback = nullptr;
  glm::mat4 screenProjection = glm::mat4(1);
  glm::mat4 miniScreenProjection = glm::mat4(1);
  glm::mat4 paintProjection = glm::mat4(1);
  glm::mat4 viewMatrix = glm::mat4(1);
  glm::mat4 modelMatrix = glm::mat4(1);
  glm::mat4 transformMatrix = glm::mat4(1);
  std::shared_ptr<FrameBuffer> sourceFrameBuffer = nullptr;
  FrameBuffer *effectFrameBuffer;
  FrameBuffer *paintFrameBuffer;
  FrameBuffer *blendFrameBuffer;
  FrameBuffer *tempPaintFrameBuffer;
  FrameBuffer *undoRedoFrameBuffer;
  BaseFilter *defaultFilter;
  GraffitiFilter *graffitiFilter;
  MosaicFilter *mosaicFilter;
  MatrixFilter *matrixFilter;
  RectFilter *rectFilter;
  std::shared_ptr<RenderContext> renderContext = nullptr;
  std::shared_ptr<RecordRenderer> recordRender = nullptr;
  std::shared_ptr<ImageCache> imageCache;
  std::shared_ptr<Effect> currentEffect = nullptr;
  std::vector<float> touchSequences;
  int screenWidth = 0;
  int screenHeight = 0;
  int miniScreenWidth = 0;
  int miniScreenHeight = 0;
  int sourceWidth = 0;
  int sourceHeight = 0;
  int bottomOffset = 0;
  float touchStartX = 0.f;
  float touchStartY = 0.f;
  float touchX = 0.f;
  float touchY = 0.f;
};

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_RENDER_RENDERER_H_
