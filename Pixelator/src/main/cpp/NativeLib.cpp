//
// Created by 李金祥 on 2023/1/20.
//

#include "NativeLib.h"
#include "Pixelator.h"
#include "Log.h"

jlong Android_Jni_Pixelator_create(JNIEnv *env, jobject object) {
  auto pixelator = new Pixelator(object);
  return reinterpret_cast<jlong>(pixelator);
}

void Android_Jni_surface_create(JNIEnv *env, jobject object, jlong id, jobject jsurface) {
  auto pixelator = reinterpret_cast<Pixelator *>(id);
  pixelator->onSurfaceCreate(jsurface);
}

void Android_Jni_surface_changed(JNIEnv *env, jobject object, jlong id, jint width, jint height) {
  auto pixelator = reinterpret_cast<Pixelator *>(id);
  pixelator->onSurfaceChanged(width, height);
}

void Android_Jni_add_image_path(JNIEnv *env, jobject object, jlong id, jstring jpath) {
  auto pixelator = reinterpret_cast<Pixelator *>(id);
  auto path = env->GetStringUTFChars(jpath, JNI_FALSE);
  pixelator->addImagePath(path);
  env->ReleaseStringUTFChars(jpath, path);
}

jboolean Android_Jni_setBrush(JNIEnv *env, jobject object, jlong id, jobject bitmap) {
  auto pixelator = reinterpret_cast<Pixelator *>(id);
  return pixelator->setBrush(bitmap);
}

void Android_Jni_pushTouchBuffer(JNIEnv *env, jobject object, jlong id, jfloatArray buffer, jint count) {
  auto pixelator = reinterpret_cast<Pixelator *>(id);
  jfloat *touchBuffer = env->GetFloatArrayElements(buffer, nullptr);
  jsize length = env->GetArrayLength(buffer);

  auto *copyBuffer = new float[length];
  memcpy(copyBuffer, touchBuffer, length * sizeof(float));
  pixelator->pushTouchBuffer(copyBuffer, length);
  env->ReleaseFloatArrayElements(buffer, touchBuffer, 0);
}

void Andriod_Jni_refresh_frame(JNIEnv *env, jobject object, jlong id) {
  auto pixelator = reinterpret_cast<Pixelator *>(id);
  pixelator->refreshFrame();
}
