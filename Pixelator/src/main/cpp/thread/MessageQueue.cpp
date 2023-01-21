//
// Created by 李金祥 on 2023/1/21.
//

#include "MessageQueue.h"
#include "Log.h"
#include <sstream>

namespace thread {
MessageQueue::MessageQueue() {
  pthread_mutex_init(&mutex_, nullptr);
  pthread_cond_init(&condition_, nullptr);
}

MessageQueue::~MessageQueue() {
  LOGI("enter %s", __func__);
  pthread_mutex_lock(&mutex_);
  isDestroy_ = true;
  pthread_mutex_unlock(&mutex_);
  pthread_mutex_destroy(&mutex_);
  pthread_cond_destroy(&condition_);
  LOGI("level %s", __func__);
}

void MessageQueue::offer(Message *msg) {
  pthread_mutex_lock(&mutex_);
  if (isDestroy_) {
    pthread_mutex_unlock(&mutex_);
    return;
  }
  list_.push_back(msg);
  pthread_cond_broadcast(&condition_);
  pthread_mutex_unlock(&mutex_);
}

Message *MessageQueue::take() {
  if (isDestroy_) {
    return nullptr;
  }
  pthread_mutex_lock(&mutex_);
  if (size() <= 0) {
    LOGI("message queue is empty, should wait");
    pthread_cond_wait(&condition_, &mutex_);
    LOGI("message queue wake up");
  }
  if (isEmpty()) {
    return nullptr;
  }
  Message *msg = list_.front();
  list_.pop_front();
  pthread_mutex_unlock(&mutex_);
  return msg;
}

void MessageQueue::clear() {
  if (isEmpty()) {
    return;
  }
  pthread_mutex_lock(&mutex_);
  while (!isEmpty()) {
    Message *msg = list_.front();
    list_.pop_front();
    delete msg;
  }
  list_.clear();
  pthread_mutex_unlock(&mutex_);
}

void MessageQueue::removeMessage(int what) {
  if (isDestroy_) {
    return;
  }
  pthread_mutex_lock(&mutex_);
  auto iterator = list_.begin();
  while (iterator != list_.end()) {
    Message *msg = *iterator;
    if (what == msg->what) {
      delete msg;
      iterator = list_.erase(iterator);
      continue;
    }
    ++iterator;
  }
  pthread_mutex_unlock(&mutex_);
}

void MessageQueue::notify() {
  if (isDestroy_) {
    return;
  }
  pthread_mutex_lock(&mutex_);
  pthread_cond_broadcast(&condition_);
  pthread_mutex_unlock(&mutex_);
}

int MessageQueue::size() {
  return list_.size();
}

void MessageQueue::dump() {
  pthread_mutex_lock(&mutex_);
  std::ostringstream os;
  auto iterator = list_.begin();
  while (iterator != list_.end()) {
    Message *msg = *iterator;
    os << msg->what << "\n";
    ++iterator;
  }
  LOGI("message queue dump %s", os.str().c_str());
  pthread_mutex_unlock(&mutex_);
}

bool MessageQueue::isEmpty() {
  return list_.empty();
}
}
