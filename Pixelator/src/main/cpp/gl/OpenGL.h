//
// Created by 李金祥 on 2023/1/23.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_GL_OPENGL_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_GL_OPENGL_H_

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

/// 上下翻转的纹理坐标
static float TEXTURE_COORDINATE_FLIP_UP_DOWN[] = {
    0.0f, 1.0f,
    1.0f, 1.0f,
    0.0f, 0.0f,
    1.0f, 0.0f
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

static const char *PIXELATE_RECT_FRAGMENT_SHADER =
    "#ifdef GL_ES \n"
    "precision highp float; \n"
    "#endif \n"
    "varying vec2 textureCoordinate; \n"
    "uniform sampler2D inputImageTexture; \n"
    "uniform vec2 textureSize; \n"
    "uniform vec2 rectSize; \n"
    "uniform float trackR; \n"
    "uniform vec2 start; \n"
    "uniform vec2 end; \n"
    "uniform float speed; \n"
    "void main () { \n"
    "vec2 textureXY = vec2(textureCoordinate.x *textureSize.x, textureCoordinate.y * textureSize.y); \n"
    "vec2 rectXY = vec2(floor(textureXY.x/rectSize.x)*rectSize.x, floor(textureXY.y/rectSize.y)*rectSize.y); \n"
    "vec2 rectUV = vec2(rectXY.x/textureSize.x, rectXY.y/textureSize.y); \n"
    "vec4 color = texture2D(inputImageTexture, textureCoordinate); \n"
    "vec2 line1 = vec2(gl_FragCoord.x,gl_FragCoord.y)-start; \n"
    "vec2 line2 = end - start; \n"
    "float len1 = sqrt(dot(line1, line1)); \n"
    "float len2 = sqrt(dot(line2, line2)); \n"
    "float cosv = abs(dot(line1, line2)) / len1 / len2; \n"
    "float len3 = len1 * cosv; \n"
    "float len4 = sqrt(pow(len1, 2.0)-pow(len3,2.0)); \n"
    "float distance = len4/trackR; \n"
    "gl_FragColor = vec4(color.r, color.g, color.b,1.0-pow(distance,3.0)); \n"
    "} \n";

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_GL_OPENGL_H_
