//
// Created by 李金祥 on 2023/1/21.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_PIXELATOR_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_PIXELATOR_H_

#include "Handler.h"
#include "HandlerThread.h"
#include <pthread.h>

class Pixelator :public thread::HandlerCallback {
 public:
  explicit Pixelator();
  virtual ~Pixelator();

  void handleMessage(thread::Message *msg) override;

 private:
  thread::HandlerThread *handlerThread_ = nullptr;
  thread::Handler *handler_ = nullptr;
};

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_PIXELATOR_H_
