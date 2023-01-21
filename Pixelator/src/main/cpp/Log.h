//
// Created by 李金祥 on 2023/1/21.
//

#ifndef PIXELATE_LOG_H
#define PIXELATE_LOG_H

#include <android/log.h>

#define LOG_TAG "pixelator"

static void PrintLog(android_LogPriority level, const char format[], ...) {
    va_list args;
    va_start(args, format);
    __android_log_vprint(level, LOG_TAG, format, args);
    va_end(args);
}

#define LOGI(...) PrintLog(ANDROID_LOG_INFO, __VA_ARGS__)
#define LOGE(...) PrintLog(ANDROID_LOG_ERROR, __VA_ARGS__)

#endif //PIXELATE_LOG_H
