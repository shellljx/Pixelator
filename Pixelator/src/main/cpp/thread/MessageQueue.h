//
// Created by 李金祥 on 2023/1/21.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_THREAD_MESSAGEQUEUE_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_THREAD_MESSAGEQUEUE_H_

#include "Message.h"
#include <pthread.h>
#include <list>

namespace thread {

class Message;

class MessageQueue {
 public:
  MessageQueue();
  ~MessageQueue();

  void offer(Message *msg);
  Message *take();
  void notify();
  int size();
  bool isEmpty();
  void clear();
  void removeMessage(int what);
  void dump();

 private:
  pthread_mutex_t mutex_ = {};
  pthread_cond_t condition_ = {};
  std::list<Message *> list_;
  bool isDestroy_ = false;
};
}

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_THREAD_MESSAGEQUEUE_H_
