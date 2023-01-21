//
// Created by 李金祥 on 2023/1/20.
//

#ifndef PIXELATE_PIXELATOR_JNI_H
#define PIXELATE_PIXELATOR_JNI_H
#define PIXELATOR "com/gmail/shellljx/pixelator/Pixelator"

#include <jni.h>

jlong Android_Jni_Pixelator_create(JNIEnv *env, jobject jobject);


static JNINativeMethod pixelatorNativeMethods[] = {
        {"create", "()J", (void **) Android_Jni_Pixelator_create}
};


#endif //PIXELATE_PIXELATOR_JNI_H
