//
// Created by shell m1 on 2023/7/13.
//

#include "RectFilter.h"

static constexpr char VERTEX_SHADER[] = R"(
    #ifdef GL_ES
    precision highp float;
    #endif
    attribute vec4 position;
    uniform mat4 mvp;
    void main() {
      gl_Position = mvp * position;
    }
)";

static constexpr char FRAGMENT_SHADER[] = R"(
    #ifdef GL_ES
    precision highp float;
    #endif
    uniform sampler2D inputImageTexture;
    uniform vec2 textureSize;
    uniform vec2 inputStartPoint;
    uniform vec2 inputEndPoint;
    void main(){
        vec2 textureXY = gl_FragCoord.xy;
        vec2 rectUV = vec2(textureXY.x/textureSize.x, textureXY.y/textureSize.y);
        vec2 fragCoord = 2.0*rectUV-1.0;
        bool insideRectangle = fragCoord.x >= inputStartPoint.x && fragCoord.x <= inputEndPoint.x && fragCoord.y >= inputStartPoint.y && fragCoord.y <= inputEndPoint.y;
        if(insideRectangle){
        gl_FragColor = texture2D(inputImageTexture, rectUV);
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

void RectFilter::onPrepareProgram(GLuint program) {
  textureSizeHandler = glGetUniformLocation(program, "textureSize");
  startPointHandler = glGetUniformLocation(program, "inputStartPoint");
  endPointHandler = glGetUniformLocation(program, "inputEndPoint");
}

void RectFilter::onUpdateParams(const FilterSource *source, const FilterTarget *target) {
  float textureSize[] = {(float) target->width, (float) target->height};
  glUniform2fv(textureSizeHandler, 1, textureSize);
  auto textureCoord = source->coordinate;
  glm::vec4 startPoint = target->vertexMatrix
      * glm::vec4(textureCoord[0], textureCoord[1], 0.f, 1.f);
  float start[] = {startPoint.x, startPoint.y};
  glUniform2fv(startPointHandler, 1, start);
  glm::vec4 endPoint = target->vertexMatrix
      * glm::vec4(textureCoord[2], textureCoord[3], 0.f, 1.f);
  float end[] = {endPoint.x, endPoint.y};
  glUniform2fv(endPointHandler, 1, end);
}