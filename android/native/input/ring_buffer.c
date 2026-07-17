// Ring Buffer — lock-free SPSC input event queue
#include "ring_buffer.h"
#include <string.h>

void ll_ring_init(ll_ring_buffer_t* rb) {
    memset(rb->events, 0, sizeof(rb->events));
    atomic_init(&rb->head,    0);
    atomic_init(&rb->tail,    0);
    atomic_init(&rb->dropped, 0);
    atomic_init(&rb->total,   0);
}

bool ll_ring_push(ll_ring_buffer_t* rb, const ll_input_event_t* ev) {
    int head = atomic_load(&rb->head);
    int next = (head + 1) % LL_RING_SIZE;

    // drop if full
    if (next == atomic_load(&rb->tail)) {
        atomic_fetch_add(&rb->dropped, 1);
        atomic_fetch_add(&rb->total, 1);
        return false;
    }

    rb->events[head] = *ev;
    atomic_store(&rb->head, next);
    atomic_fetch_add(&rb->total, 1);
    return true;
}

bool ll_ring_pop(ll_ring_buffer_t* rb, ll_input_event_t* ev) {
    int tail = atomic_load(&rb->tail);
    if (tail == atomic_load(&rb->head)) return false; // empty

    *ev = rb->events[tail];
    atomic_store(&rb->tail, (tail + 1) % LL_RING_SIZE);
    return true;
}

int ll_ring_dropped(const ll_ring_buffer_t* rb) {
    return atomic_load(&rb->dropped);
}

float ll_ring_utilization(const ll_ring_buffer_t* rb) {
    int h = atomic_load(&rb->head);
    int t = atomic_load(&rb->tail);
    int count = (h - t + LL_RING_SIZE) % LL_RING_SIZE;
    return (float)count / (float)LL_RING_SIZE;
}

void ll_ring_push_batch(ll_ring_buffer_t* rb, const int32_t* events, int count) {
    // Decode: [count][type,action,x,y,pressure,pointer,...] repeated
    int idx = 0;
    while (idx < count) {
        ll_input_event_t ev = {0};
        ev.type     = events[idx++];
        ev.action   = events[idx++];
        ev.x        = (float)events[idx++] / 10000.0f;
        ev.y        = (float)events[idx++] / 10000.0f;
        ev.pressure = (float)events[idx++] / 10000.0f;
        ev.pointer_id = events[idx++];
        ll_ring_push(rb, &ev);
    }
}
