//
// Created by shell m1 on 2023/7/13.
//

#include "Renderer.h"
#include "ImageDecoder.h"
#include "glm/gtc/type_ptr.hpp"

Renderer::Renderer() {
  sourceFrameBuffer = new FrameBuffer();
  effectFrameBuffer = new FrameBuffer();
  paintFrameBuffer = new FrameBuffer();
  blendFrameBuffer = new FrameBuffer();
  tempPaintFrameBuffer = new FrameBuffer();
  defaultFilter = new BaseFilter();
  graffitiFilter = new GraffitiFilter();
  mosaicFilter = new MosaicFilter();
  matrixFilter = new MatrixFilter();
  rectFilter = new RectFilter();
  glm::vec3 position = glm::vec3(0.f, 0.f, 10.f);
  glm::vec3 direction = glm::vec3(0.f, 0.f, 0.f);
  glm::vec3 up = glm::vec3(0.f, 1.f, 0.f);
  viewMatrix = glm::lookAt(position, direction, up);
}

Renderer::~Renderer() {
  delete sourceFrameBuffer;
}
void Renderer::setRenderCallback(RenderCallback *callback) {
  renderCallback = callback;
}

void Renderer::setSurfaceChanged(int width, int height) {
  LOGI("enter func %s", __func__);
  screenWidth = width;
  screenHeight = height;
  //屏幕投影矩阵上下反转是因为双指手势和view坐标系一致
  screenProjection = glm::ortho(0.f, static_cast<float>(screenWidth),
                                static_cast<float>(screenHeight), 0.f, 1.f, 100.f);
}

void Renderer::setMiniSurfaceChanged(int width, int height) {
  miniScreenWidth = width;
  miniScreenHeight = height;
  miniScreenProjection = glm::ortho(0.f, width * 1.f, height * 1.f, 0.f, 1.f, 100.f);
}

void Renderer::setBottomOffset(int offset) {
  LOGI("enter func %s", __func__);
  bottomOffset = offset;
  modelMatrix = getCenterInsideMatrix(screenWidth, screenHeight, sourceWidth, sourceHeight, bottomOffset);
  notifyInitBoundChanged();
  notifyTransformChanged();
  drawScreen();
}

void Renderer::setInputImage(GLuint texture, int width, int height) {
  LOGI("enter func %s, width: %d, height: %d", __func__, width, height);
  //todo 长宽要根据原图缩放一个合适的比例
  drawSourceTexture(texture, width, height);
  sourceWidth = sourceFrameBuffer->getTextureWidth();
  sourceHeight = sourceFrameBuffer->getTextureHeight();
  paintProjection = glm::ortho(0.f, width * 1.f, height * 1.f, 0.f, 1.f, 100.f);
  modelMatrix = getCenterInsideMatrix(screenWidth, screenHeight, sourceWidth, sourceHeight, bottomOffset);
  clearPaintCache();
  notifyTransformChanged(true);
  drawBlend();
  drawScreen();
}

void Renderer::setBrushImage(ImageInfo *image) {
  LOGI("enter func %s", __func__);
  graffitiFilter->updateBrushTexture(image);
}

void Renderer::setMaskImage(ImageInfo *image) {
  LOGI("enter func %s", __func__);
  graffitiFilter->updateMaskTexture(image);
}

void Renderer::setPaintType(int type) {
  LOGI("enter func %s", __func__);
  paintType = type;
  if (paintType == Graffiti) {
    tempPaintFrameBuffer->deleteFrameBuffer();
  }
}

void Renderer::setPaintMode(int mode) {
  LOGI("enter func %s", __func__);
  paintMode = mode;
}

void Renderer::setPaintSize(int size) {
  LOGI("enter func %s", __func__);
  paintSize = size;
}

void Renderer::setMaskMode(int mode) {
  LOGI("enter func %s", __func__);
  maskMode = mode;
}

