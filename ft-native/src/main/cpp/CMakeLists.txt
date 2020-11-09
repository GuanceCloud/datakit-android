cmake_minimum_required(VERSION 3.4.1)
project(breakpad-core)

set(ENABLE_INPROCESS ON)
set(ENABLE_OUTOFPROCESS ON)
set(ENABLE_LIBCORKSCREW ON)
set(ENABLE_LIBUNWIND ON)
set(ENABLE_LIBUNWINDSTACK ON)
set(ENABLE_CXXABI ON)
set(ENABLE_STACKSCAN ON)

if (${ENABLE_INPROCESS})
    add_definitions(-DENABLE_INPROCESS)
endif ()
if (${ENABLE_OUTOFPROCESS})
    add_definitions(-DENABLE_OUTOFPROCESS)
endif ()


set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Werror=implicit-function-declaration")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 ")

# breakpad
include_directories(external/libbreakpad/src external/libbreakpad/src/common/android/include)
add_subdirectory(external/libbreakpad)
list(APPEND LINK_LIBRARIES breakpad)

add_library(breakpad-core SHARED
        breakpad.cpp)

target_link_libraries(breakpad-core ${LINK_LIBRARIES}
        log)


add_library( # Sets the name of the library.
        native_exception_lib
        # Sets the library as a shared library.
        SHARED
        # Provides a relative path to your source file(s).
        native_exception_lib.cpp)

find_library( # Sets the name of the path variable.
        log-lib
        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        native_exception_lib

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})