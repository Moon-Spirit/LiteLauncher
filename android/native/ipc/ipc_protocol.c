// IPC Protocol — ashmem shared memory between processes
#include "ipc_protocol.h"
#include <string.h>
#include <stdlib.h>
#include <sys/mman.h>
#include <unistd.h>

#if __ANDROID_API__ >= 26
#include <android/sharedmem.h>
#else
#include <sys/mman.h>
#include <linux/ashmem.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#endif

#define IPC_SIZE 8192

ll_ipc_header_t* ll_ipc_create(int* out_fd) {
    int fd = -1;

#if __ANDROID_API__ >= 26
    fd = ASharedMemory_create("litelauncher_ipc", IPC_SIZE);
#else
    fd = open("/dev/ashmem", O_RDWR);
    if (fd >= 0) {
        char name[] = "litelauncher_ipc";
        ioctl(fd, ASHMEM_SET_NAME, name);
        ioctl(fd, ASHMEM_SET_SIZE, IPC_SIZE);
    }
#endif

    if (fd < 0) {
        LL_LOGE("ashmem create failed");
        return NULL;
    }

    ll_ipc_header_t* header = (ll_ipc_header_t*)mmap(
        NULL, IPC_SIZE, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0
    );

    if (header == MAP_FAILED) {
        LL_LOGE("mmap failed");
        close(fd);
        return NULL;
    }

    memset(header, 0, IPC_SIZE);
    atomic_init(&header->state, LL_STATE_IDLE);
    atomic_init(&header->render_mode, LL_RENDER_UNKNOWN);
    atomic_init(&header->fps, 0);
    atomic_init(&header->dropped_frames, 0);
    atomic_init(&header->width, 0);
    atomic_init(&header->height, 0);

    *out_fd = fd;
    LL_LOGI("IPC shared memory created: fd=%d, size=%d", fd, IPC_SIZE);
    return header;
}

ll_ipc_header_t* ll_ipc_open(int fd) {
    ll_ipc_header_t* header = (ll_ipc_header_t*)mmap(
        NULL, IPC_SIZE, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0
    );
    return (header == MAP_FAILED) ? NULL : header;
}

void ll_ipc_close(ll_ipc_header_t* header) {
    if (!header) return;
    munmap(header, IPC_SIZE);
}

void ll_ipc_set_crash(ll_ipc_header_t* header, const char* msg) {
    if (!header || !msg) return;
    strncpy(header->crash_buf, msg, sizeof(header->crash_buf) - 1);
    header->crash_buf[sizeof(header->crash_buf) - 1] = '\0';
}

void ll_ipc_update_frame(ll_ipc_header_t* header, int fps, int dropped) {
    if (!header) return;
    atomic_store(&header->fps, fps);
    atomic_store(&header->dropped_frames, dropped);
}

void ll_ipc_set_state(ll_ipc_header_t* header, ll_engine_state_t state) {
    if (!header) return;
    atomic_store(&header->state, (int)state);
}

ll_engine_state_t ll_ipc_get_state(const ll_ipc_header_t* header) {
    if (!header) return LL_STATE_IDLE;
    return (ll_engine_state_t)atomic_load(&header->state);
}
