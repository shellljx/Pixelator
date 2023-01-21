//
// Created by 李金祥 on 2023/1/21.
//

#ifndef PIXELATE_LOOPER_H
#define PIXELATE_LOOPER_H

#include "Message.h"
#include "MessageQueue.h"
#include <map>
#include <mutex>

namespace thread {

class Message;
class MessageQueue;

class Looper {
 public:
  Looper();
  ~Looper();

  static void Prepare();
  static void Loop();
  static Looper *MyLooper();
  static void Exit();

  void quit(bool safely);

  void dump();
  int size();
  void sendMessage(Message *msg);
  void removeMessage(int what);

 private:
  void loopInternal();
  void enqueuMessage(Message *msg);
  Message *take();

 private:
  bool exiting_ = false;
  bool exited_ = false;
  bool exitSafely_ = false;
  bool looping_ = false;
  pthread_mutex_t mutex_ = {};
  MessageQueue *messageQueue_ = nullptr;
};

class LooperManager {
 public:
  friend Looper;

  static LooperManager *GetInstance();

 public:
  ~LooperManager();
  Looper *create(int64_t tid);
  Looper *get(int64_t tid);
  void remove(int64_t tid);
  int size();

 private:
  LooperManager();
 private:
  static LooperManager *instance_;
  std::map<int64_t, Looper *> looperMap_;
  std::mutex mutex_;
};
}

#endif //PIXELATE_LOOPER_H
