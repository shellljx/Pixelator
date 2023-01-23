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

static JNINativeMethod pixelatorNativeMethods[] = {
    {"create", "()J", (void **) Android_Jni_Pixelator_create},
    {"onSurfaceCreate", "(JLandroid/view/Surface;)V", (void **) Android_Jni_surface_create},
    {"onSurfaceChanged", "(JII)V", (void **) Android_Jni_surface_changed},
    {"addImagePath", "(JLjava/lang/String;)V", (void **) Android_Jni_add_image_path}
};

#endif //PIXELATE_PIXELATOR_JNI_H
