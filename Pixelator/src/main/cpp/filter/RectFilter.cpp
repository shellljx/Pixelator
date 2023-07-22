//
// Created by shell m1 on 2023/7/13.
//

#include "RectFilter.h"

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
    uniform sampler2D maskTexture;
    uniform int maskMode;
    uniform vec2 inputStartPoint;
    uniform vec2 inputEndPoint;
    void main(){
        vec2 textureXY = gl_FragCoord.xy;
        vec2 rectUV = vec2(textureXY.x/textureSize.x, textureXY.y/textureSize.y);
        vec2 fragCoord = 2.0*rectUV-1.0;
        bool xinside = fragCoord.x >= min(inputStartPoint.x,inputEndPoint.x) && fragCoord.x <= max(inputEndPoint.x,inputStartPoint.x);
        bool yinside = fragCoord.y >= min(inputStartPoint.y,inputEndPoint.y) && fragCoord.y <= max(inputEndPoint.y,inputStartPoint.y);
        if(xinside && yinside){
          vec4 mask = texture2D(maskTexture, vec2(rectUV.x, 1. - rectUV.y));
          float fractor = 0.0;
          if (maskMode == 2) {
            fractor = 1.0 - mask.a;
          } else if (maskMode == 1) {
            fractor = mask.a;
          } else {
            fractor = 0.0;
          }
          gl_FragColor = mix(texture2D(inputImageTexture, rectUV), vec4(0.0,0.0,0.0,0.0), fractor);
        }else{
          gl_FragColor = vec4(0.0,0.0,0.0,0.0);
        }
    }
)";

std::string RectFilter::onBuildVertexShader() {
  return VERTEX_SHADER;
}

std::string RectFilter::onBuildFragmentShader() {
  return FRAGMENT_SHADER;
}

void RectFilter::updatePoint(float startX, float startY, float endX, float endY) {
  startx = startX;
  starty = startY;
  endx = endX;
  endy = endY;
}

void RectFilter::updateMaskMode(int mode) {
  maskMode = mode;
}

void RectFilter::updateMaskTexture(GLuint texture) {
  maskTexture = texture;
}

void RectFilter::onPrepareProgram(GLuint program) {
  textureSizeHandler = glGetUniformLocation(program, "textureSize");
  startPointHandler = glGetUniformLocation(program, "inputStartPoint");
  endPointHandler = glGetUniformLocation(program, "inputEndPoint");
  maskModeHandler = glGetUniformLocation(program, "maskMode");
  maskHandler = glGetUniformLocation(program, "maskTexture");
}

void RectFilter::onUpdateParams(const FilterSource *source, const FilterTarget *target) {
  float textureSize[] = {(float) target->width, (float) target->height};
  glUniform2fv(textureSizeHandler, 1, textureSize);
  glm::vec4 startPoint = target->vertexMatrix
      * glm::vec4(startx, starty, 0.f, 1.f);
  float start[] = {startPoint.x, startPoint.y};
  glUniform2fv(startPointHandler, 1, start);
  glm::vec4 endPoint = target->vertexMatrix
      * glm::vec4(endx, endy, 0.f, 1.f);
  float end[] = {endPoint.x, endPoint.y};
  glUniform2fv(endPointHandler, 1, end);
  glUniform1i(maskModeHandler, maskMode);
  if (maskTexture > 0) {
    activeGLTexture(1, GL_TEXTURE_2D, maskTexture);
    glUniform1i(maskHandler, 1);
  }
}