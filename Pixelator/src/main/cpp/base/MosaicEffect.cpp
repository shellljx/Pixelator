//
// Created by shell m1 on 2023/7/15.
//
#include "file.h"
MosaicEffect::MosaicEffect(EffectType type, int rectSize) : Effect(type), rectSize(rectSize) {}

int MosaicEffect::getRectSize() {
  return rectSize;
}