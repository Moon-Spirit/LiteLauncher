// 输入 Ring Buffer — 跨进程 ashmem 共享内存
// 生产者: Flutter/Kotlin 侧 → AIDL injectInput → 写入 ring
// 消费者: MC GLFW 线程 → 读取 ring → 分发到 LWJGL 回调
#ifndef LL_RING_BUFFER_H
#define LL_RING_BUFFER_H

#include <stdint.h>
#include <stdbool.h>
#include <stdatomic.h>

// 输入事件类型
typedef enum {
    LL_INPUT_TOUCH_DOWN   = 0,
    LL_INPUT_TOUCH_MOVE   = 1,
    LL_INPUT_TOUCH_UP     = 2,
    LL_INPUT_KEY_DOWN     = 3,
    LL_INPUT_KEY_UP       = 4,
    LL_INPUT_MOUSE_MOVE   = 5,
    LL_INPUT_SCROLL       = 6,
} ll_input_type_t;

// 输入事件结构（16 字节，缓存行对齐）
typedef struct __attribute__((aligned(16))) {
    int64_t timestamp;     // ns
    int32_t type;          // ll_input_type_t
    int32_t action;        // 按键码 / 按钮码
    float   x;             // 窗口坐标 (0.0 - 1.0)
    float   y;
    float   pressure;      // 0.0 - 1.0
    int32_t pointer_id;
    int32_t _pad;          // 对齐到 64 字节
} ll_input_event_t;

// Ring Buffer (单生产者单消费者，无锁)
#define LL_RING_SIZE 1024  // 约 1 秒 @ 60fps 触控

typedef struct {
    ll_input_event_t events[LL_RING_SIZE];
    atomic_int       head;       // 生产者写入位置
    atomic_int       tail;       // 消费者读取位置
    atomic_int       dropped;    // 溢出丢帧计数
    atomic_int       total;      // 总写入计数
} ll_ring_buffer_t;

// 初始化 ring buffer
void ll_ring_init(ll_ring_buffer_t* rb);

// 生产者：写入一个事件（溢出丢最旧事件）
// 返回: true 成功写入，false 溢出（事件仍写入但计丢帧）
bool ll_ring_push(ll_ring_buffer_t* rb, const ll_input_event_t* ev);

// 消费者：读取一个事件
// 返回: true 有事件可读，false 缓冲区空
bool ll_ring_pop(ll_ring_buffer_t* rb, ll_input_event_t* ev);

// 获取丢帧计数
int ll_ring_dropped(const ll_ring_buffer_t* rb);

// 获取缓冲区利用率（0.0 - 1.0）
float ll_ring_utilization(const ll_ring_buffer_t* rb);

// 批量写入（从 JNI int[] 数组解码）
// events: 编码格式 [count][ev1_type,ev1_action,ev1_x_int,ev1_y_int,ev1_pressure_int,ev1_pointer,...]
void ll_ring_push_batch(ll_ring_buffer_t* rb, const int32_t* events, int count);

#endif // LL_RING_BUFFER_H
