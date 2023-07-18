//
// Created by shell m1 on 2023/7/11.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_UTILS_GLUTILS_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_UTILS_GLUTILS_H_

#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <GLES3/gl3.h>
#include <GLES3/gl3ext.h>
#include <glm.hpp>
#include "glm/gtc/matrix_transform.hpp"
#include "Log.h"
#include "ImageInfo.h"

void activeGLTexture(int unitIndex, GLuint target, GLuint texture);

bool CheckGLError();

glm::mat4 getCenterInsideMatrix(int screenWidth, int screenHeight, int width, int height, int bottomOffset);

void textureCenterCrop(int width, int height, int targetWidth, int targetHeight, float *array);
void getVertexCoordinate(int width, int height, float *array);
void createImageTexture(GLuint &texture, ImageInfo *image);
#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_UTILS_GLUTILS_H_
