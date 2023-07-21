//
// Created by 李金祥 on 2023/1/20.
//

#include "NativeLib.h"
#include "ImageEngine.h"
#include "Log.h"

jlong Android_Jni_Pixelator_create(JNIEnv *env, jobject object) {
  auto pixelator = new ImageEngine(object);
  return reinterpret_cast<jlong>(pixelator);
}

void Android_Jni_surface_create(JNIEnv *env, jobject object, jlong id, jobject jsurface) {
  auto pixelator = reinterpret_cast<ImageEngine *>(id);
  pixelator->onSurfaceCreate(jsurface);
}

void Android_Jni_surface_changed(JNIEnv *env, jobject object, jlong id, jint width, jint height) {
  auto pixelator = reinterpret_cast<ImageEngine *>(id);
  pixelator->onSurfaceChanged(width, height);
}

void Android_Jni_surface_destroy(JNIEnv *env, jobject object, jlong id) {
  auto engine = reinterpret_cast<ImageEngine *>(id);
  engine->onSurfaceDestroy();
}

void Android_Jni_mini_surface_create(JNIEnv *env, jobject object, jlong id, jobject jsurface) {
  auto pixelator = reinterpret_cast<ImageEngine *>(id);
  pixelator->onMiniSurfaceCreate(jsurface);
}

void Android_Jni_mini_surface_changed(JNIEnv *env,
                                      jobject object,
                                      jlong id,
                                      jint width,
                                      jint height) {
  auto pixelator = reinterpret_cast<ImageEngine *>(id);
  pixelator->onMiniSurfaceChanged(width, height);
}

void Android_Jni_mini_surface_destroy(JNIEnv *env, jobject object, jlong id) {
  auto pixelator = reinterpret_cast<ImageEngine *>(id);
  pixelator->onMiniSurfaceDestroy();
}

void Android_Jni_add_image_path(JNIEnv *env, jobject object, jlong id, jstring jpath, jint rotate) {
  auto pixelator = reinterpret_cast<ImageEngine *>(id);
  auto path = env->GetStringUTFChars(jpath, JNI_FALSE);

  pixelator->addImagePath(path, rotate);
  env->ReleaseStringUTFChars(jpath, path);
}

void Android_Jni_set_effect(JNIEnv *env, jobject object, jlong id, jstring config) {
  auto engine = reinterpret_cast<ImageEngine *>(id);
  auto configStr = env->GetStringUTFChars(config, JNI_FALSE);
  engine->setEffect(configStr);
  env->ReleaseStringUTFChars(config, configStr);
}

void Android_Jni_update_effect(JNIEnv *env, jobject object, jlong id, jstring config) {
  auto engine = reinterpret_cast<ImageEngine *>(id);
  auto configStr = env->GetStringUTFChars(config, JNI_FALSE);
  engine->updateEffect(configStr);
  env->ReleaseStringUTFChars(config, configStr);
}

jboolean Android_Jni_setBrush(JNIEnv *env, jobject object, jlong id, jobject bitmap) {
  auto pixelator = reinterpret_cast<ImageEngine *>(id);
  return pixelator->setBrush(bitmap);
}

void Android_Jni_setDeeplabMask(JNIEnv *env, jobject object, jlong id, jobject bitmap) {
  auto engine = reinterpret_cast<ImageEngine *>(id);
  engine->setDeeplabMask(bitmap);
}

void Android_Jni_setDeeplabMaskMode(JNIEnv *env, jobject object, jlong id, jint mode) {
  auto engine = reinterpret_cast<ImageEngine *>(id);
  engine->setDeeplabMaskMode(mode);
}

void Android_Jni_setPaintMode(JNIEnv *env, jobject object, jlong id, jint paintType) {
  auto engine = reinterpret_cast<ImageEngine *>(id);
  engine->setPaintMode(paintType);
}

void Android_Jni_setPaintType(JNIEnv *env, jobject object, jlong id, jint paintType) {
  auto engine = reinterpret_cast<ImageEngine *>(id);
  engine->setPaintType(paintType);
}

void Android_Jni_setPaintSize(JNIEnv *env, jobject object, jlong id, jint size) {
  auto engine = reinterpret_cast<ImageEngine *>(id);
  engine->setPaintSize(size);
}

void Android_Jni_setCanvasHide(JNIEnv *env, jobject object, jlong id, jboolean hide) {
  auto engine = reinterpret_cast<ImageEngine *>(id);
  engine->setCanvasHide(hide);
}

void Android_Jni_pushTouchBuffer(JNIEnv *env,
                                 jobject object,
                                 jlong id,
                                 jfloatArray buffer,
                                 jfloat cx, jfloat cy) {
  auto pixelator = reinterpret_cast<ImageEngine *>(id);
  jfloat *touchBuffer = env->GetFloatArrayElements(buffer, nullptr);
  jsize length = env->GetArrayLength(buffer);

  auto *copyBuffer = new float[length];
  memcpy(copyBuffer, touchBuffer, length * sizeof(float));
  pixelator->pushTouchBuffer(copyBuffer, length, cx, cy);
  env->ReleaseFloatArrayElements(buffer, touchBuffer, 0);
}

void Android_Jni_nativeStopTouch(JNIEnv *env, jobject object, jlong id) {
  auto engine = reinterpret_cast<ImageEngine *>(id);
  engine->stopTouch();
}

void Android_Jni_nativeStartTouch(JNIEnv *env, jobject object, jlong id, jfloat x, jfloat y) {
  auto engine = reinterpret_cast<ImageEngine *>(id);
  engine->startTouch(x, y);
}

void Android_Jni_set_matrix(JNIEnv *env, jobject object, jlong id, jfloatArray floatArray) {
  auto engine = reinterpret_cast<ImageEngine *>(id);
  jfloat *matrix = env->GetFloatArrayElements(floatArray, nullptr);
  jsize length = env->GetArrayLength(floatArray);

  auto *copyMatrix = new float[length];
  memcpy(copyMatrix, matrix, length * sizeof(float));
  engine->setMatrix(copyMatrix);
  env->ReleaseFloatArrayElements(floatArray, matrix, 0);
}

void Android_Jni_update_viewport(JNIEnv *env, jobject object, jlong id, jint offset) {
  auto engine = reinterpret_cast<ImageEngine *>(id);
  engine->updateViewPort(offset);
}

void Andriod_Jni_refresh_frame(JNIEnv *env, jobject object, jlong id) {
  auto pixelator = reinterpret_cast<ImageEngine *>(id);
  pixelator->refreshFrame();
}

void Android_Jni_undo(JNIEnv *env, jobject object, jlong id) {
  auto engine = reinterpret_cast<ImageEngine *>(id);
  engine->undo();
}

void Android_Jni_redo(JNIEnv *env, jobject object, jlong id) {
  auto engine = reinterpret_cast<ImageEngine *>(id);
  engine->redo();
}

void Android_Jni_save(JNIEnv *env, jobject object, jlong id) {
  auto pixelator = reinterpret_cast<ImageEngine *>(id);
  pixelator->save();
}

void Android_Jni_destroy(JNIEnv *env, jobject object, jlong id) {
  auto engine = reinterpret_cast<ImageEngine *>(id);
  engine->destroy();
  delete engine;
}