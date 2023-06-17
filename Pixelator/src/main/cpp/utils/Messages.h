//
// Created by 李金祥 on 2023/1/22.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_UTILS_MESSAGES_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_UTILS_MESSAGES_H_

enum EGLMessage {
  kCreateEGL = 0,
  kCreateEGLSurface,
  kSurfaceChanged,
  kSetSurfaceSize,
  kDestroyEGLSurface,
  kDestroyEGL
};

enum PixelateMessage {
  kInsertImage = 100,
  kSetEffect,
  kUpdateEffect,
  kSetBrush,
  kSetDeeplabMask,
  ksetDeeplabMaskMode,
  kSetPaintSize,
  kTouchEvent,
  kSetMatrix,
  kRefreshFrame,
  kSave,
  kUndo,
  kRedo,
  kCreateMiniSurface,
  kMiniSurfaceChanged,
  kMiniSurfaceDestroy
};
#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_UTILS_MESSAGES_H_
