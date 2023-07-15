//
// Created by shell m1 on 2023/7/14.
//

#include "MosaicFilter.h"

static constexpr char VERTEX_SHADER[] = R"(
    #ifdef GL_ES
    precision highp float;
    #endif
    attribute vec4 position;
    void main() {
        gl_Position = position;
    }
)";

static constexpr char FRAGMENT_SHADER[] = R"(
    #ifdef GL_ES
    precision highp float;
    #endif
    uniform sampler2D inputImageTexture;
    uniform vec2 textureSize;
    uniform vec2 rectSize;
    void main() {
       vec2 textureXY = gl_FragCoord.xy;
       vec2 rectXY = vec2(floor(textureXY.x/rectSize.x)*rectSize.x, floor(textureXY.y/rectSize.y)*rectSize.y);
       vec2 rectUV = vec2(rectXY.x/textureSize.x, 1.f - rectXY.y/textureSize.y);
       vec4 color = texture2D(inputImageTexture, rectUV);
       gl_FragColor = color;
    }
)";

std::string MosaicFilter::onBuildVertexShader() {
  return VERTEX_SHADER;
}

std::string MosaicFilter::onBuildFragmentShader() {
  return FRAGMENT_SHADER;
}

void MosaicFilter::onPrepareProgram(GLuint program) {
  textureSizeHandler = glGetUniformLocation(program, "textureSize");
  rectSizeHandler = glGetUniformLocation(program, "rectSize");
}

void MosaicFilter::onUpdateParams(const FilterSource *source, const FilterTarget *target) {
  float textureSize[] = {(float) target->width, (float) target->height};
  glUniform2fv(textureSizeHandler, 1, textureSize);
  float rectSize[] = {50.f, 50.f};
  glUniform2fv(rectSizeHandler, 1, rectSize);
}