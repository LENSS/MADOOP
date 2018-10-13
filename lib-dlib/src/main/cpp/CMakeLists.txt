cmake_minimum_required(VERSION 3.4.1)

SET(OpenCV_DIR $ENV{HOME}/AndroidDevelopment/opencv-3.2.0/sdk/native/jni)

# Configure build library name.
set(TARGET_NAME dlib_jni)

# Configure install folder for:
# 1) Finding the dependent libraries.
# 2) Copy out the generated files for others to use.
set(INSTALL_DIR ${CMAKE_SOURCE_DIR}/../../../../lib-distribution/)

# Configure import libs.
set(LIB_DIR ${CMAKE_SOURCE_DIR}/../cppLibs)

# Link to libdlib.so.
# Shared lib will also be tucked into APK and sent to target
# refer to app/build.gradle, jniLibs section for that purpose.
# ${ANDROID_ABI} is handy for our purpose here. Probably this ${ANDROID_ABI} is
# the most valuable thing of this sample, the rest are pretty much normal cmake
add_library(dlib SHARED IMPORTED)
set_target_properties(dlib PROPERTIES IMPORTED_LOCATION
    ${LIB_DIR}/dlib/lib/${ANDROID_ABI}/libdlib.so)

# Link to libprotobuf-lite.so.
add_library(protobuf SHARED IMPORTED)
set_target_properties(protobuf PROPERTIES IMPORTED_LOCATION
    ${LIB_DIR}/protobuf/lib/${ANDROID_ABI}/libprotobuf-lite-3.2.0.so)

# Build project shared lib
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fexceptions -std=c++11")
add_library(${TARGET_NAME} SHARED
            jni.cpp
            profiler.cpp
            dlib-face-landmarks-detector-jni.cpp
            include/my/dlib/data/messages.pb.cc)
target_include_directories(${TARGET_NAME} PRIVATE
                           ${CMAKE_SOURCE_DIR}/include
                           ${LIB_DIR}/protobuf/include
                           ${LIB_DIR}/dlib/include
                           ${CMAKE_SOURCE_DIR}/../../../../OpenCV-android-sdk/sdk/native/jni/include)
target_link_libraries(${TARGET_NAME}
                      lib_opencv
                      dlib
                      protobuf
                      android
                      jnigraphics
                      log)


add_library(lib_opencv SHARED IMPORTED)
set_target_properties(lib_opencv PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../../../../app/src/main/jniLibs/${ANDROID_ABI}/libopencv_java3.so)

include_directories(${CMAKE_SOURCE_DIR}/../../../../OpenCV-android-sdk/sdk/native/jni/include)

