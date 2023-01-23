//
// Created by 李金祥 on 2023/1/23.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_GL_PROGRAM_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_GL_PROGRAM_H_

#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include "OpenGL.h"

class Program {
 public:
  static GLuint CreateProgram(const char* vertex, const char* fragment);

 private:
  static int CompileShader(const char* shader_string, GLuint shader);

  static int Link(GLuint program);
};

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_GL_PROGRAM_H_
