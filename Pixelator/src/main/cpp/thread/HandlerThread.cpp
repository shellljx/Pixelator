//
// Created by 李金祥 on 2023/1/22.
//

#include "HandlerThread.h"
#include <utility>
#include "Log.h"

namespace thread {
HandlerThread *HandlerThread::Create(std::string name) {
  return new HandlerThread(std::move(name));
}

static void *RunTask(void *context) {
  auto handlerThread = reinterpret_cast<HandlerThread *>(context);
  handlerThread->runInternal();
  pthread_exit(nullptr);
}

HandlerThread::HandlerThread(std::string name) {
  pthread_mutex_init(&mutex_, nullptr);
  pthread_cond_init(&condition_, nullptr);
  pthread_attr_t attr;
  pthread_attr_init(&attr);
  pthread_attr_setdetachstate(&attr, PTHREAD_CREATE_JOINABLE);
  pthread_create(&thread_, &attr, RunTask, (void *) this);
  pthread_setname_np(thread_, name.c_str());
}

HandlerThread::~HandlerThread() {
  LOGI("enter %s", __func__);
  pthread_mutex_lock(&mutex_);
  if (looper_) {
    delete looper_;
    looper_ = nullptr;
  }
  pthread_mutex_unlock(&mutex_);
  pthread_mutex_destroy(&mutex_);
  pthread_cond_destroy(&condition_);
  LOGI("level %s", __func__);
}

void HandlerThread::runInternal() {
  pthread_mutex_lock(&mutex_);
  exiting_ = false;
  exited_ = false;
  pthread_mutex_unlock(&mutex_);
  Looper::Prepare();
  pthread_mutex_lock(&mutex_);
  looper_ = Looper::MyLooper();
  pthread_mutex_unlock(&mutex_);
  pthread_cond_broadcast(&condition_);
  pthread_mutex_unlock(&mutex_);

  Looper::Loop();
  Looper::Exit();

  pthread_mutex_lock(&mutex_);
  if (looper_ == nullptr) {
    exited_ = true;
    pthread_mutex_unlock(&mutex_);
    return;
  }
  exiting_ = false;
  exited_ = true;
  if (looper_) {
    delete looper_;
    looper_ = nullptr;
  }
  pthread_mutex_unlock(&mutex_);
}

void HandlerThread::quit() {
  LOGI("enter %s", __func__);
  pthread_mutex_lock(&mutex_);
  if (exiting_ || exited_) {
    pthread_mutex_unlock(&mutex_);
    return;
  }
  exiting_ = true;
  pthread_mutex_unlock(&mutex_);
  Looper *looper = getLooper();
  if (looper) {
    looper->quit(false);
  }
  pthread_detach(thread_);
  LOGI("level %s", __func__);
}

bool HandlerThread::quitSafely() {
  pthread_mutex_lock(&mutex_);
  if (exiting_ || exited_) {
    pthread_mutex_unlock(&mutex_);
    pthread_detach(thread_);
    return false;
  }
  exiting_ = true;
  pthread_mutex_unlock(&mutex_);
  Looper *looper = getLooper();
  if (looper) {
    looper->quit(true);
    pthread_mutex_unlock(&mutex_);
    pthread_join(thread_, nullptr);
    return true;
  }
  pthread_detach(thread_);
  return false;
}

Looper *HandlerThread::getLooper() {
  pthread_mutex_lock(&mutex_);
  if (exited_) {
    pthread_mutex_unlock(&mutex_);
    return nullptr;
  }
  if (looper_ == nullptr) {
    LOGI("thread should wait %s", __func__);
    pthread_cond_wait(&condition_, &mutex_);
    LOGI("thread continue work");
  }
  pthread_mutex_unlock(&mutex_);
  return looper_;
}
}
