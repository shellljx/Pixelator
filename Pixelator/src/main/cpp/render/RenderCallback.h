//
// Created by shell m1 on 2023/7/19.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_RENDER_RENDERCALLBACK_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_RENDER_RENDERCALLBACK_H_
class RenderCallback {
 public:
  virtual void bindScreen() = 0;
  virtual void flushScreen() = 0;
  virtual void bindMiniScreen() = 0;
  virtual void flushMiniScreen() = 0;
  virtual void onTransformChanged(float left, float top, float right, float bottom, bool reset) = 0;
  virtual void onInitBoundChanged(float left, float top, float right, float bottom) = 0;
  virtual void onUndoRedoChanged(int undoSize, int redoSize) = 0;
  virtual void saveFrameBuffer(FrameBuffer *frameBuffer, int width, int height) = 0;
};

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_RENDER_RENDERCALLBACK_H_