void Renderer::setEffect(Json::Value &root) {
  LOGI("enter func %s", __func__);
  if (root["type"].isNull() || root["config"].isNull()) {
    LOGE("%s json %s is not effect", __func__, root.asCString());
    return;
  }
  int type = root["type"].asInt();
  auto config = root["config"];
  if (type == TypeMosaic) {
    int rectSize = config["rectSize"].asInt();
    delete effect;
    effect = new MosaicEffect(TypeMosaic, rectSize);
    drawMosaicEffect();
  } else if (type == TypeImage) {
    if (config["url"].isNull()) {
      LOGE("json % is not image effect", config.asCString());
      return;
    }
    auto path = config["url"].asCString();
    delete effect;
    effect = new ImageEffect(TypeImage, path);
    ImageDecoder decoder;
    GLuint imageTexture = 0;
    int width = 0;
    int height = 0;
    auto ret = decoder.decodeImage(imageTexture, path, &width, &height);
    if (ret == 0 && width > 0 && height > 0) {
      drawImageEffect(imageTexture, width, height);
    }
    if (imageTexture > 0) {
      glDeleteTextures(1, &imageTexture);
    }
  }
}

void Renderer::setTransformMatrix(const float *buffer) {
  LOGI("enter func %s", __func__);
  transformMatrix = glm::make_mat4(buffer);
  notifyTransformChanged();
  drawScreen();
}

void Renderer::startTouch(float x, float y) {
  LOGI("enter func %s", __func__);
  touchStartX = x;
  touchStartY = y;
}

bool Renderer::updateTouchBuffer(float *buffer, int length, float x, float y) {
  LOGI("enter func %s", __func__);
  if (effectFrameBuffer->getFrameBuffer() == 0) {
    LOGI("%s not apply effect", __func__);
    return false;
  }
  touchX = x;
  touchY = y;
  graffitiFilter->updatePoints(buffer, length);
  //绘制
  drawPaint();
  drawBlend();
  drawScreen();
  drawMiniScreen();
  return true;
}

