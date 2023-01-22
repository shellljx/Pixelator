//
// Created by 李金祥 on 2023/1/22.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_THREAD_HANDLERTHREAD_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_THREAD_HANDLERTHREAD_H_

#include <pthread.h>
#include <string>
#include "Looper.h"

namespace thread {
class HandlerThread {
 public:
  static HandlerThread *Create(std::string name);
  ~HandlerThread();

  void runInternal();
  
  void quit();

  bool quitSafely();

  Looper *getLooper();

 private:
  HandlerThread(std::string name);

 private:
  std::string name_;
  pthread_t thread_;
  pthread_mutex_t mutex_;
  pthread_cond_t condition_;
  Looper *looper_ = nullptr;
  bool exiting_ = false;
  bool exited_ = false;
};
}

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_THREAD_HANDLERTHREAD_H_
