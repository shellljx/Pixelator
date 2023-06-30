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

void Android_Jni_surface_destroy(JNIEnv *env, jobject object, jlong id);

void Android_Jni_mini_surface_create(JNIEnv *env, jobject object, jlong id, jobject jsurface);

void Android_Jni_mini_surface_changed(JNIEnv *env,
                                      jobject object,
                                      jlong id,
                                      jint width,
                                      jint height);

void Android_Jni_mini_surface_destroy(JNIEnv *env, jobject object, jlong id);

void Android_Jni_add_image_path(JNIEnv *env, jobject object, jlong id, jstring jpath, jint rotate);

void Android_Jni_set_effect(JNIEnv *env, jobject object, jlong id, jstring config);

void Android_Jni_update_effect(JNIEnv *env, jobject object, jlong id, jstring config);

jboolean Android_Jni_setBrush(JNIEnv *env, jobject object, jlong id, jobject bitmap);

void Android_Jni_setDeeplabMask(JNIEnv *env, jobject object, jlong id, jobject bitmap);

void Android_Jni_setDeeplabMaskMode(JNIEnv *env, jobject object, jlong id, jint mode);

void Android_Jni_setPaintType(JNIEnv *env, jobject object, jlong id, jint paintType);

void Android_Jni_setPaintSize(JNIEnv *env, jobject object, jlong id, jint size);

void Android_Jni_pushTouchBuffer(JNIEnv *env,
                                 jobject object,
                                 jlong id,
                                 jfloatArray buffer,
                                 jfloat cx,
                                 jfloat cy);

void Android_Jni_nativeStopTouch(JNIEnv *env, jobject object, jlong id);

void Android_Jni_set_matrix(JNIEnv *env, jobject object, jlong id, jfloatArray floatArray);

void Android_Jni_update_viewport(JNIEnv *env, jobject object, jlong id, jint offset);

void Andriod_Jni_refresh_frame(JNIEnv *env, jobject object, jlong id);

void Android_Jni_undo(JNIEnv *env, jobject object, jlong id);

void Android_Jni_redo(JNIEnv *env, jobject object, jlong id);

void Android_Jni_save(JNIEnv *env, jobject object, jlong id);

void Android_Jni_destroy(JNIEnv *env, jobject object, jlong id);

static JNINativeMethod pixelatorNativeMethods[] = {
    {"create", "()J", (void **) Android_Jni_Pixelator_create},
    {"onSurfaceCreate", "(JLandroid/view/Surface;)V", (void **) Android_Jni_surface_create},
    {"onSurfaceChanged", "(JII)V", (void **) Android_Jni_surface_changed},
    {"onSurfaceDestroy", "(J)V", (void **) Android_Jni_surface_destroy},
    {"onMiniScreenSurfaceCreate", "(JLandroid/view/Surface;)V",
     (void **) Android_Jni_mini_surface_create},
    {"onMiniScreenSurfaceChanged", "(JII)V", (void **) Android_Jni_mini_surface_changed},
    {"onMiniScreenSurfaceDestroy", "(J)V", (void **) Android_Jni_mini_surface_destroy},
    {"nativeAddImagePath", "(JLjava/lang/String;I)V", (void **) Android_Jni_add_image_path},
    {"nativeSetEffect", "(JLjava/lang/String;)V", (void **) Android_Jni_set_effect},
    {"nativeUpdateEffect", "(JLjava/lang/String;)V", (void **) Android_Jni_update_effect},
    {"setBrush", "(JLandroid/graphics/Bitmap;)Z", (void **) Android_Jni_setBrush},
    {"nativeSetDeeplabMask", "(JLandroid/graphics/Bitmap;)V", (void **) Android_Jni_setDeeplabMask},
    {"nativeSetDeeplabMaskMode", "(JI)V", (void **) Android_Jni_setDeeplabMaskMode},
    {"nativeSetPaintType", "(JI)V", (void **) Android_Jni_setPaintType},
    {"setPaintSize", "(JI)V", (void **) Android_Jni_setPaintSize},
    {"pushTouchBuffer", "(J[FFF)V", (void **) Android_Jni_pushTouchBuffer},
    {"nativeStopTouch", "(J)V", (void **) Android_Jni_nativeStopTouch},
    {"refreshFrame", "(J)V", (void **) Andriod_Jni_refresh_frame},
    {"nativeUndo", "(J)V", (void **) Android_Jni_undo},
    {"nativeRedo", "(J)V", (void **) Android_Jni_redo},
    {"nativeSetMatrix", "(J[F)V", (void **) Android_Jni_set_matrix},
    {"nativeUpdateViewPort", "(JI)V", (void **) Android_Jni_update_viewport},
    {"nativeSave", "(J)V", (void **) Android_Jni_save},
    {"nativeDestroy", "(J)V", (void **) Android_Jni_destroy}
};

#endif //PIXELATE_PIXELATOR_JNI_H
