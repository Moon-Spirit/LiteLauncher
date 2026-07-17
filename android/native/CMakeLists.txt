cmake_minimum_required(VERSION 3.22)
project(litelauncher_engine C)

set(CMAKE_C_STANDARD 11)
set(CMAKE_C_STANDARD_REQUIRED ON)

# 仅构建 arm64-v8a
set(CMAKE_ANDROID_ARCH_ABI arm64-v8a)

# 查找依赖
find_library(log-lib log)
find_library(android-lib android)
find_library(EGL-lib EGL)
find_library(GLESv2-lib GLESv2)
find_library(OpenSLES-lib OpenSLES)

# 源文件
set(ENGINE_SOURCES
    engine/engine.c
    engine/jvm_bootstrap.c
    render/surface_bridge.c
    render/renderer_dispatch.c
    input/ring_buffer.c
    ipc/ipc_protocol.c
)

# 头文件
set(ENGINE_HEADERS
    engine/engine.h
    engine/jvm_bootstrap.h
    render/surface_bridge.h
    render/renderer_dispatch.h
    input/ring_buffer.h
    ipc/ipc_protocol.h
)

# 构建共享库
add_library(litelauncher_engine SHARED
    ${ENGINE_SOURCES}
    ${ENGINE_HEADERS}
)

# 链接
target_link_libraries(litelauncher_engine
    ${log-lib}
    ${android-lib}
    ${EGL-lib}
    ${GLESv2-lib}
    ${OpenSLES-lib}
)

# 编译选项
target_compile_options(litelauncher_engine PRIVATE
    -Wall -Wextra -Werror
    -fPIC -fvisibility=hidden
    -O2 -DNDEBUG
)

# 调试构建使用 ASAN
if(CMAKE_BUILD_TYPE STREQUAL "Debug")
    target_compile_options(litelauncher_engine PRIVATE
        -O0 -g -fsanitize=address -fno-omit-frame-pointer
    )
    target_link_options(litelauncher_engine PRIVATE
        -fsanitize=address
    )
endif()

# 安装到 APK
install(TARGETS litelauncher_engine
    LIBRARY DESTINATION lib/${CMAKE_ANDROID_ARCH_ABI}
)
