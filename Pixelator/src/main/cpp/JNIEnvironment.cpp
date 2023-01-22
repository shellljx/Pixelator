//
// Created by 李金祥 on 2023/1/20.
//

#include "JNIEnvironment.h"
#include <pthread.h>
#include "NativeLib.h"

static JavaVM *globalJavaVM = nullptr;
static pthread_key_t threadKey = 0;

static void JNI_Thread_Destroy(void *value) {
    auto *env = reinterpret_cast<JNIEnv *>(value);
    if (env != nullptr && globalJavaVM != nullptr) {
        globalJavaVM->DetachCurrentThread();
        pthread_setspecific(threadKey, nullptr);
    }
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    globalJavaVM = vm;
    JNIEnv *env = nullptr;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    auto pixelatorClass = env->FindClass(PIXELATOR);
    auto result = env->RegisterNatives(pixelatorClass, pixelatorNativeMethods, sizeof(pixelatorNativeMethods) / sizeof(pixelatorNativeMethods[0]));
    env->DeleteLocalRef(pixelatorClass);

    if (result != JNI_OK) {
        return JNI_ERR;
    }
    pthread_key_create(&threadKey, JNI_Thread_Destroy);
    return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *) {
    pthread_key_delete(threadKey);
}

JNIEnv *JNIEnvironment::Current() {
    if (globalJavaVM == nullptr) {
        return nullptr;
    }
    JNIEnv *env = nullptr;
    auto result = globalJavaVM->GetEnv((void **) &env, JNI_VERSION_1_6);
    if (result == JNI_EDETACHED || env == nullptr) {
        JavaVMAttachArgs args = {JNI_VERSION_1_6, "Pixelate_JNIEnvironment", nullptr};
        if (globalJavaVM->AttachCurrentThread(&env, &args) != JNI_OK) {
            return env;
        }
        pthread_setspecific(threadKey, (void *) env);
        return env;
    }
    return env;
}
