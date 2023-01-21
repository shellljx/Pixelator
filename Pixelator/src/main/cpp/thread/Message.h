//
// Created by 李金祥 on 2023/1/21.
//

#ifndef PIXELATE_MESSAGE_H
#define PIXELATE_MESSAGE_H

#include "Handler.h"
#include <string>

namespace thread {

class Handler;

class Message {
 public:
  Message();
  ~Message();

 public:
  int what = -1;
  int arg1 = -1;
  int arg2 = -1;
  bool flag1 = false;
  bool flag2 = false;
  void *obj1 = nullptr;
  void *obj2 = nullptr;
  std::string str1;
  std::string str2;

 public:
  Handler *target_{};
};
}

#endif //PIXELATE_MESSAGE_H
