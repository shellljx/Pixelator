//
// Created by shell m1 on 2023/7/18.
//

#include "RecordRenderer.h"

RecordRenderer::RecordRenderer(std::shared_ptr<RenderContext> context, std::shared_ptr<ImageCache> cache)
    : renderContext(std::move(context)), imageCache(std::move(cache)) {
  cacheFrameBuffer = new FrameBuffer();
  effectFrameBuffer = new FrameBuffer();
  defaultFilter = new BaseFilter();
  graffitiFilter = new GraffitiFilter();
  mosaicFilter = new MosaicFilter();
  rectFilter = new RectFilter();
}

RecordRenderer::~RecordRenderer() {
  delete cacheFrameBuffer;
  delete effectFrameBuffer;
  delete defaultFilter;
  delete mosaicFilter;
  delete rectFilter;
}

void RecordRenderer::saveCanvas(DrawRecord *record, FrameBuffer *sourceFb) {
  //首次绘制要把原图绘制上
  bool isFirst = cacheFrameBuffer->getFrameBuffer() == 0;
  int width = sourceFb->getTextureWidth();
  int height = sourceFb->getTextureHeight();
  cacheFrameBuffer->createFrameBuffer(width, height);
  if (isFirst) {
    defaultFilter->initialize();
    FilterSource source = {sourceFb->getTexture(), DEFAULT_TEXTURE_COORDINATE_FLIP_DOWN_UP};
    FilterTarget target = {cacheFrameBuffer, {}, DEFAULT_VERTEX_COORDINATE, width, height};
    defaultFilter->draw(&source, &target);
  }
  effectFrameBuffer->createFrameBuffer(width, height);
  if (record->effectType == TypeMosaic) {
    //effect is cache
    mosaicFilter->initialize();
    //要用最新的纹理来生成马赛克
    FilterSource source = {cacheFrameBuffer->getTexture(), nullptr};
    FilterTarget target = {effectFrameBuffer, {}, DEFAULT_VERTEX_COORDINATE, width, height};
    mosaicFilter->draw(&source, &target);
  } else if (record->effectType == TypeImage) {
    //effect is image
    auto image = imageCache->get(record->srcPath);
    if (image == nullptr) {
      LOGE("%s image effect record cannot find image cache", __func__);
      return;
    }
    defaultFilter->initialize();
    FilterSource source = {image->getTexture(), DEFAULT_TEXTURE_COORDINATE_FLIP_DOWN_UP};
    FilterTarget target = {effectFrameBuffer, {}, DEFAULT_VERTEX_COORDINATE, width, height};
    defaultFilter->draw(&source, &target);
  }
  if (record->paintType == Graffiti) {
    graffitiFilter->initialize();
    graffitiFilter->updatePoints(record->data, record->length);
    graffitiFilter->updateBrush(renderContext->brushTexture);
    graffitiFilter->updateMask(renderContext->maskTexture);
    graffitiFilter->updatePaintSize(record->paintSize);
    graffitiFilter->updateMaskMode(record->maskMode);
    graffitiFilter->updatePaintMode(record->paintMode);
    FilterSource source = {effectFrameBuffer->getTexture(), nullptr};
    FilterTarget target = {cacheFrameBuffer, record->matrix, nullptr, width, height, false};
    graffitiFilter->draw(&source, &target);
  } else if (record->effectType == Rect) {
    if (record->length < 4) {
      LOGE("%s record data length < 4, not rect effect");
      return;
    }
    float vertexCoordinate_[] = {
        0.f, height * 1.f, width * 1.f, height * 1.f, 0.f, 0.f, width * 1.f, 0.f
    };
    auto data = record->data;
    float textureCoordinate[] = {data[0], data[1], data[2], data[3]};
    rectFilter->initialize();
    FilterSource source = {effectFrameBuffer->getTexture(), textureCoordinate};
    FilterTarget target = {cacheFrameBuffer, record->matrix, vertexCoordinate_, width, height, true};
    rectFilter->draw(&source, &target);
  }
}

FrameBuffer *RecordRenderer::getFrameBuffer() {
  return cacheFrameBuffer;
}