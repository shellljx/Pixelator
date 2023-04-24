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

static float DEFAULT_TEXTURE_COORDINATE[] = {
    0.0f, 0.0f,
    1.0f, 0.0f,
    0.0f, 1.0f,
    1.0f, 1.0f
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

/// 默认顶点shader
static const char *PIXELATE_VERTEX_SHADER =
    "#ifdef GL_ES                                                                           \n"
    "precision highp float;                                                                 \n"
    "#endif                                                                                 \n"
    "attribute vec4 position;                                                               \n"
    "void main() {                                                                          \n"
    "    gl_Position = position;                                                            \n"
    "    gl_PointSize = 100.;                                     \n"
    "}                                                                                      \n";

static const char *PIXELATE_RECT_FRAGMENT_SHADER =
    "#ifdef GL_ES                                                                           \n"
    "precision highp float;                                                                 \n"
    "#endif                                                                                 \n"
    "varying vec2 textureCoordinate;                                                        \n"
    "uniform sampler2D inputImageTexture;                                                   \n"
    "uniform vec2 textureSize; \n"
    "uniform vec2 rectSize; \n"
    "void main() {                                                                          \n"
    "   vec2 textureXY = gl_FragCoord.xy; \n"
    "   vec2 rectXY = vec2(floor(textureXY.x/rectSize.x)*rectSize.x, floor(textureXY.y/rectSize.y)*rectSize.y); \n"
    "   vec2 rectUV = vec2(rectXY.x/textureSize.x, rectXY.y/textureSize.y); \n"
    "   vec4 color = texture2D(inputImageTexture, rectUV); \n"
    "   gl_FragColor = color;                    \n"
    "}                                                                                      \n";

static const char *BRUSH_FRAGMENT_SHADER =
    "#ifdef GL_ES \n"
    "precision highp float; \n"
    "#endif \n"
    "uniform sampler2D inputImageTexture; \n"
    "uniform sampler2D brushTexture; \n"
    "uniform vec2 textureSize; \n"
    "uniform vec2 rectSize; \n"
    "float outColorTransparent; \n"
    "float aTransparent; \n"
    "void main () { \n"
    "vec4 mask = texture2D(brushTexture, vec2(gl_PointCoord.x, gl_PointCoord.y)); \n"
    "vec2 textureXY = gl_FragCoord.xy; \n"
    "vec2 rectUV = vec2(textureXY.x/textureSize.x, textureXY.y/textureSize.y); \n"
    "vec4 color = texture2D(inputImageTexture, rectUV); \n"
    "outColorTransparent = color.a; \n"
    "vec3 aTransparentColor=vec3(0.);\n"
    "if(mask.a<1.0){\n"
    "aTransparent = mask.a * outColorTransparent; \n"
    "aTransparentColor = mask.rgb; \n"
    "gl_FragColor = aTransparent *(vec4(1.0) - ((vec4(1.0)-color))*(vec4(1.0)-vec4(aTransparentColor,1.0)));\n"
    "} else {\n"
    "gl_FragColor = outColorTransparent * (vec4(1.0) - ((vec4(1.0)-vec4(color.rgb,1.0)))*(vec4(1.0)-mask)); \n"
    "} \n"
    "} \n";

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_GL_OPENGL_H_
