//
// Created by 李金祥 on 2022/11/12.
//

#ifndef CAMERARECORD_FRAMEBUFFER_H
#define CAMERARECORD_FRAMEBUFFER_H

#include "Program.h"

class FrameBuffer {
public:
    FrameBuffer();

    ~FrameBuffer();

    void createFrameBuffer(int width, int height);

    GLuint getTexture();

    GLuint getFrameBuffer();

    void deleteFrameBuffer();

private:
    GLuint frameBufferId_ = 0;
    GLuint frameTextureId_ = 0;
    int width_;
    int height_;
};


#endif //CAMERARECORD_FRAMEBUFFER_H
