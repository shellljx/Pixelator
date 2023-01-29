//
// Created by 李金祥 on 2023/1/20.
//

#ifndef PIXELATE_PIXELATOR_JNI_H
#define PIXELATE_PIXELATOR_JNI_H
#define PIXELATOR "com/gmail/shellljx/pixelator/Pixelator"

#include <jni.h>

jlong Android_Jni_Pixelator_create(JNIEnv *env, jobject jobject);

void Android_Jni_surface_create(JNIEnv *env, jobject object, jlong id, jobject jsurface);

void Android_Jni_surface_changed(JNIEnv *env, jobject object, jlong id, jint width, jint height);

void Android_Jni_add_image_path(JNIEnv *env, jobject object, jlong id, jstring jpath);

jboolean Android_Jni_setBrush(JNIEnv *env, jobject object, jlong id, jobject bitmap);

void Android_Jni_touch_event(JNIEnv *env, jobject object, jlong id, jfloat x, jfloat y);

void Andriod_Jni_refresh_frame(JNIEnv *env, jobject object, jlong id);

static JNINativeMethod pixelatorNativeMethods[] = {
    {"create", "()J", (void **) Android_Jni_Pixelator_create},
    {"onSurfaceCreate", "(JLandroid/view/Surface;)V", (void **) Android_Jni_surface_create},
    {"onSurfaceChanged", "(JII)V", (void **) Android_Jni_surface_changed},
    {"addImagePath", "(JLjava/lang/String;)V", (void **) Android_Jni_add_image_path},
    {"setBrush", "(JLandroid/graphics/Bitmap;)Z", (void **) Android_Jni_setBrush},
    {"touchEvent", "(JFF)V", (void **) Android_Jni_touch_event},
    {"refreshFrame", "(J)V", (void **) Andriod_Jni_refresh_frame}
};

#endif //PIXELATE_PIXELATOR_JNI_H
