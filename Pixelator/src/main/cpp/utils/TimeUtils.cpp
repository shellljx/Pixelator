//
// Created by 李金祥 on 2023/1/21.
//

#include "TimeUtils.h"

int64_t TimeUtils::GetCurrentTimeUs() {
  struct timeval time{};
  gettimeofday(&time, nullptr);
  return static_cast<int64_t>(time.tv_sec * 1000000.0 + time.tv_usec);
}
