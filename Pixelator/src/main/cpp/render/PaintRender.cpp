//
// Created by 李金祥 on 2023/4/24.
//

#include "PaintRender.h"

PaintRender::PaintRender() : rectFrameBuffer_(nullptr) {
  frame_buffer_ = new FrameBuffer();
  paintProgram_ = Program::CreateProgram(PIXELATE_VERTEX_SHADER, BRUSH_FRAGMENT_SHADER);
  blendProgram_ = Program::CreateProgram(DEFAULT_VERTEX_SHADER, DEFAULT_FRAGMENT_SHADER);
  rectProgram_ = Program::CreateProgram(RECT_PAINT_VERTEX_SHADER, RECT_PAINT_FRAGMENT_SHADER);
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
  if (image == nullptr) {
    return;
  }
  if (image->pixels_ != nullptr) {
    if (brushTexture_ == 0) {
      glGenTextures(1, &brushTexture_);
      glBindTexture(GL_TEXTURE_2D, brushTexture_);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
      glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

      glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, image->width_, image->height_, 0,
                   GL_RGBA, GL_UNSIGNED_BYTE,
                   image->pixels_);
    } else {
      glBindTexture(GL_TEXTURE_2D, brushTexture_);
      glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, image->width_, image->height_,
                      GL_RGBA, GL_UNSIGNED_BYTE, image->pixels_);
    }
    glBindTexture(GL_TEXTURE_2D, 0);
  }
}

void PaintRender::setDeeplabMask(const ImageInfo *image) {
  if (image == nullptr) {
    return;
  }
  if (image->pixels_ != nullptr) {
    if (maskTexture_ == 0) {
      glGenTextures(1, &maskTexture_);
      glBindTexture(GL_TEXTURE_2D, maskTexture_);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
      glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

      glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, image->width_, image->height_, 0,
                   GL_RGBA, GL_UNSIGNED_BYTE,
                   image->pixels_);
    } else {
      glBindTexture(GL_TEXTURE_2D, maskTexture_);
      glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, image->width_, image->height_,
                      GL_RGBA, GL_UNSIGNED_BYTE, image->pixels_);
    }
    glBindTexture(GL_TEXTURE_2D, 0);
  }
}

int PaintRender::processPushBufferInternal(float *buffer, int length) {
  if (vao_ == 0) {
    glGenVertexArrays(1, &vao_);
  }
  glBindVertexArray(vao_);
  points = length / 2;
  if (pointsVbo_ == 0) {
    glGenBuffers(1, &pointsVbo_);
    glBindBuffer(GL_ARRAY_BUFFER, pointsVbo_);
    glBufferData(GL_ARRAY_BUFFER, length * sizeof(float), buffer, GL_DYNAMIC_DRAW);
    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(float), nullptr);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glEnableVertexAttribArray(0);
  } else {
    glBindBuffer(GL_ARRAY_BUFFER, pointsVbo_);
    glBufferData(GL_ARRAY_BUFFER, length * sizeof(float), buffer, GL_DYNAMIC_DRAW);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
  }
  glBindVertexArray(0);
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
  glViewport(0, 0, width, height);
  glEnable(GL_BLEND);
  if (paintMode_ == 1) {
    glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
  } else {
    glBlendFunc(GL_ZERO, GL_ONE_MINUS_SRC_ALPHA);
  }
  glBlendEquation(GL_FUNC_ADD);
  auto matrix = projection * viewMatrix * glm::inverse(matrix_);

  if (paintType_ == Graffiti) {
    //绘制涂鸦
    drawGraffiti(matrix, textureId, width, height);
  } else if (paintType_ == Rect) {
    //绘制矩形
    drawRect(matrix, textureId, width, height);
  }

  glBindFramebuffer(GL_FRAMEBUFFER, GL_NONE);
  glDisable(GL_BLEND);
  return frame_buffer_->getTexture();
}

void PaintRender::drawGraffiti(const glm::mat4 &matrix, GLuint textureId, int width, int height) const {
  frame_buffer_->createFrameBuffer(width, height);
  glBindFramebuffer(GL_FRAMEBUFFER, frame_buffer_->getFrameBuffer());
  glUseProgram(paintProgram_);
  auto mvpLoc = glGetUniformLocation(paintProgram_, "mvp");
  glUniformMatrix4fv(mvpLoc, 1, GL_FALSE, glm::value_ptr(matrix));

  auto pointSizeLocation = glGetUniformLocation(paintProgram_, "pointSize");
  glUniform1f(pointSizeLocation, paintSize_ * 1.7f / scale_);

  auto textureSizeLocation = glGetUniformLocation(paintProgram_, "textureSize");
  float textureSize[] = {(float) width, (float) height};
  glUniform2fv(textureSizeLocation, 1, textureSize);
  //绑定笔刷纹理到纹理单元1
  glActiveTexture(GL_TEXTURE1);
  glBindTexture(GL_TEXTURE_2D, brushTexture_);
  auto brushTextureLoc = glGetUniformLocation(paintProgram_, "brushTexture");
  glUniform1i(brushTextureLoc, 1);
  if (maskTexture_ > 0) {
    //绑定deeplab mask 纹理到纹理单元2
    glActiveTexture(GL_TEXTURE2);
    glBindTexture(GL_TEXTURE_2D, maskTexture_);
    auto maskTextureLoc = glGetUniformLocation(paintProgram_, "deeplabMask");
    glUniform1i(maskTextureLoc, 2);
  }
  //设置mask mode
  auto maskModeLocation = glGetUniformLocation(paintProgram_, "deeplabMode");
  glUniform1i(maskModeLocation, maskMode_);
  //绑定输入纹理到纹理单元0
  glActiveTexture(GL_TEXTURE0);
  glBindTexture(GL_TEXTURE_2D, textureId);
  auto inputTextureLoc = glGetUniformLocation(paintProgram_, "inputImageTexture");
  glUniform1i(inputTextureLoc, 0);
  glBindVertexArray(vao_);
  glDrawArrays(GL_POINTS, 0, points);
  glBindVertexArray(0);
  glBindTexture(GL_TEXTURE_2D, 0);
}

