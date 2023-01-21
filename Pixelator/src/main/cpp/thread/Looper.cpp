//
// Created by 李金祥 on 2023/1/21.
//

#include "Looper.h"
#include "Log.h"
#include "utils/TimeUtils.h"
#include <cassert>

namespace thread {

Looper::Looper() {
  messageQueue_ = new MessageQueue();
  pthread_mutex_init(&mutex_, nullptr);
}

Looper::~Looper() {
  LOGI("enter %s", __func__);
  pthread_mutex_lock(&mutex_);
  delete messageQueue_;
  messageQueue_ = nullptr;

  exiting_ = false;
  exited_ = true;
  looping_ = false;
  pthread_mutex_unlock(&mutex_);
}

void Looper::Prepare() {
  int64_t tid = pthread_self();
  Looper *looper = LooperManager::GetInstance()->create(tid);
  if (looper == nullptr) {
    LOGE("current thread looper has bean created");
  }
}

void Looper::Loop() {
  MyLooper()->loopInternal();
}

Looper *Looper::MyLooper() {
  int64_t tid = pthread_self();
  Looper *looper = LooperManager::GetInstance()->get(tid);
  if (looper == nullptr) {
    LOGE("please invoke prepare first");
  }
  assert(looper);
  return looper;
}

void Looper::Exit() {
  int64_t tid = pthread_self();
  LooperManager::GetInstance()->remove(tid);
}

void Looper::quit(bool safely) {
  pthread_mutex_lock(&mutex_);
  if (exiting_ || exited_) {
    pthread_mutex_unlock(&mutex_);
    return;
  }
  exitSafely_ = safely;
  exiting_ = true;
  pthread_mutex_unlock(&mutex_);
  LOGI("message queue size=%d", messageQueue_->size());
  dump();
  messageQueue_->notify();
}

void Looper::dump() {
  if (messageQueue_ != nullptr) {
    messageQueue_->dump();
  }
}

int Looper::size() {
  if (messageQueue_ != nullptr) {
    return messageQueue_->size();
  }
  return 0;
}

void Looper::sendMessage(Message *msg) {
  pthread_mutex_lock(&mutex_);
  if (exiting_ || exited_) {
    pthread_mutex_unlock(&mutex_);
    return;
  }
  pthread_mutex_unlock(&mutex_);
  enqueuMessage(msg);
}

void Looper::removeMessage(int what) {
  if (messageQueue_ != nullptr) {
    messageQueue_->removeMessage(what);
  }
}

void Looper::loopInternal() {
  pthread_mutex_lock(&mutex_);
  if (looping_ || exiting_ || exited_) {
    pthread_mutex_unlock(&mutex_);
    return;
  }
  looping_ = true;
  pthread_mutex_unlock(&mutex_);

  for (;;) {
    Message *msg = take();
    if (msg) {
      if (msg->target_) {
        msg->target_->dispatchMessage(msg);
      }
      delete msg;
    }
    if (exited_) {
      break;
    }
    pthread_mutex_lock(&mutex_);
    if (exitSafely_) {
      if (exiting_ && messageQueue_->isEmpty()) {
        pthread_mutex_unlock(&mutex_);
        break;
      }
    } else {
      if (exiting_) {
        pthread_mutex_unlock(&mutex_);
        break;
      }
    }
    pthread_mutex_unlock(&mutex_);
  }
  LOGI("exit message loop");
  if (messageQueue_ == nullptr || exited_) {
    return;
  }
  int64_t time = TimeUtils::GetCurrentTimeUs();
  messageQueue_->clear();
  LOGI("clear message queue cost time=%lld us", (TimeUtils::GetCurrentTimeUs() - time));
  pthread_mutex_lock(&mutex_);
  exiting_ = false;
  exited_ = true;
  looping_ = false;
  pthread_mutex_unlock(&mutex_);
}

void Looper::enqueuMessage(Message *msg) {
  if (messageQueue_ != nullptr) {
    messageQueue_->offer(msg);
  }
}

Message *Looper::take() {
  if (messageQueue_ != nullptr) {
    return messageQueue_->take();
  }
  return nullptr;
}

LooperManager *LooperManager::instance_ = new LooperManager();

LooperManager::LooperManager() = default;

LooperManager::~LooperManager() = default;

LooperManager *LooperManager::GetInstance() {
  return instance_;
}

Looper *LooperManager::create(int64_t tid) {
  std::lock_guard<std::mutex> guard(mutex_);
  auto iterator = looperMap_.find(tid);
  if (iterator == looperMap_.end()) {
    auto *looper = new Looper();
    looperMap_[tid] = looper;
    return looper;
  }
  return nullptr;
}

Looper *LooperManager::get(int64_t tid) {
  std::lock_guard<std::mutex> guard(mutex_);
  auto iterator = looperMap_.find(tid);
  if (iterator == looperMap_.end()) {
    return nullptr;
  }
  return iterator->second;
}

void LooperManager::remove(int64_t tid) {
  std::lock_guard<std::mutex> guard(mutex_);
  auto iterator = looperMap_.find(tid);
  if (iterator != looperMap_.end()) {
    looperMap_.erase(iterator);
  }
}

int LooperManager::size() {
  std::lock_guard<std::mutex> guard(mutex_);
  return looperMap_.size();
}
}
