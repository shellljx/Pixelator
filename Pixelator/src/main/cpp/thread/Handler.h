//
// Created by 李金祥 on 2023/1/21.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_THREAD_HANDLER_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_THREAD_HANDLER_H_

#include "Looper.h"
#include "Message.h"

namespace thread {

class Looper;
class Message;

class HandlerCallback {
 public:
  virtual void handleMessage(Message *msg) {}
};

class Handler {
 public:
  Handler(Looper *looper, HandlerCallback *callback);
  ~Handler();

  void sendMessage(Message *msg);
  void dispatchMessage(Message *msg);
  void removeMessage(int what);
  int size();
 private:
  Looper *looper_ = nullptr;
  HandlerCallback *callback_ = nullptr;
};
}

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_THREAD_HANDLER_H_
