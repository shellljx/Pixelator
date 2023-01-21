//
// Created by 李金祥 on 2023/1/20.
//

#ifndef PIXELATE_JNIENVIRONMENT_H
#define PIXELATE_JNIENVIRONMENT_H

#include <jni.h>

class JNIEnvironment {
public:
    static JNIEnv* Current();
};


#endif //PIXELATE_JNIENVIRONMENT_H
