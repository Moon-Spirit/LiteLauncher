// IPC 协议 — 进程间共享内存头
// 存储状态、帧率、崩溃信息等
#ifndef LL_IPC_PROTOCOL_H
#define LL_IPC_PROTOCOL_H

#include <stdint.h>
#include <stdatomic.h>
#include "engine.h"

// IPC 共享内存头
typedef struct {
    atomic_int state;          // ll_engine_state_t
    atomic_int render_mode;    // ll_render_mode_t
    atomic_int fps;            // 当前帧率
    atomic_int dropped_frames; // 丢帧计数
    atomic_int width;          // 窗口宽度
    atomic_int height;         // 窗口高度
    char        crash_buf[4096]; // 崩溃信息
} ll_ipc_header_t;

// 创建/映射 ashmem 共享内存
// fd: 输出 — 文件描述符（通过 Binder 传递给 Flutter 进程）
// 返回: 映射后的 IPC 头指针
ll_ipc_header_t* ll_ipc_create(int* fd);

// 从文件描述符映射已有共享内存
ll_ipc_header_t* ll_ipc_open(int fd);

// 释放共享内存
void ll_ipc_close(ll_ipc_header_t* header);

// 写入崩溃信息
void ll_ipc_set_crash(ll_ipc_header_t* header, const char* msg);

// 更新帧统计
void ll_ipc_update_frame(ll_ipc_header_t* header, int fps, int dropped);

// 设置状态
void ll_ipc_set_state(ll_ipc_header_t* header, ll_engine_state_t state);

// 读取状态（Flutter 进程侧）
ll_engine_state_t ll_ipc_get_state(const ll_ipc_header_t* header);

#endif // LL_IPC_PROTOCOL_H
