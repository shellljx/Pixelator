//
// Created by 李金祥 on 2022/11/6.
//

#ifndef CAMERARECORD_LOCAL_H
#define CAMERARECORD_LOCAL_H

#include <jni.h>

template<typename T>
class Local {
public:
    Local() = default;

    Local(const Local<T> &that) = delete;

    Local<T> &operator=(const Local<T> &that) = delete;

    Local(JNIEnv *env, T ref) : env_(env), ref_(ref) {
    }

    ~Local() {
        if (env_ != nullptr) {
            env_->DeleteLocalRef(ref_);
        }
    }

    void reset(JNIEnv *env, T reference) {
        if (reference == ref_) {
            return;
        }
        if (env_ != nullptr) {
            env_->DeleteLocalRef(ref_);
        }
        if (env == nullptr) {
            return;
        }
        env_ = env;
        ref_ = reference;
    }

    bool empty() const {
        return ref_ == nullptr;
    }

    T get() const {
        return ref_;
    }

    JNIEnv *env() const {
        return env_;
    }

private:
    JNIEnv *env_ = nullptr;
    T ref_ = nullptr;
};

#endif //CAMERARECORD_LOCAL_H
