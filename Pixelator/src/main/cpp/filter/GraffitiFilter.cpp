//
// Created by shell m1 on 2023/7/12.
//

#include "GraffitiFilter.h"

static constexpr char VERTEX_SHADER[] = R"(
    #ifdef GL_ES
    precision highp float;
    #endif
    attribute vec4 position;
    uniform mat4 mvp;
    uniform float pointSize;
    void main() {
        gl_Position = mvp * position;
        gl_PointSize = pointSize;
    }
)";

static constexpr char FRAGMENT_SHADER[] = R"(
  #ifdef GL_ES
  precision highp float;
  #endif
  uniform sampler2D inputImageTexture;
  uniform sampler2D brushTexture;
  uniform sampler2D deeplabMask;
  uniform int deeplabMode;
  uniform vec2 textureSize;
  uniform vec2 rectSize;
  float outColorTransparent;
  float aTransparent;
  float fractor;
  void main() {
    vec4 mask = texture2D(brushTexture, vec2(gl_PointCoord.x, gl_PointCoord.y));
    vec2 textureXY = gl_FragCoord.xy;
    vec2 rectUV = vec2(textureXY.x / textureSize.x, textureXY.y / textureSize.y);
    vec4 color = texture2D(inputImageTexture, rectUV);
    vec4 deeplabColor = texture2D(deeplabMask, vec2(rectUV.x, 1. - rectUV.y));
    outColorTransparent = color.a;
    vec3 aTransparentColor = vec3(0.);
    if (mask.a < 1.0) {
      aTransparent = mask.a * outColorTransparent;
      aTransparentColor = mask.rgb;
      if (deeplabMode == 2) {
        fractor = 1.0 - deeplabColor.a;
      } else if (deeplabMode == 1) {
        fractor = deeplabColor.a;
      } else {
        fractor = 0.0;
      }
      gl_FragColor = mix(aTransparent * (vec4(1.0) - ((vec4(1.0) - color)) * (vec4(1.0) - vec4(aTransparentColor, 1.0))), vec4(0.0, 0.0, 0.0, 0.0), fractor);
    } else {
      gl_FragColor = outColorTransparent * (vec4(1.0) - ((vec4(1.0) - vec4(color.rgb, 1.0))) * (vec4(1.0) - mask));
    }
  }
)";

GraffitiFilter::~GraffitiFilter() {
  if (maskTexture > 0) {
    glDeleteTextures(1, &maskTexture);
    maskTexture = 0;
  }
  glBindVertexArray(GL_NONE);
  glBindBuffer(GL_ARRAY_BUFFER, GL_NONE);
  if (vao > 0) {
    glDeleteVertexArrays(1, &vao);
    vao = 0;
  }
  if (pointsVbo > 0) {
    glDeleteBuffers(1, &pointsVbo);
    pointsVbo = 0;
  }
}

void GraffitiFilter::updateBrush(GLuint texture) {
  brushTexture = texture;
}

void GraffitiFilter::updateMask(GLuint texture) {
  maskTexture = texture;
}

void GraffitiFilter::updatePoints(float *buffer, int length) {
  if (vao == 0) {
    glGenVertexArrays(1, &vao);
  }
  glBindVertexArray(vao);
  pointsCount = length / 2;
  if (pointsVbo == 0) {
    glGenBuffers(1, &pointsVbo);
    glBindBuffer(GL_ARRAY_BUFFER, pointsVbo);
    glBufferData(GL_ARRAY_BUFFER, length * sizeof(float), buffer, GL_DYNAMIC_DRAW);
    glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(float), nullptr);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
    glEnableVertexAttribArray(0);
  } else {
    glBindBuffer(GL_ARRAY_BUFFER, pointsVbo);
    glBufferData(GL_ARRAY_BUFFER, length * sizeof(float), buffer, GL_DYNAMIC_DRAW);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
  }
  glBindVertexArray(GL_NONE);
}

void GraffitiFilter::updatePaintSize(float size) {
  this->paintSize = size;
}

void GraffitiFilter::updateMaskMode(int mode) {
  this->maskMode = mode;
}

void GraffitiFilter::updatePaintMode(int mode) {
  this->paintMode = mode;
}

std::string GraffitiFilter::onBuildVertexShader() {
  return VERTEX_SHADER;
}
std::string GraffitiFilter::onBuildFragmentShader() {
  return FRAGMENT_SHADER;
}

void GraffitiFilter::onPrepareProgram(GLuint program) {
  pointSizeHandler = glGetUniformLocation(program, "pointSize");
  textureSizeHandler = glGetUniformLocation(program, "textureSize");
  brushTextureHandler = glGetUniformLocation(program, "brushTexture");
  maskTextureHandler = glGetUniformLocation(program, "deeplabMask");
  maskModeHandler = glGetUniformLocation(program, "deeplabMode");
}

void GraffitiFilter::onPreDraw() {
  if (paintMode == 1) {
    enableBlend(GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
  } else {
    enableBlend(GL_ZERO, GL_ONE_MINUS_SRC_ALPHA);
  }
}

void GraffitiFilter::onUpdateParams(const FilterSource *source, const FilterTarget *target) {
  glUniform1f(pointSizeHandler, paintSize);
  float textureSize[] = {(float) target->width, (float) target->height};
  glUniform2fv(textureSizeHandler, 1, textureSize);
  glUniform1i(maskModeHandler, maskMode);
  activeGLTexture(1, GL_TEXTURE_2D, brushTexture);
  glUniform1i(brushTextureHandler, 1);
  if (maskTexture > 0) {
    activeGLTexture(2, GL_TEXTURE_2D, maskTexture);
    glUniform1i(maskTextureHandler, 2);
  }
}

void GraffitiFilter::onRunDrawTask() {
  glBindVertexArray(vao);
  glDrawArrays(GL_POINTS, 0, pointsCount);
  glBindVertexArray(GL_NONE);
}

void GraffitiFilter::onAfterDraw() {
  disableBlend();
}