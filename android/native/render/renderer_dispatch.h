// 三级渲染分派 — MC 版本 → GL 翻译层选择
#ifndef LL_RENDERER_DISPATCH_H
#define LL_RENDERER_DISPATCH_H

#include <stdbool.h>
#include "engine.h"
#include "surface_bridge.h"

// 渲染器句柄
typedef void* ll_renderer_handle_t;

// 检测 MC 版本并选择渲染层级
// mc_version: MC 版本字符串（如 "1.12.2", "1.20.1", "26.2"）
// device_tier: 设备分级 (0=低, 1=中, 2=高)
// 返回: 选中的渲染模式
ll_render_mode_t ll_render_detect(const char* mc_version, int device_tier);

// 加载渲染器 .so
ll_renderer_handle_t ll_render_load(ll_render_mode_t mode);

// 初始化渲染器
bool ll_render_init(ll_renderer_handle_t renderer, ll_egl_context_t* ctx);

// 每帧交换（调用渲染器的 swap 函数）
bool ll_render_swap(ll_renderer_handle_t renderer, ll_egl_context_t* ctx);

// 释放渲染器
void ll_render_shutdown(ll_renderer_handle_t renderer);

// 检测 Sodium 的 AZDO 能力（GL_EXT_multi_draw_indirect）
bool ll_render_has_azdo(void);

#endif // LL_RENDERER_DISPATCH_H