void PaintRender::drawRect(const glm::mat4 &matrix, GLuint textureId, int width, int height) {
  if (rectFrameBuffer_ == nullptr) {
    rectFrameBuffer_ = new FrameBuffer();
  }
  rectFrameBuffer_->createFrameBuffer(width, height);
  glBindFramebuffer(GL_FRAMEBUFFER, rectFrameBuffer_->getFrameBuffer());
  glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
  GL_CHECK(glUseProgram(rectProgram_))

  float vertexCoordinate_[] = {
      0.f, height * 1.f, width * 1.f, height * 1.f, 0.f, 0.f, width * 1.f, 0.f
  };
  auto positionLoc = glGetAttribLocation(rectProgram_, "position");
  GL_CHECK(glEnableVertexAttribArray(positionLoc))
  GL_CHECK(glVertexAttribPointer(positionLoc, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat),
                                 vertexCoordinate_))

  auto mvpLoc = glGetUniformLocation(rectProgram_, "mvp");
  if (mvpLoc >= 0) {
    GL_CHECK(glUniformMatrix4fv(mvpLoc, 1, GL_FALSE, glm::value_ptr(matrix)))
  }

  auto textureSizeLocation = glGetUniformLocation(rectProgram_, "textureSize");
  float textureSize[] = {(float) width, (float) height};
  glUniform2fv(textureSizeLocation, 1, textureSize);

  auto startPointLoc = glGetUniformLocation(rectProgram_, "inputStartPoint");
  if (startPointLoc >= 0) {
    glm::vec4 startPoint = matrix * glm::vec4((float) startTouchPointX_, (float) startTouchPointY_, (float) 0, (float) 1);
    float start[] = {startPoint.x, startPoint.y};
    GL_CHECK(glUniform2fv(startPointLoc, 1, start))
  }

  auto endPointLoc = glGetUniformLocation(rectProgram_, "inputEndPoint");
  if (endPointLoc >= 0) {
    glm::vec4 endPoint = matrix * glm::vec4((float) touchPointX_, (float) touchPointY_, (float) 0, (float) 1);
    float end[] = {endPoint.x, endPoint.y};
    GL_CHECK(glUniform2fv(endPointLoc, 1, end))
  }

  GL_CHECK(glActiveTexture(GL_TEXTURE0))
  GL_CHECK(glBindTexture(GL_TEXTURE_2D, textureId))
  auto inputTextureLoc = glGetUniformLocation(rectProgram_, "inputImageTexture");
  GL_CHECK(glUniform1i(inputTextureLoc, 0))
  GL_CHECK(glDrawArrays(GL_TRIANGLE_STRIP, 0, 4))
  GL_CHECK(glDisableVertexAttribArray(positionLoc))
  GL_CHECK(glBindTexture(GL_TEXTURE_2D, 0))
}

void PaintRender::finalApplyRect() {
  glBindFramebuffer(GL_FRAMEBUFFER, frame_buffer_->getFrameBuffer());
  GL_CHECK(glEnable(GL_BLEND))
  GL_CHECK(glBlendFunc(GL_ONE, GL_ONE_MINUS_SRC_ALPHA))
  GL_CHECK(glBlendEquation(GL_FUNC_ADD))

  GL_CHECK(glViewport(0, 0, frame_buffer_->getTextureWidth(), frame_buffer_->getTextureHeight()));

  auto textureCoordinate = DEFAULT_TEXTURE_COORDINATE;
  GL_CHECK(glUseProgram(blendProgram_))
  auto positionLoction = glGetAttribLocation(blendProgram_, "position");
  GL_CHECK(glEnableVertexAttribArray(positionLoction))
  //因为绘制的时候是以左上角为原点为基准来定的绘制点，但是合成的时候是opengl默认的左下角为原点，所以要上下反转
  GL_CHECK(glVertexAttribPointer(positionLoction, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat),
                                 DEFAULT_VERTEX_COORDINATE))
  auto textureLocation = glGetAttribLocation(blendProgram_, "inputTextureCoordinate");
  GL_CHECK(glEnableVertexAttribArray(textureLocation))
  GL_CHECK(glVertexAttribPointer(textureLocation, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat),
                                 textureCoordinate))

  GL_CHECK(glActiveTexture(GL_TEXTURE0));
  GL_CHECK(glBindTexture(GL_TEXTURE_2D, rectFrameBuffer_->getTexture()));
  auto inputTextureLocation = glGetUniformLocation(blendProgram_, "inputImageTexture");
  GL_CHECK(glUniform1i(inputTextureLocation, 0))
  GL_CHECK(glDrawArrays(GL_TRIANGLE_STRIP, 0, 4))

  GL_CHECK(glDisableVertexAttribArray(positionLoction))
  GL_CHECK(glDisableVertexAttribArray(textureLocation))
  GL_CHECK(glBindTexture(GL_TEXTURE_2D, 0))
  GL_CHECK(glDisable(GL_BLEND))
  GL_CHECK(glBindFramebuffer(GL_FRAMEBUFFER, GL_NONE))
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
