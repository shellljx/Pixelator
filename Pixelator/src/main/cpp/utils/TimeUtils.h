//
// Created by 李金祥 on 2023/1/21.
//

#ifndef PIXELATE_PIXELATOR_SRC_MAIN_CPP_UTILS_TIMEUTILS_H_
#define PIXELATE_PIXELATOR_SRC_MAIN_CPP_UTILS_TIMEUTILS_H_
#include <sys/time.h>

class TimeUtils {
 public:
  static int64_t GetCurrentTimeUs();
};

#endif //PIXELATE_PIXELATOR_SRC_MAIN_CPP_UTILS_TIMEUTILS_H_
