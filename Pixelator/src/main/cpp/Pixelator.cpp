//
// Created by 李金祥 on 2023/1/21.
//

#include "Pixelator.h"
#include "Log.h"

Pixelator::Pixelator() {
  std::string name("pixelator thread");
  handlerThread_ = thread::HandlerThread::Create(name);
  handler_ = new thread::Handler(handlerThread_->getLooper(), this);
  thread::Message *msg = new thread::Message();
  msg->what = 1;
  handler_->sendMessage(msg);
}

Pixelator::~Pixelator() {

}

void Pixelator::handleMessage(thread::Message *msg) {
  LOGI("lijinxiang message %d", msg->what);
}
