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
static const char* DEFAULT_VERTEX_SHADER =
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
static const char* DEFAULT_FRAGMENT_SHADER =
    "#ifdef GL_ES                                                                           \n"
    "precision highp float;                                                                 \n"
    "#endif                                                                                 \n"
    "varying vec2 textureCoordinate;                                                        \n"
    "uniform sampler2D inputImageTexture;                                                   \n"
    "void main() {                                                                          \n"
    "    gl_FragColor = texture2D(inputImageTexture, textureCoordinate);                    \n"
    "}                                                                                      \n";

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_GL_OPENGL_H_
