//
// Created by 李金祥 on 2023/1/29.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_UTILS_BITMAPUTILS_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_UTILS_BITMAPUTILS_H_

#include "JNIEnvironment.h"
#include <memory>
#include <android/bitmap.h>
#include "ImageInfo.h"
#include "Local.h"
#include "Log.h"

static int createBitmapInfo(jobject &bitmap, ImageInfo **image) {
  auto env = JNIEnvironment::Current();
  if (env == nullptr) {
    LOGE("%s jni env is null", __func__);
    return -1;
  }
  AndroidBitmapInfo info;
  int result = AndroidBitmap_getInfo(env, bitmap, &info);
  if (result != ANDROID_BITMAP_RESULT_SUCCESS) {
    LOGE("AndroidBitmap_getInfo error %d", result);
    return result;
  }
  unsigned char *data;
  result = AndroidBitmap_lockPixels(env, bitmap, reinterpret_cast<void **>(&data));
  if (result != ANDROID_BITMAP_RESULT_SUCCESS) {
    LOGE("AndroidBitmap_lockPixels error %d", result);
    return result;
  }
  size_t count = info.stride * info.height;
  unsigned char *resultData = new unsigned char[count];
  memcpy(resultData, data, count);

  result = AndroidBitmap_unlockPixels(env, bitmap);
  if (result != ANDROID_BITMAP_RESULT_SUCCESS) {
    LOGE("AndroidBitmap_unlockPixels error %d", result);
    delete[] resultData;
    return result;
  }
  *image = new ImageInfo(info.width, info.height, resultData);
  return 0;
}

static int loadImageFromPath(const char *path, ImageInfo **info) {
  auto env = JNIEnvironment::Current();
  if (env == nullptr) {
    return -1;
  }
  Local<jclass> bitmapFactoryClass = {env, env->FindClass("android/graphics/BitmapFactory")};
  jmethodID decodeMethodId = env->GetStaticMethodID(bitmapFactoryClass.get(),
                                                    "decodeFile",
                                                    "(Ljava/lang/String;)Landroid/graphics/Bitmap;");
  jobject bitmapObj = env->CallStaticObjectMethod(bitmapFactoryClass.get(),
                                                  decodeMethodId,
                                                  env->NewStringUTF(path));
  auto ret = createBitmapInfo(bitmapObj, info);
  env->DeleteLocalRef(bitmapObj);
  return ret;
}

static void saveFrameBufferToBitmap(jobject pixelator, GLuint frameBuffer, int width, int height) {
  auto buffer = new uint8_t[width * height * 4];
  if (frameBuffer > 0) {
    glBindFramebuffer(GL_READ_FRAMEBUFFER, frameBuffer);
    glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
    glBindFramebuffer(GL_READ_FRAMEBUFFER, GL_NONE);
  } else {
    glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
  }

  JNIEnv *env = JNIEnvironment::Current();
  if (env == nullptr) {
    return;
  }
  Local<jclass> bitmapClass = {env, env->FindClass("android/graphics/Bitmap")};
  const char *bitmapCreateMethod = "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;";
  jmethodID bitmapCreateMethodId = env->GetStaticMethodID(bitmapClass.get(), "createBitmap",
                                                          bitmapCreateMethod);

  jstring configName = env->NewStringUTF("ARGB_8888");
  Local<jclass> configClass = {env, env->FindClass("android/graphics/Bitmap$Config")};
  const char *bitmapConfigSignature = "(Ljava/lang/String;)Landroid/graphics/Bitmap$Config;";
  jmethodID bitmapConfigMethodId = env->GetStaticMethodID(configClass.get(), "valueOf",
                                                          bitmapConfigSignature);
  Local<jobject> bitmapConfig = {env, env->CallStaticObjectMethod(configClass.get(),
                                                                  bitmapConfigMethodId,
                                                                  configName)};
  Local<jobject> bitmap = {env, env->CallStaticObjectMethod(bitmapClass.get(), bitmapCreateMethodId,
                                                            width, height, bitmapConfig.get())};

  void *bitmapPixels;
  if ((AndroidBitmap_lockPixels(env, bitmap.get(), &bitmapPixels)) < 0) {
    return;
  }
  memcpy((uint8_t *) bitmapPixels, buffer, width * height * 4);
  AndroidBitmap_unlockPixels(env, bitmap.get());

  Local<jclass> sdkClass = {env, env->GetObjectClass(pixelator)};
  jmethodID frameSavedMethodId = env->GetMethodID(sdkClass.get(), "onFrameSaved",
                                                  "(Landroid/graphics/Bitmap;)V");
  env->CallVoidMethod(pixelator, frameSavedMethodId, bitmap.get());
}
#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_UTILS_BITMAPUTILS_H_
