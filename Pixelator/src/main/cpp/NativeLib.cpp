//
// Created by 李金祥 on 2023/1/20.
//

#include "NativeLib.h"
#include "Pixelator.h"
#include "Log.h"

jlong Android_Jni_Pixelator_create(JNIEnv *env, jobject object) {
  auto pixelator = new Pixelator();
  return reinterpret_cast<jlong>(pixelator);
}
