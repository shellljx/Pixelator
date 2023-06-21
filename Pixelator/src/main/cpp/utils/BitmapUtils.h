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
  jmethodID decodeMethodId = env->GetStaticMethodID(bitmapFactoryClass.get(), "decodeFile", "(Ljava/lang/String;)Landroid/graphics/Bitmap;");
  jobject bitmapObj = env->CallStaticObjectMethod(bitmapFactoryClass.get(), decodeMethodId, env->NewStringUTF(path));
  auto ret = createBitmapInfo(bitmapObj, info);
  env->DeleteLocalRef(bitmapObj);
  return ret;
}
#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_UTILS_BITMAPUTILS_H_
