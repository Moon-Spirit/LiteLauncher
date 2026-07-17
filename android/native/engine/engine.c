// LiteLauncher 引擎 — 核心实现
#include "engine.h"
#include "jvm_bootstrap.h"
#include "surface_bridge.h"
#include "renderer_dispatch.h"
#include "ring_buffer.h"
#include "ipc_protocol.h"
#include <string.h>

static ll_engine_state_t g_state = LL_STATE_IDLE;
static ll_ipc_header_t* g_ipc = NULL;
static ll_ring_buffer_t g_input_ring;
static ll_egl_context_t g_egl_ctx;
static ll_renderer_handle_t g_renderer = NULL;

bool ll_engine_init(void) {
    LL_LOGI("引擎初始化开始");
    g_state = LL_STATE_IDLE;

    // 初始化输入 ring buffer
    ll_ring_init(&g_input_ring);

    // 创建 IPC 共享内存
    int ipc_fd = -1;
    g_ipc = ll_ipc_create(&ipc_fd);
    if (!g_ipc) {
        LL_LOGE("IPC 共享内存创建失败");
        return false;
    }
    ll_ipc_set_state(g_ipc, LL_STATE_IDLE);

    LL_LOGI("引擎初始化完成 (IPC fd=%d)", ipc_fd);
    return true;
}

ll_engine_state_t ll_engine_get_state(void) {
    return g_state;
}

void ll_engine_shutdown(void) {
    LL_LOGI("引擎关闭");
    g_state = LL_STATE_STOPPED;

    if (g_renderer) {
        ll_render_shutdown(g_renderer);
        g_renderer = NULL;
    }

    ll_surface_destroy(&g_egl_ctx);
    ll_ipc_close(g_ipc);
    g_ipc = NULL;
    g_state = LL_STATE_STOPPED;
}

// ====== JNI 实现 ======

JNIEXPORT jboolean JNICALL
Java_com_litelauncher_runtime_MCRuntimeService_nativeInitEngine(JNIEnv* env, jobject thiz) {
    (void)thiz;
    return ll_engine_init() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jboolean JNICALL
Java_com_litelauncher_runtime_MCRuntimeService_nativeBootstrapJVM(
    JNIEnv* env, jobject thiz,
    jstring jrePath, jstring classpath, jstring mainClass,
    jobjectArray jvmArgs, jobjectArray mcArgs
) {
    (void)thiz;
    // 解析 JNI 字符串
    const char* jre = (*env)->GetStringUTFChars(env, jrePath, NULL);
    const char* cp  = (*env)->GetStringUTFChars(env, classpath, NULL);
    const char* mc  = (*env)->GetStringUTFChars(env, mainClass, NULL);

    // 解析 JVM 参数数组
    jint jvm_count = (*env)->GetArrayLength(env, jvmArgs);
    const char* jvm_argv[jvm_count];
    for (int i = 0; i < jvm_count; i++) {
        jstring s = (jstring)(*env)->GetObjectArrayElement(env, jvmArgs, i);
        jvm_argv[i] = (*env)->GetStringUTFChars(env, s, NULL);
    }

    // 解析 MC 参数数组
    jint mc_count = (*env)->GetArrayLength(env, mcArgs);
    const char* mc_argv[mc_count];
    for (int i = 0; i < mc_count; i++) {
        jstring s = (jstring)(*env)->GetObjectArrayElement(env, mcArgs, i);
        mc_argv[i] = (*env)->GetStringUTFChars(env, s, NULL);
    }

    g_state = LL_STATE_JVM_LOADING;
    ll_ipc_set_state(g_ipc, LL_STATE_JVM_LOADING);

    ll_jvm_result_t result = {0};
    bool ok = ll_jvm_bootstrap(jre, cp, mc, jvm_argv, jvm_count, mc_argv, mc_count, &result);

    // 释放 JNI 引用
    (*env)->ReleaseStringUTFChars(env, jrePath, jre);
    (*env)->ReleaseStringUTFChars(env, classpath, cp);
    (*env)->ReleaseStringUTFChars(env, mainClass, mc);

    if (!ok || result.exit_code != 0) {
        g_state = LL_STATE_CRASHED;
        ll_ipc_set_crash(g_ipc, result.crash_report);
    } else {
        g_state = LL_STATE_STOPPED;
    }
    ll_ipc_set_state(g_ipc, g_state);

    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_com_litelauncher_runtime_MCRuntimeService_nativeSetSurface(
    JNIEnv* env, jobject thiz, jobject surface
) {
    (void)thiz;
    ANativeWindow* window = ANativeWindow_fromSurface(env, surface);
    if (!window) {
        LL_LOGE("无法从 Surface 创建 ANativeWindow");
        return;
    }

    if (ll_surface_create(window, &g_egl_ctx, LL_RENDER_UNKNOWN)) {
        g_state = LL_STATE_SURFACE_READY;
        ll_ipc_set_state(g_ipc, LL_STATE_SURFACE_READY);
        LL_LOGI("EGL 上下文创建成功 (%dx%d)", g_egl_ctx.width, g_egl_ctx.height);
    } else {
        LL_LOGE("EGL 上下文创建失败");
    }
}

JNIEXPORT void JNICALL
Java_com_litelauncher_runtime_MCRuntimeService_nativeInjectInput(
    JNIEnv* env, jobject thiz, jintArray events
) {
    (void)thiz;
    jint* buf = (*env)->GetIntArrayElements(env, events, NULL);
    jint len  = (*env)->GetArrayLength(env, events);
    ll_ring_push_batch(&g_input_ring, buf, len);
    (*env)->ReleaseIntArrayElements(env, events, buf, JNI_ABORT);
}

JNIEXPORT void JNICALL
Java_com_litelauncher_runtime_MCRuntimeService_nativeStop(JNIEnv* env, jobject thiz) {
    (void)env; (void)thiz;
    g_state = LL_STATE_STOPPED;
    ll_ipc_set_state(g_ipc, LL_STATE_STOPPED);
    ll_engine_shutdown();
}

JNIEXPORT jstring JNICALL
Java_com_litelauncher_runtime_MCRuntimeService_nativeGetCrashReport(JNIEnv* env, jobject thiz) {
    (void)thiz;
    if (g_ipc && g_ipc->crash_buf[0]) {
        return (*env)->NewStringUTF(env, g_ipc->crash_buf);
    }
    return (*env)->NewStringUTF(env, "");
}

JNIEXPORT jint JNICALL
Java_com_litelauncher_runtime_MCRuntimeService_nativeGetState(JNIEnv* env, jobject thiz) {
    (void)env; (void)thiz;
    return (jint)g_state;
}

JNIEXPORT void JNICALL
Java_com_litelauncher_runtime_MCRuntimeService_nativeSetRenderMode(
    JNIEnv* env, jobject thiz, jint mode
) {
    (void)env; (void)thiz;
    LL_LOGI("设置渲染模式: %d", mode);
    ll_ipc_header_t* h = g_ipc;
    if (h) atomic_store(&h->render_mode, (int)mode);
}
