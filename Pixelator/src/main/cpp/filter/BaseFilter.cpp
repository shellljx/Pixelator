//
// Created by shell m1 on 2023/7/11.
//

#include "glm/gtc/matrix_transform.hpp"
#include "glm/gtc/type_ptr.hpp"
#include "BaseFilter.h"

static constexpr char VERTEX_SHADER[] = R"(
    #ifdef GL_ES
    precision highp float;
    #endif
    attribute vec4 position;
    attribute vec4 inputTextureCoordinate;
    varying vec2 textureCoordinate;
    void main() {
        gl_Position = position;
        textureCoordinate = inputTextureCoordinate.xy;
    }
)";

static constexpr char FRAGMENT_SHADER[] = R"(
    #ifdef GL_ES
    precision highp float;
    #endif
    varying vec2 textureCoordinate;
    uniform sampler2D inputImageTexture;
    void main() {
        gl_FragColor = texture2D(inputImageTexture, textureCoordinate);
    }
)";

BaseFilter::~BaseFilter() {
  if (program > 0) {
    glDeleteProgram(program);
    program = 0;
  }
}

bool BaseFilter::initialize() {
  if (isInited)return true;
  CheckGLError();
  auto vertex = onBuildVertexShader();
  auto fragment = onBuildFragmentShader();
  program = Program::CreateProgram(vertex.c_str(), fragment.c_str());
  if (program <= 0) {
    return false;
  }
  positionHandler = glGetAttribLocation(program, "position");
  textureCoordinateHandler = glGetAttribLocation(program, "inputTextureCoordinate");
  mvpMatrixHandler = glGetUniformLocation(program, "mvp");
  onPrepareProgram(program);
  if (!CheckGLError()) {
    glDeleteProgram(program);
    program = 0;
    return false;
  }
  isInited = true;
  return true;
}

std::string BaseFilter::onBuildVertexShader() {
  return VERTEX_SHADER;
}

std::string BaseFilter::onBuildFragmentShader() {
  return FRAGMENT_SHADER;
}

void BaseFilter::onPrepareProgram(GLuint) {
}

void BaseFilter::enableBlend(GLenum sfactor, GLenum dfactor) {
  glEnable(GL_BLEND);
  glBlendFunc(sfactor, dfactor);
  glBlendEquation(GL_FUNC_ADD);
}

void BaseFilter::disableBlend() {
  glDisable(GL_BLEND);
}

void BaseFilter::onPreDraw() {
}

void BaseFilter::onAfterDraw() {
}

void BaseFilter::onUpdateParams(const FilterSource *source, const FilterTarget *target) {
}
void BaseFilter::onRunDrawTask() {
  glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
}

void BaseFilter::draw(const FilterSource *source, const FilterTarget *target) {
  if (source == nullptr || target == nullptr || program <= 0) {
    LOGE("filter can not drawRecord, because source/target is null or program invalid");
    return;
  }
  onPreDraw();
  if (target->frameBuffer != nullptr) {
    glBindFramebuffer(GL_FRAMEBUFFER, target->frameBuffer->getFrameBuffer());
  }
  if (target->clear) {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
  }
  glUseProgram(program);
  glClearColor(0.2f, 0.2f, 0.2f, 1.f);
  glViewport(0, 0, target->width, target->height);
  activeGLTexture(0, GL_TEXTURE_2D, source->texture);
  if (mvpMatrixHandler >= 0) {
    glUniformMatrix4fv(mvpMatrixHandler, 1, GL_FALSE, glm::value_ptr(target->vertexMatrix));
  }
  if (positionHandler >= 0) {
    glEnableVertexAttribArray(positionHandler);
    glVertexAttribPointer(positionHandler, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat),
                          target->vertexs);
  }
  if (textureCoordinateHandler >= 0) {
    glEnableVertexAttribArray(textureCoordinateHandler);
    glVertexAttribPointer(textureCoordinateHandler, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(GLfloat),
                          source->coordinate);
  }
  onUpdateParams(source, target);
  onRunDrawTask();
  if (positionHandler >= 0) {
    glDisableVertexAttribArray(positionHandler);
  }
  if (textureCoordinateHandler >= 0) {
    glDisableVertexAttribArray(textureCoordinateHandler);
  }
  glBindFramebuffer(GL_FRAMEBUFFER, GL_NONE);
  onAfterDraw();
}