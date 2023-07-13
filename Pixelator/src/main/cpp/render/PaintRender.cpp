//
// Created by 李金祥 on 2023/4/24.
//

#include "PaintRender.h"

PaintRender::PaintRender() : rectFrameBuffer_(nullptr) {
  frame_buffer_ = new FrameBuffer();
  paintProgram_ = Program::CreateProgram(PIXELATE_VERTEX_SHADER, BRUSH_FRAGMENT_SHADER);
  blendProgram_ = Program::CreateProgram(DEFAULT_VERTEX_SHADER, DEFAULT_FRAGMENT_SHADER);
  rectProgram_ = Program::CreateProgram(RECT_PAINT_VERTEX_SHADER, RECT_PAINT_FRAGMENT_SHADER);
  graffitiFilter = new GraffitiFilter();
  rectFilter = new RectFilter();
  blendFilter = new BaseFilter();
}

PaintRender::~PaintRender() {
  if (frame_buffer_ != nullptr) {
    delete frame_buffer_;
    frame_buffer_ = nullptr;
  }
  if (rectFrameBuffer_ != nullptr) {
    delete rectFrameBuffer_;
    rectFrameBuffer_ = nullptr;
  }
  if (blendProgram_ > 0) {
    glDeleteProgram(blendProgram_);
    blendProgram_ = 0;
  }
  if (paintProgram_ > 0) {
    glDeleteProgram(paintProgram_);
    paintProgram_ = 0;
  }
  if (rectProgram_ > 0) {
    glDeleteProgram(rectProgram_);
    rectProgram_ = 0;
  }
  if (brushTexture_ > 0) {
    glDeleteTextures(1, &brushTexture_);
    brushTexture_ = 0;
  }
  if (maskTexture_ > 0) {
    glDeleteTextures(1, &maskTexture_);
    maskTexture_ = 0;
  }
}

void PaintRender::setBrush(const ImageInfo *image) {
  graffitiFilter->updateBrushTexture(image);
}

void PaintRender::setDeeplabMask(const ImageInfo *image) {
  graffitiFilter->updateMaskTexture(image);
}

int PaintRender::processPushBufferInternal(float *buffer, int length) {
  graffitiFilter->updatePoints(buffer, length);
  return 0;
}

void PaintRender::setTouchStartPoint(float x, float y) {
  startTouchPointX_ = x;
  startTouchPointY_ = y;
}

void PaintRender::setCurrTouchPoint(float x, float y) {
  touchPointX_ = x;
  touchPointY_ = y;
}

GLuint PaintRender::draw(GLuint textureId, int width, int height) {
  glm::mat4 projection = glm::ortho(0.f, width * 1.f,
                                    height * 1.f, 0.f, 1.f, 100.f);
  glm::vec3 position = glm::vec3(0.f, 0.f, 10.f);
  glm::vec3 direction = glm::vec3(0.f, 0.f, 0.f);
  glm::vec3 up = glm::vec3(0.f, 1.f, 0.f);
  glm::mat4 viewMatrix = glm::lookAt(position, direction, up);
  auto matrix = projection * viewMatrix * glm::inverse(matrix_);

  if (paintType_ == Graffiti) {
    //绘制涂鸦
    drawGraffiti(matrix, textureId, width, height);
  } else if (paintType_ == Rect) {
    //绘制矩形
    drawRect(matrix, textureId, width, height);
  }

  glBindFramebuffer(GL_FRAMEBUFFER, GL_NONE);
  return frame_buffer_->getTexture();
}

void PaintRender::drawGraffiti(const glm::mat4 &matrix,
                               GLuint textureId,
                               int width,
                               int height) const {
  frame_buffer_->createFrameBuffer(width, height);
  graffitiFilter->initialize();
  graffitiFilter->updatePaintSize(paintSize_ * 1.7f / scale_);
  graffitiFilter->updateMaskMode(maskMode_);
  graffitiFilter->updatePaintMode(paintMode_);
  FilterSource source = {textureId, nullptr, width, height};
  FilterTarget target = {frame_buffer_, matrix, nullptr, width, height, false};
  graffitiFilter->draw(&source, &target);
}

void PaintRender::drawRect(const glm::mat4 &matrix, GLuint textureId, int width, int height) {
  if (rectFrameBuffer_ == nullptr) {
    rectFrameBuffer_ = new FrameBuffer();
  }
  rectFrameBuffer_->createFrameBuffer(width, height);

  float vertexCoordinate_[] = {
      0.f, height * 1.f, width * 1.f, height * 1.f, 0.f, 0.f, width * 1.f, 0.f
  };
  float textureCoordinate[] = {startTouchPointX_, startTouchPointY_, touchPointX_, touchPointY_};
  rectFilter->initialize();
  FilterSource source = {textureId, textureCoordinate, width, height};
  FilterTarget target = {rectFrameBuffer_, matrix, vertexCoordinate_, width, height, true};
  rectFilter->draw(&source, &target);
}

void PaintRender::finalApplyRect() {

  blendFilter->initialize();
  blendFilter->enableBlend(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
  FilterSource source = {rectFrameBuffer_->getTexture(), DEFAULT_TEXTURE_COORDINATE, 0, 0};
  FilterTarget target = {
      frame_buffer_, {}, DEFAULT_VERTEX_COORDINATE,
      frame_buffer_->getTextureWidth(), frame_buffer_->getTextureHeight(), false
  };
  blendFilter->draw(&source, &target);
  blendFilter->disableBlend();
}

GLuint PaintRender::getFrameBuffer() {
  if (rectFrameBuffer_ == nullptr) {
    return -1;
  }
  return rectFrameBuffer_->getFrameBuffer();
}

GLuint PaintRender::getTexture() {
  return frame_buffer_->getTexture();
}

void PaintRender::translate(float scale) {
  scale_ = scale;
}
void PaintRender::setPaintSize(int paintSize) {
  paintSize_ = paintSize;
}

int PaintRender::getPaintSize() {
  return paintSize_;
}

int PaintRender::getPaintMode() {
  return paintMode_;
}

void PaintRender::clear() {
  glBindFramebuffer(GL_FRAMEBUFFER, frame_buffer_->getFrameBuffer());
  glClearColor(0, 0, 0, 0);
  glClear(GL_COLOR_BUFFER_BIT);
  glBindFramebuffer(GL_FRAMEBUFFER, GL_NONE);
}
void PaintRender::setPaintMode(int paintMode) {
  paintMode_ = paintMode;
}

void PaintRender::setDeeplabMaskMode(int mode) {
  maskMode_ = mode;
}

void PaintRender::setPaintType(int type) {
  paintType_ = type;
}

GLuint PaintRender::getRectTexture() {
  if (rectFrameBuffer_ != nullptr && paintType_ == Rect) {
    return rectFrameBuffer_->getTexture();
  }
  return GL_NONE;
}

void PaintRender::stopTouch() {
  if (paintType_ == Rect) {
    finalApplyRect();
  }
}
