cmake_minimum_required(VERSION 3.22.1)

project("clip-android")

# Include paths for macOS (similar to `-I` options in Gradle)
include_directories(${JAVA_HOME}/include ${JAVA_HOME}/include/darwin)

# Set the appropriate compiler flags
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -I$ENV{JAVA_HOME}/include -I$ENV{JAVA_HOME}/include/darwin")

# Add local clip.cpp CMake project
add_subdirectory(../../../../clip.cpp build-clip)

add_library(
        ${CMAKE_PROJECT_NAME}
        SHARED
        clip_android.cpp)

set_target_properties(${CMAKE_PROJECT_NAME} PROPERTIES
    LIBRARY_OUTPUT_DIRECTORY ../../../../../build/libs/shared/clip)

target_link_libraries(${CMAKE_PROJECT_NAME}
        clip
        ggml)