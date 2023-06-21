//
// Created by 李金祥 on 2023/1/23.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_GL_OPENGL_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_GL_OPENGL_H_
#include "Log.h"

#ifndef GL_CHECK
#define GL_CHECK(x) { \
    x;                  \
    GLenum glError = glGetError();                                                      \
    if(glError != GL_NO_ERROR) {                                                        \
        LOGE("gl error: %i (0x%.8x) at %s:%i\n", glError, glError, __FILE__, __LINE__); \
    }                                                                                   \
}
#endif

/// 默认顶点坐标
static float DEFAULT_VERTEX_COORDINATE[] = {
    -1.F, -1.F,
    1.F, -1.F,
    -1.F, 1.F,
    1.F, 1.F
};

static float DEFAULT_VERTEX_COORDINATE_FLIP_DOWN_UP[] = {
    -1.F, 1.F,
    1.F, 1.F,
    -1.F, -1.F,
    1.F, -1.F
};

/// 上下翻转的纹理坐标
static float TEXTURE_COORDINATE_FLIP_UP_DOWN[] = {
    0.0f, 1.0f,
    1.0f, 1.0f,
    0.0f, 0.0f,
    1.0f, 0.0f
};

static float DEFAULT_TEXTURE_COORDINATE[] = {
    0.0f, 0.0f,
    1.0f, 0.0f,
    0.0f, 1.0f,
    1.0f, 1.0f
};

static float DEFAULT_TEXTURE_COORDINATE_FLIP_DOWN_UP[] = {
    0.0f, 1.0f,
    1.0f, 1.0f,
    0.0f, 0.0f,
    1.0f, 0.0f
};

/// 旋转270度，然后上下翻转
static float TEXTURE_COORDINATE_270_FLIP_UP_DOWN[] = {
    1.0f, 1.0f,
    1.0f, 0.0f,
    0.0f, 1.0f,
    0.0f, 0.0f
};

/// 旋转180度，然后上下翻转
static float TEXTURE_COORDINATE_180_FLIP_UP_DOWN[] = {
    1.0f, 0.0f,
    0.0f, 0.0f,
    1.0f, 1.0f,
    0.0f, 1.0f
};

/// 默认顶点shader
static const char *DEFAULT_VERTEX_SHADER =
    "#ifdef GL_ES                                                                           \n"
    "precision highp float;                                                                 \n"
    "#endif                                                                                 \n"
    "attribute vec4 position;                                                               \n"
    "attribute vec4 inputTextureCoordinate;                                                 \n"
    "varying vec2 textureCoordinate;                                                        \n"
    "void main() {                                                                          \n"
    "    gl_Position = position;                                                            \n"
    "    textureCoordinate = inputTextureCoordinate.xy;                                     \n"
    "}                                                                                      \n";

/// 带矩阵的顶点shader
static const char *DEFAULT_MATRIX_VERTEX_SHADER =
    "#ifdef GL_ES                                                                           \n"
    "precision highp float;                                                                 \n"
    "#endif                                                                                 \n"
    "attribute vec4 position;                                                               \n"
    "attribute vec4 inputTextureCoordinate;                                                 \n"
    "uniform mat4 mvp;                                                                      \n"
    "varying vec2 textureCoordinate;                                                        \n"
    "void main() {                                                                          \n"
    "    gl_Position = mvp * position;                                                      \n"
    "    textureCoordinate = inputTextureCoordinate.xy;                                     \n"
    "}                                                                                      \n";

/// 默认fragment shader
static const char *DEFAULT_FRAGMENT_SHADER =
    "#ifdef GL_ES                                                                           \n"
    "precision highp float;                                                                 \n"
    "#endif                                                                                 \n"
    "varying vec2 textureCoordinate;                                                        \n"
    "uniform sampler2D inputImageTexture;                                                   \n"
    "void main() {                                                                          \n"
    "    gl_FragColor = texture2D(inputImageTexture, textureCoordinate);                    \n"
    "}                                                                                      \n";

/// 默认顶点shader
static const char *PIXELATE_VERTEX_SHADER =
    "#ifdef GL_ES                                                                           \n"
    "precision highp float;                                                                 \n"
    "#endif                                                                                 \n"
    "attribute vec4 position;                                                               \n"
    "uniform mat4 mvp;                                                                      \n"
    "uniform float pointSize;                                                               \n"
    "void main() {                                                                          \n"
    "    gl_Position = mvp * position;                                                            \n"
    "    gl_PointSize = pointSize;                                     \n"
    "}                                                                                      \n";

