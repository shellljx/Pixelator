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
#include <vector>

class RecordRenderer {
 public:
  explicit RecordRenderer(std::shared_ptr<RenderContext> context, std::shared_ptr<ImageCache> cache);
  ~RecordRenderer();
  void saveCanvas(DrawRecord *record, FrameBuffer *sourceFb);
  FrameBuffer* getFrameBuffer();
 private:
  std::shared_ptr<RenderContext> renderContext;
  std::shared_ptr<ImageCache> imageCache;
  FrameBuffer *cacheFrameBuffer;
  FrameBuffer *effectFrameBuffer;
  BaseFilter *defaultFilter;
  GraffitiFilter *graffitiFilter;
  MosaicFilter *mosaicFilter;
  RectFilter *rectFilter;
};

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_RENDER_RECORDRENDERER_H_
