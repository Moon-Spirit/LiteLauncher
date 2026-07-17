cmake_minimum_required(VERSION 3.22)
project(litelauncher_engine C)
set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)

set(NDK_DIR "/home/sanjiu/Android/Sdk/ndk/android-ndk-r29")
set(NDK_CLANG "${NDK_DIR}/toolchains/llvm/prebuilt/linux-aarch64/bin/clang")
set(NDK_SYSROOT "${NDK_DIR}/toolchains/llvm/prebuilt/linux-x86_64/sysroot")
set(API 26)

set(CMAKE_C_COMPILER ${NDK_CLANG})
set(CMAKE_C_COMPILER_TARGET aarch64-linux-android26)

add_compile_options(--target=aarch64-linux-android${API} --sysroot=${NDK_SYSROOT} -DANDROID -fPIC -fvisibility=hidden)
add_link_options(--target=aarch64-linux-android${API} --sysroot=${NDK_SYSROOT})

set(ENGINE_SOURCES
    engine/engine.c engine/jvm_bootstrap.c
    render/surface_bridge.c render/renderer_dispatch.c
    input/ring_buffer.c ipc/ipc_protocol.c
)

add_library(litelauncher_engine SHARED ${ENGINE_SOURCES})

target_include_directories(litelauncher_engine PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${CMAKE_CURRENT_SOURCE_DIR}/engine
    ${CMAKE_CURRENT_SOURCE_DIR}/render
    ${CMAKE_CURRENT_SOURCE_DIR}/input
    ${CMAKE_CURRENT_SOURCE_DIR}/ipc
)

# 直接链接库名（Android NDK 的库在前面路径）
target_link_libraries(litelauncher_engine -llog -landroid -lEGL -lGLESv2 -lOpenSLES)
target_compile_options(litelauncher_engine PRIVATE -Wall -Wextra -Wno-unused-parameter -O2 -g)