static const char *PIXELATE_RECT_FRAGMENT_SHADER =
    "#ifdef GL_ES                                                                           \n"
    "precision highp float;                                                                 \n"
    "#endif                                                                                 \n"
    "uniform sampler2D inputImageTexture;                                                   \n"
    "uniform vec2 textureSize; \n"
    "uniform vec2 rectSize; \n"
    "void main() {                                                                          \n"
    "   vec2 textureXY = gl_FragCoord.xy; \n"
    "   vec2 rectXY = vec2(floor(textureXY.x/rectSize.x)*rectSize.x, floor(textureXY.y/rectSize.y)*rectSize.y); \n"
    "   vec2 rectUV = vec2(rectXY.x/textureSize.x, 1.f - rectXY.y/textureSize.y); \n"
    "   vec4 color = texture2D(inputImageTexture, rectUV); \n"
    "   gl_FragColor = color;                    \n"
    "}                                                                                      \n";

static const char *BRUSH_FRAGMENT_SHADER =
    "    #ifdef GL_ES \n"
    "    precision highp float; \n"
    "    #endif \n"
    "    uniform sampler2D inputImageTexture; \n"
    "    uniform sampler2D brushTexture; \n"
    "    uniform sampler2D deeplabMask;  \n"
    "    uniform int deeplabMode; \n"
    "    uniform vec2 textureSize; \n"
    "    uniform vec2 rectSize; \n"
    "    float outColorTransparent; \n"
    "    float aTransparent; \n"
    "    void main () { \n"
    "    vec4 mask = texture2D(brushTexture, vec2(gl_PointCoord.x, gl_PointCoord.y)); \n"
    "    vec2 textureXY = gl_FragCoord.xy; \n"
    "    vec2 rectUV = vec2(textureXY.x/textureSize.x, textureXY.y/textureSize.y); \n"
    "    vec4 color = texture2D(inputImageTexture, rectUV); \n"
    "    vec4 deeplabColor = texture2D(deeplabMask, vec2(rectUV.x,1.-rectUV.y));\n"
    "    outColorTransparent = color.a; \n"
    "    vec3 aTransparentColor=vec3(0.);\n"
    "    if(mask.a<1.0){\n"
    "    aTransparent = mask.a * outColorTransparent; \n"
    "    aTransparentColor = mask.rgb;\n"
    "    if(deeplabMode == 1 && deeplabColor.a==1.){\n"
    "    } else if(deeplabMode == 2 && deeplabColor.r==0.) {\n"
    "        aTransparent = 0.;\n"
    "    }\n"
    "    gl_FragColor = mix(aTransparent *(vec4(1.0) - ((vec4(1.0)-color))*(vec4(1.0)-vec4(aTransparentColor,1.0))),vec4(0.0,0.0,0.0,0.0),deeplabColor.a);\n"
    "    } else {\n"
    "    gl_FragColor = outColorTransparent * (vec4(1.0) - ((vec4(1.0)-vec4(color.rgb,1.0)))*(vec4(1.0)-mask)); \n"
    "    } \n"
    "    }";

/// 人像保护透明蒙版fragment shader, 1人像保护 2背景保护
static const char *DEEPLAB_FRAGMENT_SHADER =
    "    #ifdef GL_ES                                                                           \n"
    "    precision highp float;                                                                 \n"
    "    #endif                                                                                 \n"
    "    varying vec2 textureCoordinate;                                                        \n"
    "    uniform int mode;                                                                      \n"
    "    uniform sampler2D inputImageTexture;                                                   \n"
    "    void main() {                                                                          \n"
    "        vec4 color = texture2D(inputImageTexture, textureCoordinate);                      \n"
    "        if(mode == 1) {                                                                    \n"
    "            vec4 color = texture2D(inputImageTexture, textureCoordinate);                  \n"
    "            float r = color.r;                                                             \n"
    "            if(r==0.0) {                                                                   \n"
    "                color = vec4(0.0,0.0,0.0,0.0);                                             \n"
    "            } else {                                                                       \n"
    "                color = vec4(1.0,0.0,0.0,1.0);                                             \n"
    "            }                                                                              \n"
    "            gl_FragColor = color;                                                          \n"
    "        } else {                                                                           \n"
    "            vec4 color = texture2D(inputImageTexture, textureCoordinate);                  \n"
    "            float r = color.r;                                                             \n"
    "            if(r==1.0) {                                                                   \n"
    "                color = vec4(0.0,0.0,0.0,0.0);                                             \n"
    "            } else {                                                                       \n"
    "                color = vec4(1.0,0.0,0.0,0.7);                                             \n"
    "            }                                                                              \n"
    "            gl_FragColor = color;                                                          \n"
    "        }                                                                                  \n"
    "    }";

//void PrintGLError() {
//  GLenum err;
//  for (;;) {
//    err = glGetError();
//    if (err == GL_NO_ERROR) break;
//    LOGE("lijinxiang egl error %d", err);
//  }
//}

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_GL_OPENGL_H_