void Renderer::stopTouch() {
  if (tempPaintFrameBuffer->getTexture() > 0) {
    //如果有绘制的画笔缓存，在绘制完成时同步到画笔画布
    paintFrameBuffer->createFrameBuffer(sourceWidth, sourceHeight);
    defaultFilter->initialize();
    defaultFilter->enableBlend(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
    FilterSource source = {tempPaintFrameBuffer->getTexture(), DEFAULT_TEXTURE_COORDINATE, 0, 0};
    FilterTarget target = {
        paintFrameBuffer, {}, DEFAULT_VERTEX_COORDINATE, sourceWidth, sourceHeight, false
    };
    defaultFilter->draw(&source, &target);
    defaultFilter->disableBlend();
  }
}

void Renderer::drawSourceTexture(GLuint texture, int width, int height) {
  LOGI("enter func %s", __func__);
  sourceFrameBuffer->createFrameBuffer(width, height);
  defaultFilter->initialize();
  FilterSource source = {texture, DEFAULT_TEXTURE_COORDINATE};
  FilterTarget target = {sourceFrameBuffer, {}, DEFAULT_VERTEX_COORDINATE, width, height};
  defaultFilter->draw(&source, &target);
}

void Renderer::drawPaint() {
  if (paintType == Graffiti) {
    drawGraffitiPaint();
  } else if (paintType == Rect) {
    drawRectPaint();
  }
}

void Renderer::drawGraffitiPaint() {
  LOGI("enter func %s", __func__);
  if (sourceWidth <= 0 || sourceHeight <= 0) {
    LOGE("%s source size is 0", __func__);
    return;
  }
  auto model = transformMatrix * modelMatrix;
  paintFrameBuffer->createFrameBuffer(sourceWidth, sourceHeight);
  graffitiFilter->initialize();
  graffitiFilter->updatePaintSize(paintSize * 1.7f / model[0][0]);
  graffitiFilter->updateMaskMode(maskMode);
  graffitiFilter->updatePaintMode(paintMode);
  auto matrix = paintProjection * viewMatrix * glm::inverse(model);
  FilterSource source = {effectFrameBuffer->getTexture(), nullptr, sourceWidth, sourceHeight};
  FilterTarget target = {paintFrameBuffer, matrix, nullptr, sourceWidth, sourceHeight, false};
  graffitiFilter->draw(&source, &target);
}

void Renderer::drawRectPaint() {
  LOGI("enter func %s", __func__);
  float vertexCoordinate_[] = {
      0.f, sourceHeight * 1.f, sourceWidth * 1.f, sourceHeight * 1.f, 0.f, 0.f, sourceWidth * 1.f, 0.f
  };
  float textureCoordinate[] = {touchStartX, touchStartY, touchX, touchY};
  auto model = transformMatrix * modelMatrix;
  auto matrix = paintProjection * viewMatrix * glm::inverse(model);
  tempPaintFrameBuffer->createFrameBuffer(sourceWidth, sourceHeight);
  rectFilter->initialize();
  FilterSource source = {effectFrameBuffer->getTexture(), textureCoordinate};
  FilterTarget target = {tempPaintFrameBuffer, matrix, vertexCoordinate_, sourceWidth, sourceHeight, true};
  rectFilter->draw(&source, &target);
}

void Renderer::drawMosaicEffect() {
  LOGI("enter func %s", __func__);
  if (sourceWidth <= 0 || sourceHeight <= 0) {
    LOGE("%s source width or height is 0", __func__);
    return;
  }
  effectFrameBuffer->createFrameBuffer(sourceWidth, sourceHeight);
  mosaicFilter->initialize();
  //要用最新的纹理来生成马赛克
  FilterSource source = {blendFrameBuffer->getTexture(), nullptr};
  FilterTarget target = {effectFrameBuffer, {}, DEFAULT_VERTEX_COORDINATE, sourceWidth, sourceHeight};
  mosaicFilter->draw(&source, &target);
}

void Renderer::drawImageEffect(GLuint &texture, int width, int height) {
  LOGI("enter func %s", __func__);
  if (sourceWidth <= 0 || sourceHeight <= 0) {
    LOGE("%s source width or height is 0", __func__);
    return;
  }
  float textureCoordinate[9];
  textureCenterCrop(width, height, sourceWidth, sourceHeight, textureCoordinate);
  effectFrameBuffer->createFrameBuffer(sourceWidth, sourceHeight);
  defaultFilter->initialize();
  FilterSource source = {texture, textureCoordinate};
  FilterTarget target = {effectFrameBuffer, {}, DEFAULT_VERTEX_COORDINATE_FLIP_DOWN_UP, sourceWidth, sourceHeight};
  defaultFilter->draw(&source, &target);
}

void Renderer::drawBlend() {
  blendFrameBuffer->createFrameBuffer(sourceWidth, sourceHeight);
  blendTexture(sourceFrameBuffer->getTexture(), false);
  if (paintFrameBuffer->getTexture() > 0) {
    blendTexture(paintFrameBuffer->getTexture(), true);
  }
  if (tempPaintFrameBuffer->getTexture() > 0) {
    blendTexture(tempPaintFrameBuffer->getTexture(), true);
  }
}

void Renderer::blendTexture(GLuint texture, bool revert) {
  if (sourceWidth <= 0 || sourceHeight <= 0) {
    LOGE("%s source width or height is 0", __func__);
    return;
  }
  auto textureCoordinate = DEFAULT_TEXTURE_COORDINATE;
  if (revert) {
    textureCoordinate = DEFAULT_TEXTURE_COORDINATE_FLIP_DOWN_UP;
  }
  defaultFilter->initialize();
  defaultFilter->enableBlend(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
  FilterSource source = {texture, textureCoordinate, sourceWidth, sourceHeight};
  FilterTarget target = {blendFrameBuffer, {}, DEFAULT_VERTEX_COORDINATE, sourceWidth, sourceHeight, false};
  defaultFilter->draw(&source, &target);
  defaultFilter->disableBlend();
}

void Renderer::notifyTransformChanged(bool reset) {
  LOGI("enter func %s", __func__);
  if (renderCallback == nullptr || sourceWidth <= 0 || sourceHeight <= 0) {
    LOGE("%s source width or height is 0", __func__);
    return;
  }
  auto matrix = transformMatrix * modelMatrix;
  glm::vec4 lt = glm::vec4(0.f, 0.f, 0.f, 1.f);
  glm::vec4 rb = glm::vec4(sourceWidth, sourceHeight, 0.f, 1.f);
  lt = matrix * lt;
  rb = matrix * rb;
  renderCallback->onTransformChanged(lt.x, lt.y, rb.x, rb.y, reset);
}

void Renderer::notifyInitBoundChanged() {
  LOGI("enter func %s", __func__);
  if (renderCallback == nullptr || sourceWidth <= 0 || sourceHeight <= 0) {
    LOGE("%s source width or height is 0", __func__);
    return;
  }
  glm::vec4 lt = glm::vec4(0.f, 0.f, 0.f, 1.f);
  glm::vec4 rb = glm::vec4(sourceWidth, sourceHeight, 0.f, 1.f);
  lt = modelMatrix * lt;
  rb = modelMatrix * rb;
  renderCallback->onInitBoundChanged(lt.x, lt.y, rb.x, rb.y);
}

void Renderer::drawScreen() {
  LOGI("enter func %s", __func__);
  if (renderCallback != nullptr && sourceWidth > 0 && sourceHeight > 0) {
    renderCallback->bindScreen();
    float vertexCoordinate[9];
    getVertexCoordinate(sourceWidth, sourceHeight, vertexCoordinate);
    auto matrix = screenProjection * viewMatrix * transformMatrix * modelMatrix;
    matrixFilter->initialize();
    FilterSource source = {blendFrameBuffer->getTexture(), DEFAULT_TEXTURE_COORDINATE_FLIP_DOWN_UP, sourceWidth, sourceHeight};
    FilterTarget target = {nullptr, matrix, vertexCoordinate, screenWidth, screenHeight};
    matrixFilter->draw(&source, &target);
    renderCallback->flushScreen();
  }
}

void Renderer::drawMiniScreen() {
  if (renderCallback != nullptr && sourceWidth > 0 && sourceHeight > 0) {
    renderCallback->bindMiniScreen();
    float vertexCoordinate_[8] = {0.f, 0.f, sourceWidth * 1.f, 0.f, 0.f, sourceHeight * 1.f,
                                  sourceWidth * 1.f, sourceHeight * 1.f};
    auto model = glm::mat4(1);
    model = glm::translate(model, glm::vec3(-touchX, -touchY, 0.f));
    model = glm::translate(model, glm::vec3(miniScreenWidth / 2.f, miniScreenHeight / 2.f, 0.f));
    auto matrix = miniScreenProjection * viewMatrix * model * transformMatrix * modelMatrix;
    matrixFilter->initialize();
    FilterSource source = {blendFrameBuffer->getTexture(), DEFAULT_TEXTURE_COORDINATE};
    FilterTarget target = {nullptr, matrix, vertexCoordinate_, miniScreenWidth, miniScreenHeight};
    matrixFilter->draw(&source, &target);
    renderCallback->flushMiniScreen();
  }
}

FrameBuffer *Renderer::getBlendFrameBuffer() const {
  return blendFrameBuffer;
}

void Renderer::clearPaintCache() {
  transformMatrix = glm::mat4(1);
  paintFrameBuffer->deleteFrameBuffer();
  tempPaintFrameBuffer->deleteFrameBuffer();
  blendFrameBuffer->deleteFrameBuffer();
}