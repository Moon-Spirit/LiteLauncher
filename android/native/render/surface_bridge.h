// 跨进程 Surface 桥 — 接收 Flutter 侧 Surface，创建 EGL 上下文
#ifndef LL_SURFACE_BRIDGE_H
#define LL_SURFACE_BRIDGE_H

#include <stdbool.h>
#include <EGL/egl.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include "engine.h"

// EGL 上下文
typedef struct {
    EGLDisplay display;
    EGLConfig  config;
    EGLContext context;
    EGLSurface surface;
    int        width;
    int        height;
    int        refresh_rate;  // Hz
} ll_egl_context_t;

// 从 Android Surface 创建 EGL 上下文
bool ll_surface_create(ANativeWindow* window, ll_egl_context_t* ctx, ll_render_mode_t mode);

// 交换缓冲区
bool ll_surface_swap(ll_egl_context_t* ctx);

// 销毁 EGL 上下文
void ll_surface_destroy(ll_egl_context_t* ctx);

// 检查 GL 扩展是否可用
bool ll_surface_has_extension(const char* name);

// 获取当前帧率
float ll_surface_get_fps(void);

// 自适应帧率：当丢帧 >10% 持续 5 秒，降低到 30fps
void ll_surface_adaptive_vsync(ll_egl_context_t* ctx);

#endif // LL_SURFACE_BRIDGE_H
