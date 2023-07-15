//
// Created by shell m1 on 2023/7/15.
//

#include "file.h"

Effect::Effect(EffectType type) {
  effectType = type;
}

EffectType Effect::type() const {
  return effectType;
}