//
// Created by 李金祥 on 2023/1/21.
//

#include "Handler.h"

namespace thread {
    Handler::Handler(Looper *looper, HandlerCallback *callback)
            : looper_(looper),
              callback_(callback) {
    }

    Handler::~Handler() {

    }

    void Handler::sendMessage(Message *msg) {
        if (looper_) {
            msg->target_ = this;
            looper_->sendMessage(msg);
        }
    }

    void Handler::dispatchMessage(Message *msg) {
        if (callback_) {
            callback_->handleMessage(msg);
        }
    }

    void Handler::removeMessage(int what) {
        if (looper_) {
            looper_->removeMessage(what);
        }
    }

    int Handler::size() {
        if (looper_) {
            return looper_->size();
        }
        return 0;
    }
}
