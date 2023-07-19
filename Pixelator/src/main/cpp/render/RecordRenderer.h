//
// Created by shell m1 on 2023/7/18.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_RENDER_RECORDRENDERER_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_RENDER_RECORDRENDERER_H_

#include "BaseFilter.h"
#include "MosaicFilter.h"
#include "RectFilter.h"
#include "GraffitiFilter.h"
#include "FrameBuffer.h"
#include "file.h"
#include "RenderCallback.h"
#include <vector>

class RecordRenderer {
 public:
  explicit RecordRenderer(std::shared_ptr<RenderContext> context, std::shared_ptr<ImageCache> cache, RenderCallback *callback);
  ~RecordRenderer();
  int push(std::shared_ptr<DrawRecord> record);
  bool persistentRecord(DrawRecord *record, FrameBuffer *targetFb);
  void setSourceFrameBuffer(std::shared_ptr<FrameBuffer> fb);
  bool undo(FrameBuffer *paintFb);
  bool redo(FrameBuffer *paintFb);
  FrameBuffer *getFrameBuffer();
  int getUndoSize();
  int getRedoSize();
 private:
  void blendCache(FrameBuffer *targetFb);
  void blendTexture(GLuint texture, bool revert);
 private:
  std::vector<std::shared_ptr<DrawRecord>> undoStack;
  std::vector<std::shared_ptr<DrawRecord>> redoStack;
  std::shared_ptr<RenderContext> renderContext;
  std::shared_ptr<ImageCache> imageCache;
  RenderCallback *renderCallback;
  std::shared_ptr<FrameBuffer> sourceFrameBuffer = nullptr;
  FrameBuffer *cacheFrameBuffer;
  FrameBuffer *effectFrameBuffer;
  FrameBuffer *blendFrameBuffer;
  BaseFilter *defaultFilter;
  GraffitiFilter *graffitiFilter;
  MosaicFilter *mosaicFilter;
  RectFilter *rectFilter;
};

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_RENDER_RECORDRENDERER_H_
