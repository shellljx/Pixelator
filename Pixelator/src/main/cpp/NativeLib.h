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

void Android_Jni_add_image_path(JNIEnv *env, jobject object, jlong id, jstring jpath, jint rotate);

jboolean Android_Jni_setBrush(JNIEnv *env, jobject object, jlong id, jobject bitmap);

void Android_Jni_pushTouchBuffer(JNIEnv *env, jobject object, jlong id, jfloatArray buffer, jint count);

void Android_Jni_translate(JNIEnv *env, jobject object, jlong id, jfloat scale, jfloat pivotX, jfloat pivotY, jfloat angle, jfloat translateX, jfloat translateY);

void Android_Jni_set_matrix(JNIEnv *env, jobject object, jlong id, jfloatArray floatArray);

void Andriod_Jni_refresh_frame(JNIEnv *env, jobject object, jlong id);

void Android_Jni_save(JNIEnv *env, jobject object, jlong id);

static JNINativeMethod pixelatorNativeMethods[] = {
    {"create", "()J", (void **) Android_Jni_Pixelator_create},
    {"onSurfaceCreate", "(JLandroid/view/Surface;)V", (void **) Android_Jni_surface_create},
    {"onSurfaceChanged", "(JII)V", (void **) Android_Jni_surface_changed},
    {"nativeAddImagePath", "(JLjava/lang/String;I)V", (void **) Android_Jni_add_image_path},
    {"setBrush", "(JLandroid/graphics/Bitmap;)Z", (void **) Android_Jni_setBrush},
    {"pushTouchBuffer", "(J[FI)V", (void **) Android_Jni_pushTouchBuffer},
    {"refreshFrame", "(J)V", (void **) Andriod_Jni_refresh_frame},
    {"translate", "(JFFFFFF)V", (void **) Android_Jni_translate},
    {"nativeSetMatrix", "(J[F)V", (void **) Android_Jni_set_matrix},
    {"nativeSave", "(J)V", (void **) Android_Jni_save}
};

#endif //PIXELATE_PIXELATOR_JNI_H
