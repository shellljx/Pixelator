//
// Created by shell m1 on 2023/7/11.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_UTILS_GLUTILS_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_UTILS_GLUTILS_H_

#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <GLES3/gl3.h>
#include <GLES3/gl3ext.h>
#include "Log.h"

void activeGLTexture(int unitIndex, GLuint target, GLuint texture);

bool CheckGLError();
#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_UTILS_GLUTILS_H_
