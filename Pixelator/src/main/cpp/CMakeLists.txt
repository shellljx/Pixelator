# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.18.1)

# Declares and names the project.

project("pixelator")

set(PIXELATOR_DIR ${CMAKE_SOURCE_DIR})
set(STB_HEADER ${CMAKE_SOURCE_DIR}/stb)
set(OPENGL_DIR ${CMAKE_SOURCE_DIR}/gl)
set(GLM_DIR ${CMAKE_SOURCE_DIR}/glm)

include_directories(${PIXELATOR_DIR})
include_directories(${PIXELATOR_DIR}/thread)
include_directories(${PIXELATOR_DIR}/utils)
include_directories(${STB_HEADER})
include_directories(${OPENGL_DIR})
include_directories(${GLM_DIR})

add_library( # Sets the name of the library.
        pixelator

        # Sets the library as a shared library.
        SHARED
        gl/FrameBuffer.cpp
        gl/Program.cpp
        render/ImageFrameBuffer.cpp
        render/PaintRender.cpp
        render/PixelationRender.cpp
        render/ScreenRender.cpp
        thread/Handler.cpp
        thread/HandlerThread.cpp
        thread/Looper.cpp
        thread/Message.cpp
        thread/MessageQueue.cpp
        utils/TimeUtils.cpp
        Effect.cpp
        EGLCore.cpp
        ImageEngine.cpp
        JNIEnvironment.cpp
        NativeLib.cpp)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        pixelator
        log
        OpenSLES
        EGL
        GLESv3
        android
        jnigraphics)
