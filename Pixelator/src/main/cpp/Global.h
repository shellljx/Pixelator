//
// Created by 李金祥 on 2022/11/6.
//

#ifndef CAMERARECORD_GLOBAL_H
#define CAMERARECORD_GLOBAL_H

#include "JNIEnvironment.h"

template<typename T>
class Global {
 public:
  Global() = default;

  Global(const Global<T> &that) = delete;

  Global<T> &operator=(const Global<T> &that) = delete;

  Global(JNIEnv *env, T ref) : env_(env), ref_(ref) {
  }

  ~Global() {
    if (ref_ == nullptr) {
      return;
    }
    if (env_ == nullptr) {
      env_ = JNIEnvironment::Current();
    }
    if (env_ == nullptr) {
      return;
    }
    env_->DeleteGlobalRef(ref_);
    ref_ = nullptr;
  }

  void reset(JNIEnv *env, T ref) {
    if (env_ == nullptr, ref == nullptr || ref_ == ref) {
      return;
    }
    if (ref_ != nullptr && env_ != nullptr) {
      env_->DeleteGlobalRef(ref_);
      ref_ = nullptr;
    }
    env_ = env;
    ref_ = (T) env_->NewGlobalRef(ref);
  }

  bool empty() const {
    return ref_ == nullptr;
  }

  T get() const {
    return ref_;
  }

 private:
  JNIEnv *env_ = nullptr;
  T ref_ = nullptr;
};

#endif //CAMERARECORD_GLOBAL_H
