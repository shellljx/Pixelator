//
// Created by shell m1 on 2023/7/18.
//

#include "RecordRenderer.h"

RecordRenderer::RecordRenderer(std::shared_ptr<RenderContext> context,
                               std::shared_ptr<ImageCache> cache, RenderCallback *callback)
    : renderContext(std::move(context)), imageCache(std::move(cache)), renderCallback(callback) {
  cacheFrameBuffer = new FrameBuffer();
  effectFrameBuffer = new FrameBuffer();
  blendFrameBuffer = new FrameBuffer();
  defaultFilter = new BaseFilter();
  graffitiFilter = new GraffitiFilter();
  mosaicFilter = new MosaicFilter();
  rectFilter = new RectFilter();
}

RecordRenderer::~RecordRenderer() {
  delete cacheFrameBuffer;
  delete effectFrameBuffer;
  delete blendFrameBuffer;
  delete defaultFilter;
  delete mosaicFilter;
  delete rectFilter;
}

int RecordRenderer::push(std::shared_ptr<DrawRecord> record) {
  if (undoStack.size() >= 10) {
    int width = sourceFrameBuffer->getTextureWidth();
    int height = sourceFrameBuffer->getTextureHeight();
    cacheFrameBuffer->createFrameBuffer(width, height);
    persistentRecord(undoStack.begin()->get(), cacheFrameBuffer);
    undoStack.erase(undoStack.begin());
  }
  undoStack.push_back(std::move(record));
  redoStack.clear();
  return undoStack.size();
}

bool RecordRenderer::persistentRecord(DrawRecord *record, FrameBuffer *targetFb) {
  //首次绘制要把原图绘制上
  int width = sourceFrameBuffer->getTextureWidth();
  int height = sourceFrameBuffer->getTextureHeight();
  effectFrameBuffer->createFrameBuffer(width, height);
  if (record->effectType == TypeMosaic) {
    //合并原图和最新cache
    blendCache(targetFb);

    mosaicFilter->initialize();
    //要用最新的纹理来生成马赛克
    FilterSource source = {blendFrameBuffer->getTexture(), nullptr};
    FilterTarget target = {effectFrameBuffer, {}, DEFAULT_VERTEX_COORDINATE, width, height};
    mosaicFilter->draw(&source, &target);
  } else if (record->effectType == TypeImage) {
    //effect is image
    auto image = imageCache->get(record->srcPath);
    if (image == nullptr) {
      LOGE("%s image effect record cannot find image cache", __func__);
      return false;
    }
    float textureCoordinate[9];
    textureCenterCrop(image->getWidth(), image->getHeight(), width, height, textureCoordinate);
    defaultFilter->initialize();
    FilterSource source = {image->getTexture(), textureCoordinate};
    FilterTarget target = {effectFrameBuffer, {}, DEFAULT_VERTEX_COORDINATE_FLIP_DOWN_UP, width, height};
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
    FilterTarget target = {targetFb, record->matrix, nullptr, width, height, false};
    graffitiFilter->draw(&source, &target);
  } else if (record->effectType == Rect) {
    if (record->length < 4) {
      LOGE("%s record data length < 4, not rect effect");
      return false;
    }
    float vertexCoordinate_[] = {
        0.f, height * 1.f, width * 1.f, height * 1.f, 0.f, 0.f, width * 1.f, 0.f
    };
    auto data = record->data;
    float textureCoordinate[] = {data[0], data[1], data[2], data[3]};
    rectFilter->initialize();
    FilterSource source = {effectFrameBuffer->getTexture(), textureCoordinate};
    FilterTarget target = {targetFb, record->matrix, vertexCoordinate_, width, height, true};
    rectFilter->draw(&source, &target);
  }
  return true;
}

bool RecordRenderer::undo(FrameBuffer *paintFb) {
  if (!undoStack.empty()) {
    auto data = undoStack.back();
    undoStack.pop_back();
    redoStack.push_back(data);

    paintFb->clear();
    if (cacheFrameBuffer->getTexture() > 0) {
      renderCallback->saveFrameBuffer(cacheFrameBuffer, sourceFrameBuffer->getTextureWidth(),sourceFrameBuffer->getTextureHeight());
      defaultFilter->initialize();
      int sourceWidth = sourceFrameBuffer->getTextureWidth();
      int sourceHeight = sourceFrameBuffer->getTextureHeight();
      FilterSource source = {cacheFrameBuffer->getTexture(), DEFAULT_TEXTURE_COORDINATE};
      FilterTarget target = {paintFb, {}, DEFAULT_VERTEX_COORDINATE, sourceWidth, sourceHeight};
      defaultFilter->draw(&source, &target);
    }
    renderCallback->saveFrameBuffer(paintFb, sourceFrameBuffer->getTextureWidth(),sourceFrameBuffer->getTextureHeight());
    for (const auto &record : undoStack) {
      persistentRecord(record.get(), paintFb);
      renderCallback->saveFrameBuffer(paintFb,sourceFrameBuffer->getTextureWidth(),sourceFrameBuffer->getTextureHeight());
    }
    return true;
  }
  return false;
}

bool RecordRenderer::redo(FrameBuffer *paintFb) {
  if (!redoStack.empty()) {
    auto data = redoStack.back();
    redoStack.pop_back();
    auto ret = persistentRecord(data.get(), paintFb);
    if (ret) {
      undoStack.push_back(data);
    }
    return true;
  }
  return false;
}

void RecordRenderer::blendCache(FrameBuffer *targetFb) {
  int sourceWidth = sourceFrameBuffer->getTextureWidth();
  int sourceHeight = sourceFrameBuffer->getTextureHeight();
  blendFrameBuffer->createFrameBuffer(sourceWidth, sourceHeight);
  blendTexture(sourceFrameBuffer->getTexture(), false);
  if (targetFb->getTexture() > 0) {
    blendTexture(targetFb->getTexture(), true);
  }
}

void RecordRenderer::blendTexture(GLuint texture, bool revert) {
  auto textureCoordinate = DEFAULT_TEXTURE_COORDINATE;
  if (revert) {
    textureCoordinate = DEFAULT_TEXTURE_COORDINATE_FLIP_DOWN_UP;
  }
  int sourceWidth = sourceFrameBuffer->getTextureWidth();
  int sourceHeight = sourceFrameBuffer->getTextureHeight();
  defaultFilter->initialize();
  defaultFilter->enableBlend(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
  FilterSource source = {texture, textureCoordinate};
  FilterTarget target = {blendFrameBuffer, {}, DEFAULT_VERTEX_COORDINATE, sourceWidth, sourceHeight, false};
  defaultFilter->draw(&source, &target);
  defaultFilter->disableBlend();
}
void RecordRenderer::setSourceFrameBuffer(std::shared_ptr<FrameBuffer> fb) {
  sourceFrameBuffer = std::move(fb);
}

FrameBuffer *RecordRenderer::getFrameBuffer() {
  return cacheFrameBuffer;
}

int RecordRenderer::getUndoSize() {
  return undoStack.size();
}

int RecordRenderer::getRedoSize() {
  return redoStack.size();
}